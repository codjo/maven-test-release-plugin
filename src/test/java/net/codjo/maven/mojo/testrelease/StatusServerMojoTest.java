package net.codjo.maven.mojo.testrelease;
import java.io.IOException;
/**
 *
 */
public class StatusServerMojoTest extends AbstractTestReleaseMojoTestCase {

    public void test_execute_remote() throws Exception {
        StatusServerMojo mojo = initMojo("statusServer/pom-remote.xml");
        mockShellCommand(mojo);

        mojo.execute();

        log.assertContent("createShellCommand(my-login, dummyServer)"
                          + ", setLog(...)"
                          + ", execute(. ~/.profile;cd /unix/directory/SERVEUR;./server.sh status)");
    }


    public void test_execute_remote_withFailure() throws Exception {
        StatusServerMojo mojo = initMojo("statusServer/pom-remote.xml");

        IOException failure = new IOException("failure");
        mockShellCommandFailure(mojo, failure);

        mojo.execute();
    }


    public void test_execute_remoteWindows() throws Exception {
        StatusServerMojo mojo = initMojo("statusServer/pom-windows-remote.xml");
        mockDosCommand(mojo);

        mojo.execute();

        log.assertContent("createDosCommand()"
                          + ", setLog(...)"
                          + ", execute(sc \\\\dummy-server query DELRECO_INT)");
    }


    public void test_execute_remoteWindows_withFailure() throws Exception {
        StatusServerMojo mojo = initMojo("statusServer/pom-windows-remote.xml");

        mockDosCommandFailure(mojo, new IOException("failure"));

        mojo.execute();
    }


    private StatusServerMojo initMojo(String pomFilePath) throws Exception {
        return (StatusServerMojo)initMojo("status-server", pomFilePath);
    }
}
