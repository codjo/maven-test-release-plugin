package net.codjo.maven.mojo.testrelease;
import net.codjo.maven.mojo.util.DefaultJavaExecutor;
import net.codjo.reflect.collect.ReflectUtil;
import java.io.File;
import java.util.HashSet;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.stubs.ArtifactStub;
/**
 *
 */
public class RunMojoTest extends AbstractTestReleaseMojoTestCase {

    public void test_execute_local() throws Exception {
        RunMojo mojo = initMojo("run/pom-default.xml");
        mojo.setRunJavaExecutor(new JavaExecutorMock(log));

        MockUtil.singleton.getProject().setArtifacts(new HashSet());
        MockUtil.singleton.getProject().getArtifacts().add(new ArtifactMock("common-1.jar"));
        MockUtil.singleton.getProject().setFile(new File("./my-basedir/pom.xml"));

        mojo.execute();

        assertLog("setTimeout(" + RunMojo.TIMEOUT + ")"
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
        assertLog("setTimeout(" + RunMojo.TIMEOUT + ")"
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

        assertLog("setTimeout(" + RunMojo.TIMEOUT + ")"
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

        assertLog("setTimeout(" + RunMojo.TIMEOUT + ")"
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

        assertLog("setTimeout(" + RunMojo.TIMEOUT + ")"
                  + ", setWorkingDir(./my-basedir)"
                  + ", setJvmArg(-Xmx512m)"
                  + ", execute(net.codjo.test.release.ReleaseTestRunner, [common-1.jar], %target%/test-classes/mojos/run/usecase/firstTestRelease.xml)");
    }


    public void test_execute_expiredTimeoutLogsExplicitMessage() throws Exception {
        RunMojo mojo = initMojo("run/pom-default.xml");

        MockUtil.singleton.getProject().setArtifacts(new HashSet());
        MockUtil.singleton.getProject().getArtifacts().add(new ArtifactMock("common-1.jar"));

        DefaultJavaExecutor javaExecutor = new DefaultJavaExecutor() {
            public void setTimeout(long timeout) {
                super.setTimeout(5);
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
            fail();
        }
        catch (MojoExecutionException e) {
            assertEquals("Le timeout global d'execution des test-releases (4 heures) a expire !!",
                         e.getMessage());
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
        public static void main(String[] args) {
            try {
                Thread.sleep(5000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
