package net.codjo.maven.mojo.testrelease;
import java.io.File;
import java.io.IOException;
import org.apache.maven.plugin.MojoExecutionException;
/**
 *
 */
public class StartWebMojoTest extends AbstractTestReleaseMojoTestCase {

    public void test_execute() throws Exception {
        StartWebMojo mojo = initMojo("startWeb/pom-default.xml");
        mojo.setJavaExecutor(new JavaExecutorMock(log));

        mojo.execute();

        String classPath = "[icomp-web-1.00.00.00-a-SNAPSHOT.jar, jetty-util-6.1.3.jar]";

        assertLog("setSpawnProcess(true)"
                  + ", execute(net.codjo.icomp.ICompWebServer, " + classPath
                  + ", -configuration %mojo-dir%/startWeb/web-config.properties -testMode true)");
    }


    public void test_classpath() throws Exception {
        StartWebMojo mojo = initMojo("startWeb/pom-default.xml");

        File[] classPath = mojo.getClasspath();

        assertEquals(2, classPath.length);

        assertEquals("icomp-web-1.00.00.00-a-SNAPSHOT.jar", classPath[0].getName());
        assertTrue(classPath[0].exists());

        assertEquals("jetty-util-6.1.3.jar", classPath[1].getName());
        assertTrue(classPath[1].exists());
    }


    public void test_execute_remote() throws Exception {
        StartWebMojo mojo = initMojo("startWeb/pom-remote.xml");
        mojo.setWaitTimeBeforeCheckStatus(0);
        mockShellCommand(mojo);

        mojo.execute();

        log.assertContent("createShellCommand(my-login, dummyServer)"
                          + ", setLog(...)"
                          + ", execute(. ~/.profile;cd /unix/directory/WEB;./web.sh start)"
                          + ", createShellCommand(my-login, dummyServer), setLog(...)"
                          + ", execute(. ~/.profile;cd /unix/directory/WEB;./web.sh status)");
    }


    public void test_execute_remote_withFailure() throws Exception {
        AbstractStartMojo mojo = initMojo("startWeb/pom-remote.xml");

        IOException failure = new IOException("failure");
        mockShellCommandFailure(mojo, failure);

        try {
            mojo.execute();
            fail();
        }
        catch (MojoExecutionException ex) {
            assertEquals("Démarrage du serveur en erreur: \nfailure", ex.getLocalizedMessage());
            assertSame(failure, ex.getCause());
        }
    }


    public void test_execute_withoutWebModule() throws Exception {
        AbstractStartMojo mojo = initMojo("startWeb/pom-withoutWeb.xml");
        assertFalse(mojo.canExecuteGoal());
    }


    protected StartWebMojo initMojo(String pomFilePath) throws Exception {
        return (StartWebMojo)initMojo("start-web", pomFilePath);
    }
}
