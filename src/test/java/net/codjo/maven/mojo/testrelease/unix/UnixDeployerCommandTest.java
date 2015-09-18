package net.codjo.maven.mojo.testrelease.unix;
import com.jcraft.jsch.SftpException;
import java.io.File;
import junit.framework.TestCase;
import net.codjo.maven.mojo.testrelease.util.ChannelExecMock;
import net.codjo.maven.mojo.testrelease.util.ChannelSftpMock;
import net.codjo.test.common.LogString;
import net.codjo.test.release.util.ssh.SecureCommand;
import net.codjo.test.release.util.ssh.UnixSessionFactory;
/**
 * Classe de test de {@link UnixDeployerCommand}.
 */
public class UnixDeployerCommandTest extends TestCase {
    private static final String ZIP_FILE =
          new File("src/test/resources/deployer/serveur-1.0.zip").getPath();
    private static final String ZIP_FILE_NO_SHELL =
          new File("src/test/resources/deployer/test-resource-1.0.zip").getPath();
    private LogString log = new LogString();
    private ChannelSftpMock sftp = new ChannelSftpMock(log);
    private ChannelExecMock exec = new ChannelExecMock(log);
    private UnixDeployerCommand deployer;


    public void testUploadZip() throws Exception {
        deployer.uploadZip(sftp, new File(ZIP_FILE), "/my-app");

        log.assertContent("connect()"
                          + ", cd(/my-app)"
                          + ", rm(serveur-1.0.zip)"
                          + ", put(" + ZIP_FILE + ", ., monitor, ChannelSftp.OVERWRITE)"
                          + ", exit()"
                          + ", disconnect()");
    }


    public void testUploadResource() throws Exception {
        File zipFile = new File(ZIP_FILE_NO_SHELL);
        deployer.finalStep(exec, zipFile, "/my-app", "nonRienDuTout");

        log.assertContent(
              "setCommand(cd /my-app; unzip "
              + zipFile.getName() + "; rm -rf "
              + zipFile.getName() + ")"
              + ", connect()"
              + ", getExtInputStream()"
              + ", getInputStream()"
              + ", isClosed()"
              + ", isClosed()"
              + ", getExitStatus()"
              + ", disconnect()");
    }


    public void testUploadZipForTheFirstTime() throws Exception {
        sftp.mockRmFailure(new SftpException(0, "i dont want"));

        deployer.uploadZip(sftp, new File(ZIP_FILE), "/my-app");

        log.assertContent("connect()"
                          + ", cd(/my-app)"
                          + ", put(" + ZIP_FILE + ", ., monitor, ChannelSftp.OVERWRITE)"
                          + ", exit()"
                          + ", disconnect()");
    }


    public void testUploadError() throws Exception {

        SftpException putFailureMock = new SftpException(0, "i dont want");
        sftp.mockPutFailure(putFailureMock);
        try {
            deployer.uploadZip(sftp, new File(ZIP_FILE), "/my-app");
            fail();
        }
        catch (SftpException ex) {
            assertSame(putFailureMock, ex);
        }

        log.assertContent("connect()"
                          + ", cd(/my-app)"
                          + ", rm(serveur-1.0.zip)"
                          + ", exit()"
                          + ", disconnect()");
    }


    public void testUploadErrorDueToInexistantDirectory() throws Exception {
        SftpException failure = new SftpException(0, "i dont want");
        sftp.mockLsFailure(failure);
        try {
            deployer.uploadZip(sftp, new File(ZIP_FILE), "/my-app");
            fail();
        }
        catch (UnixDeployerCommand.NoSuchRemoteDirectoryException ex) {
            assertEquals("Remote directory '/my-app' does not exist", ex.getMessage());
            assertSame(failure, ex.getCause());
        }

        log.assertContent("connect()"
                          + ", exit()"
                          + ", disconnect()");
    }


    public void testFinalStep() throws Exception {
        deployer.finalStep(exec, new File(ZIP_FILE), "/my-app", "SERVEUR");

        assertFinalStepLog();
    }


    public void testFinalStepWithCommandError() throws Exception {
        exec.mockCommandErrorOutput("error");
        exec.mockCommandOutput("command output");
        exec.mockExitStatus(-1);

        try {
            deployer.finalStep(exec, new File(ZIP_FILE), "/my-app", "SERVEUR");
            fail();
        }
        catch (OneUnixCommand.CommandException ex) {
            assertEquals("Excution error !\n[output]\ncommand output\n[error]\nerror", ex.getMessage());
        }

        assertFinalStepLog();
    }


    protected void setUp() throws Exception {
        deployer = new UnixDeployerCommand(new UnixSessionFactory("login",
                                                                  "mikros.local",
                                                                  SecureCommand.DEFAULT_SSH_PORT));
    }


    private void assertFinalStepLog() {
        log.assertContent(
              "setCommand(cd /my-app; rm -rf SERVEUR; unzip serveur-1.0.zip; rm -rf serveur-1.0.zip; cd SERVEUR; chmod +x *.*sh)"
              + ", connect()"
              + ", getExtInputStream()"
              + ", getInputStream()"
              + ", isClosed()"
              + ", isClosed()"
              + ", getExitStatus()"
              + ", disconnect()");
    }
}
