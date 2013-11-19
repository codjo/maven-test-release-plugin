/*
 * Team : CODJO AM / OSI / SI / BO
 *
 * Copyright (c) 2001 CODJO Asset Management.
 */
package net.codjo.maven.mojo.testrelease;
import java.io.File;
import java.io.IOException;
import org.apache.maven.plugin.MojoExecutionException;
/**
 *
 */
public class StartServerMojoTest extends AbstractTestReleaseMojoTestCase {

    public void test_execute() throws Exception {
        StartServerMojo mojo = initMojo("startServer/pom-default.xml");
        mojo.setJavaExecutor(new JavaExecutorMock(log));

        mojo.execute();

        String classPath = "[base64-3.3.jar, codjo-variable-1.14.jar, mint-server-1.0-SNAPSHOT.jar]";

        assertLog("setSpawnProcess(true)"
                  + ", setWorkingDir(src/test/resources-filtered/mojos/startServer/SERVEUR)"
                  + ", setJvmArg(-Dlog.dir=src/test/resources-filtered/mojos/startServer/SERVEUR)"
                  + ", execute(net.codjo.mint.ServerMain, " + classPath
                  + ", -configuration %mojo-dir%/startServer/server-config.properties)"
        );
    }


    public void test_execute_with_jvmArgs() throws Exception {
        StartServerMojo mojo = initMojo("startServer/pom-local-jvmArgs.xml");
        mojo.setJavaExecutor(new JavaExecutorMock(log));

        mojo.execute();

        String classPath = "[base64-3.3.jar, codjo-variable-1.14.jar, mint-server-1.0-SNAPSHOT.jar]";

        assertLog("setSpawnProcess(true)"
                  + ", setWorkingDir(src/test/resources-filtered/mojos/startServer/SERVEUR)"
                  + ", setJvmArg(-Dlog.dir=src/test/resources-filtered/mojos/startServer/SERVEUR -Xmx128m)"
                  + ", execute(net.codjo.mint.ServerMain, " + classPath
                  + ", -configuration %mojo-dir%/startServer/server-config.properties)"
        );
    }


    public void test_execute_with_metrics() throws Exception {
        StartServerMojo mojo = initMojo("startServer/pom-metrics.xml");
        mojo.setJavaExecutor(new JavaExecutorMock(log));

        mojo.execute();

        assertLog("setSpawnProcess(true)"
                  + ", setWorkingDir(src/test/resources-filtered/mojos/startServer/SERVEUR)"
                  + ", setJvmArg(-Dlog.dir=src/test/resources-filtered/mojos/startServer/SERVEUR \"-javaagent:%emmaAgentJar%=-f my/group/id/* -o target/release-test.es\" -Demma.rt.control=false -Dcoverage.out.merge=true)"
                  + ", execute(net.codjo.reflect.collect.PreloadClassesMainWrapper, [base64-3.3.jar, codjo-variable-1.14.jar, mint-server-1.0-SNAPSHOT.jar, %reflectClassPath%, %emmaClassPath%], my.group.id net.codjo.mint.ServerMain -configuration %mojo-dir%/startServer/server-config.properties)"
        );
    }


    public void test_classpath() throws Exception {
        StartServerMojo mojo = initMojo("startServer/pom-default.xml");

        File[] classPath = mojo.getClasspath();

        assertEquals(3, classPath.length);

        assertEquals("base64-3.3.jar", classPath[0].getName());
        assertTrue(classPath[0].exists());

        assertEquals("codjo-variable-1.14.jar", classPath[1].getName());
        assertTrue(classPath[1].exists());

        assertEquals("mint-server-1.0-SNAPSHOT.jar", classPath[2].getName());
        assertTrue(classPath[2].exists());
    }


    public void test_execute_remote() throws Exception {
        StartServerMojo mojo = initMojo("startServer/pom-remote.xml");
        mojo.setWaitTimeBeforeCheckStatus(0);
        mockShellCommand(mojo);

        mojo.execute();

        log.assertContent("createShellCommand(my-login, dummyServer)"
                          + ", setLog(...)"
                          + ", execute(. ~/.profile;cd /unix/directory/SERVEUR;./server.sh start)"
                          + ", createShellCommand(my-login, dummyServer), setLog(...)"
                          + ", execute(. ~/.profile;cd /unix/directory/SERVEUR;./server.sh status)");
    }


    public void test_execute_remote_withFailure() throws Exception {
        StartServerMojo mojo = initMojo("startServer/pom-remote.xml");

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


    public void test_execute_remoteWindows() throws Exception {
        StartServerMojo mojo = initMojo("startServer/pom-windows-remote.xml");
        mojo.setWaitTimeBeforeCheckStatus(0);
        mockDosCommand(mojo);

        mojo.execute();

        log.assertContent("createDosCommand()"
                          + ", setLog(...)"
                          + ", execute(sc \\\\dummy-server start DELRECO_INT)"
                          + ", createDosCommand()"
                          + ", setLog(...)"
                          + ", execute(sc \\\\dummy-server query DELRECO_INT)");
    }


    public void test_execute_remoteWindows_withFailure() throws Exception {
        StartServerMojo mojo = initMojo("startServer/pom-windows-remote.xml");

        IOException failure = new IOException("failure");
        mockDosCommandFailure(mojo, failure);

        try {
            mojo.execute();
            fail();
        }
        catch (MojoExecutionException ex) {
            assertEquals("Démarrage du serveur en erreur: \nfailure", ex.getLocalizedMessage());
            assertSame(failure, ex.getCause());
        }
    }


    public void test_execute_withoutServerModule() throws Exception {
        AbstractStartMojo mojo = initMojo("startServer/pom-withoutServer.xml");
        assertFalse(mojo.canExecuteGoal());
    }


    protected StartServerMojo initMojo(String pomFilePath) throws Exception {
        return (StartServerMojo)initMojo("start-server", pomFilePath);
    }
}
