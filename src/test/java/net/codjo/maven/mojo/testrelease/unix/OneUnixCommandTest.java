package net.codjo.maven.mojo.testrelease.unix;
import net.codjo.maven.mojo.testrelease.util.ChannelExecMock;
import net.codjo.test.common.LogString;
import junit.framework.TestCase;
/**
 * Classe de test de {@link OneUnixCommand}.
 */
public class OneUnixCommandTest extends TestCase {
    private LogString log = new LogString();
    private ChannelExecMock exec = new ChannelExecMock(log);
    private OneUnixCommand command;


    public void testExecuteOneCommand() throws Exception {
        command.execute(exec, "ls -als");

        assertFinalStepLog();
    }


    public void test_execute_badExitStatus() throws Exception {
        exec.mockCommandErrorOutput("error");
        exec.mockCommandOutput("command output");
        exec.mockExitStatus(-1);

        try {
            command.execute(exec, "ls -als");
            fail();
        }
        catch (OneUnixCommand.CommandException ex) {
            assertEquals("Excution error !\n[output]\ncommand output\n[error]\nerror", ex.getMessage());
        }

        assertFinalStepLog();
    }


    public void test_execute_withErrorOutput() throws Exception {
        exec.mockCommandErrorOutput("mv: cannot access /java/mint-java.log");
        exec.mockCommandOutput("command output");
        exec.mockExitStatus(0);

        try {
            command.execute(exec, "ls -als");
            fail();
        }
        catch (OneUnixCommand.CommandException ex) {
            assertEquals("Excution error !\n"
                         + "[output]\ncommand output\n[error]\nmv: cannot access /java/mint-java.log",
                         ex.getMessage());
        }

        assertFinalStepLog();
    }


    private void assertFinalStepLog() {
        log.assertContent("setCommand(ls -als)"
                          + ", connect()"
                          + ", getExtInputStream()"
                          + ", getInputStream()"
                          + ", isClosed()"
                          + ", isClosed()"
                          + ", getExitStatus()"
                          + ", disconnect()");
    }


    protected void setUp() throws Exception {
        command = new OneUnixCommand(new UnixSessionFactory("login", "host"));
    }
}
