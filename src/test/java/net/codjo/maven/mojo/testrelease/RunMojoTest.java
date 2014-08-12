package net.codjo.maven.mojo.testrelease;
import java.io.File;
import java.util.HashSet;
import java.util.logging.Logger;
import net.codjo.maven.mojo.util.DefaultJavaExecutor;
import net.codjo.maven.mojo.util.TimeUtil;
import net.codjo.reflect.collect.ReflectUtil;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.stubs.ArtifactStub;
import org.joda.time.Duration;
import org.joda.time.Hours;
import org.joda.time.Minutes;
/**
 *
 */
public class RunMojoTest extends AbstractTestReleaseMojoTestCase {
    private static final Duration _2_HOURS_3_MINUTES = Hours.TWO
          .toStandardDuration()
          .plus(Minutes.THREE.toStandardDuration());


    public void testSetTimeout_default() {
        RunMojo mojo = new RunMojo();

        assertEquals("timeout", RunMojo.DEFAULT_TIMEOUT, mojo.timeout);
    }


    public void testSetTimeout_number() {
        testSetTimeout(Long.toString(_2_HOURS_3_MINUTES.getMillis()), _2_HOURS_3_MINUTES);
    }


    public void testSetTimeout_longString() {
        testSetTimeout("2 hours 3 minutes", _2_HOURS_3_MINUTES);
    }


    public void testSetTimeout_shortString() {
        testSetTimeout("2h 3m", _2_HOURS_3_MINUTES);
    }


    public void testSetTimeout_compactString() {
        testSetTimeout("2h3m", _2_HOURS_3_MINUTES);
    }


    private void testSetTimeout(String timeout, Duration expectedTimeout) {
        RunMojo mojo = new RunMojo();

        mojo.setTimeout(timeout);

        assertEquals("timeout", expectedTimeout, mojo.timeout);
    }


    public void test_execute_local() throws Exception {
        RunMojo mojo = initMojo("run/pom-default.xml");
        mojo.setRunJavaExecutor(new JavaExecutorMock(log));

        MockUtil.singleton.getProject().setArtifacts(new HashSet());
        MockUtil.singleton.getProject().getArtifacts().add(new ArtifactMock("common-1.jar"));
        MockUtil.singleton.getProject().setFile(new File("./my-basedir/pom.xml"));

        mojo.execute();

        assertLog("setTimeout(" + RunMojo.DEFAULT_TIMEOUT.getMillis() + ")"
                  + ", setWorkingDir(./my-basedir)"
                  + ", setJvmArg(-Xmx512m)"
                  + ", execute(net.codjo.test.release.ReleaseTestRunner, [common-1.jar], %target%/test-classes/mojos/run/release-test)");
    }


    public void test_execute_local_failure() throws Exception {
        RunMojo mojo = initMojo("run/pom-default.xml");

        JavaExecutorMock javaExecutor = new JavaExecutorMock(log);
        MojoExecutionException testFailure = new MojoExecutionException("failure");
        javaExecutor.mockExecuteFailure(testFailure);
        mojo.setRunJavaExecutor(javaExecutor);
        mojo.setShutdownJavaExecutor(javaExecutor);

        try {
            mojo.execute();
            fail();
        }
        catch (MojoExecutionException ex) {
            assertSame(testFailure, ex);
        }

        assertTrue(log.getContent().contains("execute(net.codjo.AdministrationShutdownerMock"));
        assertTrue(log.getContent().contains("localhost 16969"));
    }


    public void test_execute_local_failure_withoutServer() throws Exception {
        RunMojo mojo = initMojo("run/pom-withoutServer.xml");

        JavaExecutorMock javaExecutor = new JavaExecutorMock(log);
        javaExecutor.mockExecuteFailure(new MojoExecutionException("failure"));
        mojo.setRunJavaExecutor(javaExecutor);

        try {
            mojo.execute();
            fail();
        }
        catch (MojoExecutionException ex) {
        }

        assertFalse(log.getContent().contains("execute(net.codjo.AdministrationShutdownerMock"));
    }


