package net.codjo.maven.mojo.testrelease;
import java.io.IOException;
/**
 *
 */
public class StatusWebMojoTest extends AbstractTestReleaseMojoTestCase {

    public void test_execute_remote() throws Exception {
        StatusWebMojo mojo = initMojo("statusWeb/pom-remote.xml");
        mockShellCommand(mojo);

        mojo.execute();

        log.assertContent("createShellCommand(my-login, dummyServer)"
                          + ", setLog(...)"
                          + ", execute(. ~/.profile;cd /unix/directory/WEB;./web.sh status)");
    }


    public void test_execute_remote_withFailure() throws Exception {
        StatusWebMojo mojo = initMojo("statusWeb/pom-remote.xml");

        IOException failure = new IOException("failure");
        mockShellCommandFailure(mojo, failure);

        mojo.execute();
    }


    private StatusWebMojo initMojo(String pomFilePath) throws Exception {
        return (StatusWebMojo)initMojo("status-web", pomFilePath);
    }
}
