package net.codjo.maven.mojo.testrelease.unix;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.codjo.maven.mojo.testrelease.command.DeployerCommand;
import net.codjo.test.release.util.ssh.UnixSessionFactory;
/**
 *
 */
class UnixDeployerCommand extends AbstractUnixCommand implements DeployerCommand {
    private MySftpProgressMonitor monitor = new MySftpProgressMonitor();


    UnixDeployerCommand(UnixSessionFactory unixSessionFactory) {
        super(unixSessionFactory);
    }


    public void deploy(File zipFile, String applicationDirectory, String zipRootDirectory)
          throws JSchException, SftpException, IOException, OneUnixCommand.CommandException {
        if (!zipFile.exists()) {
            throw new IllegalArgumentException("Zip file do not exist : " + zipFile.getCanonicalPath());
        }

        Session session = connectSession();
        try {
            uploadZip((ChannelSftp)session.openChannel("sftp"), zipFile, applicationDirectory);
            finalStep((ChannelExec)session.openChannel("exec"),
                      zipFile,
                      applicationDirectory,
                      zipRootDirectory);
        }
        finally {
            session.disconnect();
        }
    }


    void uploadZip(ChannelSftp sftp, File zipFile, String applicationDirectory)
          throws IOException, SftpException, JSchException {
        sftp.connect();
        try {
            assertApplicationDirectoryExist(sftp, applicationDirectory);

            info("[sftp] cd " + applicationDirectory);
            sftp.cd(applicationDirectory);

            info("[sftp] rm " + zipFile.getName());
            silentRm(sftp, zipFile.getName());

            info("[sftp] put " + zipFile + " . OVERWRITE");
            sftp.put(zipFile.getPath(), ".", monitor, ChannelSftp.OVERWRITE);
        }
        finally {
            try {
                sftp.exit();
            }
            finally {
                sftp.disconnect();
            }
        }
    }


    void finalStep(ChannelExec exec, File zipFile, String applicationDirectory, String zipRootDirectory)
          throws IOException, JSchException, OneUnixCommand.CommandException {
        StringBuilder command = new StringBuilder("cd ").append(applicationDirectory);

        if (!"nonRienDuTout".equals(zipRootDirectory)) {
            command.append("; rm -rf ").append(zipRootDirectory);
        }

        command.append("; unzip ").append(zipFile.getName());
        command.append("; rm -rf ").append(zipFile.getName());

        if (!"nonRienDuTout".equals(zipRootDirectory)) {
            command.append("; cd ").append(zipRootDirectory);
        }

        if (zipContainsShell(zipFile)) {
            command.append("; chmod +x *.*sh");
        }

        OneUnixCommand oneShellCommand = new OneUnixCommand(getSessionFactory());
        oneShellCommand.setLog(getLog());
        oneShellCommand.execute(exec, command.toString());
    }


    private void silentRm(ChannelSftp sftp, String fileName) {
        try {
            sftp.rm(fileName);
        }
        catch (SftpException e) {
            debug("[sftp] file " + fileName + " do not exist");
        }
    }


    private boolean zipContainsShell(File zipFile) throws IOException {
        ZipFile zip = new ZipFile(zipFile);
        try {
            Enumeration enumeration = zip.entries();
            while (enumeration.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry)enumeration.nextElement();
                if (!zipEntry.isDirectory() && zipEntry.getName().matches(".*\\..*sh")) {
                    return true;
                }
            }
        }
        finally {
            zip.close();
        }
        return false;
    }


    private void assertApplicationDirectoryExist(ChannelSftp sftp, String applicationDirectory)
          throws NoSuchRemoteDirectoryException {
        try {
            info("Assert application directory exist : " + applicationDirectory);
            sftp.ls(applicationDirectory);
        }
        catch (SftpException e) {
            throw new NoSuchRemoteDirectoryException(applicationDirectory, e);
        }
    }


    private class MySftpProgressMonitor implements SftpProgressMonitor {
        private long current = 0;

        private long max = 0;

        private long percent = -1;


        public void init(int op, String src, String dest, long maxSize) {
            this.max = maxSize;
            current = 0;
            percent = -1;
        }


        public boolean count(long count) {
            this.current += count;
            if (percent >= this.current * 100 / max) {
                return true;
            }
            percent = this.current * 100 / max;
            info("Completed " + percent + "%");
            return true;
        }


        public void end() {
        }
    }

    public class NoSuchRemoteDirectoryException extends JSchException {
        public NoSuchRemoteDirectoryException(String path, Throwable cause) {
            super("Remote directory '" + path + "' does not exist", cause);
        }
    }
}