    public void test_execute_local_with_metrics() throws Exception {
        RunMojo mojo = initMojo("run/pom-local-metrics.xml");
        mojo.setRunJavaExecutor(new JavaExecutorMock(log));

        MockUtil.singleton.getProject().setArtifacts(new HashSet());
        MockUtil.singleton.getProject().getArtifacts().add(new ArtifactMock(getReflectClasspath()));
        MockUtil.singleton.getProject().setFile(new File("./my-basedir/pom.xml"));

        mojo.execute();
        assertLog("setTimeout(" + RunMojo.DEFAULT_TIMEOUT.getMillis() + ")"
                  + ", setWorkingDir(./my-basedir)"
                  + ", setJvmArg(-Xmx512m \"-javaagent:%emmaAgentJar%=-f my/group/id/* -o target/release-test.es\" -Demma.rt.control=false -Dcoverage.out.merge=true)"
                  + ", execute(net.codjo.reflect.collect.PreloadClassesMainWrapper, [%reflectClassPath%, %emmaClassPath%], my.group.id net.codjo.test.release.ReleaseTestRunner %target%/test-classes/mojos/run/release-test)");
    }


    public void test_execute_local_with_metrics_appendReflect() throws Exception {
        RunMojo mojo = initMojo("run/pom-local-metrics.xml");
        mojo.setRunJavaExecutor(new JavaExecutorMock(log));

        MockUtil.singleton.getProject().setArtifacts(new HashSet());
        MockUtil.singleton.getProject().getArtifacts().add(new ArtifactMock("common-1.jar"));
        MockUtil.singleton.getProject().setFile(new File("./my-basedir/pom.xml"));

        mojo.execute();

        assertLog("setTimeout(" + RunMojo.DEFAULT_TIMEOUT.getMillis() + ")"
                  + ", setWorkingDir(./my-basedir)"
                  + ", setJvmArg(-Xmx512m \"-javaagent:%emmaAgentJar%=-f my/group/id/* -o target/release-test.es\" -Demma.rt.control=false -Dcoverage.out.merge=true)"
                  + ", execute(net.codjo.reflect.collect.PreloadClassesMainWrapper, [common-1.jar, %reflectClassPath%, %emmaClassPath%], my.group.id net.codjo.test.release.ReleaseTestRunner %target%/test-classes/mojos/run/release-test)");
    }


    public void test_execute_remote() throws Exception {
        RunMojo mojo = initMojo("run/pom-remote.xml");
        mojo.setRunJavaExecutor(new JavaExecutorMock(log));

        MockUtil.singleton.getProject().setArtifacts(new HashSet());
        MockUtil.singleton.getProject().getArtifacts().add(new ArtifactMock("common-1.jar"));
        MockUtil.singleton.getProject().setFile(new File("./my-basedir/pom.xml"));

        mojo.execute();

        assertLog("setTimeout(" + RunMojo.DEFAULT_TIMEOUT.getMillis() + ")"
                  + ", setWorkingDir(./my-basedir)"
                  + ", setJvmArg(-Xmx512m -Dagf.test.remote=yes)"
                  + ", execute(net.codjo.test.release.ReleaseTestRunner, [common-1.jar], %target%/test-classes/mojos/run/release-test)");
    }


    public void test_execute_localWithCustomTestFile() throws Exception {
        RunMojo mojo = initMojo("run/pom-local-customTestFile.xml");
        mojo.setRunJavaExecutor(new JavaExecutorMock(log));

        MockUtil.singleton.getProject().setArtifacts(new HashSet());
        MockUtil.singleton.getProject().getArtifacts().add(new ArtifactMock("common-1.jar"));
        MockUtil.singleton.getProject().setFile(new File("./my-basedir/pom.xml"));

        mojo.execute();

        assertLog("setTimeout(" + RunMojo.DEFAULT_TIMEOUT.getMillis() + ")"
                  + ", setWorkingDir(./my-basedir)"
                  + ", setJvmArg(-Xmx512m)"
                  + ", execute(net.codjo.test.release.ReleaseTestRunner, [common-1.jar], %target%/test-classes/mojos/run/usecase/firstTestRelease.xml)");
    }


