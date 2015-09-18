package net.codjo.maven.mojo.testrelease.unix;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.IOException;
import java.io.InputStream;
import net.codjo.maven.mojo.testrelease.command.OneShellCommand;
import net.codjo.test.release.util.ssh.UnixSessionFactory;
/**
 *
 */
class OneUnixCommand extends AbstractUnixCommand implements OneShellCommand {

    OneUnixCommand(UnixSessionFactory sessionFactory) {
        super(sessionFactory);
    }


    public void execute(String shellCommand) throws CommandException, JSchException, IOException {
        Session session = connectSession();
        try {
            execute((ChannelExec)session.openChannel("exec"), shellCommand);
        }
        finally {
            session.disconnect();
        }
    }


    void execute(ChannelExec exec, String shellCommand) throws CommandException, IOException, JSchException {
        info("[ssh] " + shellCommand);

        exec.setCommand(shellCommand);

        exec.connect();

        InputStream errorStream = exec.getExtInputStream();
        String outputs = readStream(exec, exec.getInputStream());
        info(outputs);

        String errors = readStream(exec, errorStream);
        if (errors.length() > 0) {
            error(errors);
        }

        int exitStatus = exec.getExitStatus();
        info("[ssh] exit-status: " + exitStatus);
        exec.disconnect();

        if (exitStatus != 0 || errors.length() > 0) {
            throw new OneUnixCommand.CommandException("Excution error !"
                                                      + "\n[output]\n" + outputs
                                                      + "\n[error]\n" + errors);
        }
    }


    private static String readStream(Channel channel, InputStream in) throws IOException {
        StringBuffer result = new StringBuffer();
        byte[] buffer = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int read = in.read(buffer, 0, buffer.length);
                if (read < 0) {
                    break;
                }
                result.append(new String(buffer, 0, read));
            }
            if (channel.isClosed()) {
                break;
            }
            //noinspection EmptyCatchBlock
            try {
                Thread.sleep(500);
            }
            catch (Exception ee) {
            }
        }
        return result.toString();
    }


    public static class CommandException extends Exception {
        public CommandException(String message) {
            super(message);
        }
    }
}