    public void test_execute_localWithCustomTimeout() throws Exception {
        RunMojo mojo = initMojo("run/pom-local-customTimeout.xml");
        mojo.setRunJavaExecutor(new JavaExecutorMock(log));

        MockUtil.singleton.getProject().setArtifacts(new HashSet());
        MockUtil.singleton.getProject().getArtifacts().add(new ArtifactMock("common-1.jar"));
        MockUtil.singleton.getProject().setFile(new File("./my-basedir/pom.xml"));

        MockPlexusLogger logger = new MockPlexusLogger();
        mojo.setLog(new DefaultLog(logger));

        mojo.execute();

        assertLog("setTimeout(" + _2_HOURS_3_MINUTES.getMillis() + ")"
                  + ", setWorkingDir(./my-basedir)"
                  + ", setJvmArg(-Xmx512m)"
                  + ", execute(net.codjo.test.release.ReleaseTestRunner, [common-1.jar], %target%/test-classes/mojos/run/release-test)");
        StringBuffer expectedLog = new StringBuffer("[INFO] The global timeout of release tests execution is ");
        TimeUtil.printTo(expectedLog, _2_HOURS_3_MINUTES).append('.');
        logger.assertContains(expectedLog.toString());
    }


    public void test_execute_expiredTimeoutLogsExplicitMessage() throws Exception {
        execute_expiredTimeoutLogsExplicitMessage("run/pom-default.xml", Hours.FOUR.toStandardDuration());
    }


    public void test_execute_expiredTimeoutLogsExplicitMessage_customValue() throws Exception {
        execute_expiredTimeoutLogsExplicitMessage("run/pom-local-customTimeout.xml", _2_HOURS_3_MINUTES);
    }


    private void execute_expiredTimeoutLogsExplicitMessage(String pomFilePath, Duration expectedTimeout)
          throws Exception {
        RunMojo mojo = initMojo(pomFilePath);

        MockUtil.singleton.getProject().setArtifacts(new HashSet());
        MockUtil.singleton.getProject().getArtifacts().add(new ArtifactMock("common-1.jar"));

        DefaultJavaExecutor javaExecutor = new DefaultJavaExecutor() {
            public void setTimeout(Duration timeout) {
                super.setTimeout(new Duration(5));
            }


            public void execute(String mainClass, File[] classpathFiles, String arguments)
                  throws MojoExecutionException {
                super.execute(FakeMain.class.getName(),
                              new File[]{new File(ReflectUtil.determinePathFrom(FakeMain.class))},
                              "");
            }
        };
        mojo.setRunJavaExecutor(javaExecutor);

        try {
            mojo.execute();
            fail("An exception was expected");
        }
        catch (MojoExecutionException e) {
            StringBuffer expectedMessage = new StringBuffer();
            expectedMessage.append("The global timeout of release tests execution (");
            TimeUtil.printTo(expectedMessage, expectedTimeout).append(") has expired !!");
            assertEquals(expectedMessage.toString(), e.getMessage());
        }
    }


    private RunMojo initMojo(String pomFilePath) throws Exception {
        return (RunMojo)initMojo("run", pomFilePath);
    }


    private static class ArtifactMock extends ArtifactStub {
        private String pathname;


        private ArtifactMock(String path) {
            pathname = path;
        }


        public File getFile() {
            return new File(pathname);
        }


        public ArtifactHandler getArtifactHandler() {
            //noinspection InnerClassTooDeeplyNested
            return new DefaultArtifactHandler() {

                public boolean isAddedToClasspath() {
                    return true;
                }
            };
        }
    }

    public static class FakeMain {

        private static final Logger LOGGER = Logger.getLogger("FakeMain");


        public static void main(String[] args) {
            Logger.getLogger("FakeMain").info("BEGIN FakeMain.main");
            try {
                Thread.sleep(5000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                LOGGER.info("END FakeMain.main");
            }
        }
    }
}
