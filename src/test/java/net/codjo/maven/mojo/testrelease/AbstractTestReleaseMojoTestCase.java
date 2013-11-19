package net.codjo.maven.mojo.testrelease;
import com.intellij.rt.execution.emma.RunnerAgent;
import com.vladium.emma.EMMAProperties;
import java.io.File;
import java.io.IOException;
import net.codjo.maven.mojo.testrelease.command.DeployerCommand;
import net.codjo.maven.mojo.testrelease.command.DeployerCommandMock;
import net.codjo.maven.mojo.testrelease.command.OneShellCommand;
import net.codjo.maven.mojo.testrelease.command.OneShellCommandMock;
import net.codjo.maven.mojo.testrelease.unix.UnixCommandFactory;
import net.codjo.maven.mojo.testrelease.unix.UnixSessionFactory;
import net.codjo.maven.mojo.testrelease.windows.WindowsCommandFactory;
import net.codjo.reflect.collect.ClassCollector;
import net.codjo.reflect.collect.ReflectUtil;
import net.codjo.test.common.LogString;
import net.codjo.test.common.PathUtil;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
/**
 *
 */
abstract class AbstractTestReleaseMojoTestCase extends AbstractMojoTestCase {
    protected LogString log = new LogString();


    protected void mockShellCommandFailure(AbstractTestReleaseMojo mojo, final IOException failure) {
        mojo.setUnixCommandFactory(new UnixCommandFactory() {

            public OneShellCommand createShellCommand(UnixSessionFactory unixSessionFactory) {
                return new ShellCommandFailure(failure);
            }


            public DeployerCommand createDeployer(UnixSessionFactory unixSessionFactory) {
                return new DeployerCommandFailure(failure);
            }
        });
    }


    protected void mockShellCommand(AbstractTestReleaseMojo mojo) {
        mojo.setUnixCommandFactory(new UnixCommandFactory() {

            public OneShellCommand createShellCommand(UnixSessionFactory unixSessionFactory) {
                log.call("createShellCommand",
                         unixSessionFactory.getLogin(),
                         unixSessionFactory.getHost());
                return new OneShellCommandMock(log);
            }


            public DeployerCommand createDeployer(UnixSessionFactory unixSessionFactory) {
                log.call("createDeployer",
                         unixSessionFactory.getLogin(),
                         unixSessionFactory.getHost());
                return new DeployerCommandMock(log);
            }
        });
    }


    protected void mockDosCommand(AbstractTestReleaseMojo mojo) {
        mojo.setWindowsCommandFactory(new WindowsCommandFactory() {

            public OneShellCommand createShellCommand() {
                log.call("createDosCommand");
                return new OneShellCommandMock(log);
            }


            public DeployerCommand createDeployer() {
                log.call("createDeployer");
                return new DeployerCommandMock(log);
            }
        });
    }


    protected void mockDosCommandFailure(AbstractTestReleaseMojo mojo, final IOException failure) {
        mojo.setWindowsCommandFactory(new WindowsCommandFactory() {

            public OneShellCommand createShellCommand() {
                return new ShellCommandFailure(failure);
            }
        });
    }


    protected Mojo lookupMojo(String goal, String pomFile) throws Exception {
        try {
            return lookupMojo(goal, getPomFile(pomFile));
        }
        catch (Exception e) {
            fail("lookup en echec : " + e.getLocalizedMessage());
        }
        return null;
    }


    protected File getPomFile(String path) {
        return getTestFile("target/test-classes/mojos/" + path);
    }


    protected AbstractTestReleaseMojo initMojo(String goal, String pomFilePath) throws Exception {
        MockUtil.setupEnvironment(pomFilePath);
        return (AbstractTestReleaseMojo)lookupMojo(goal, pomFilePath);
    }


    protected void assertLog(String expected) {
        expected = expected
              .replaceAll("%target%", toUnixStylePath(PathUtil.findTargetDirectory(getClass())))
              .replaceAll("%mojo-dir%",
                          toUnixStylePath(PathUtil.findTestResourcesDirectory(getClass()))
                          + "-filtered/mojos")
              .replaceAll("%emmaAgentJar%", toUnixStylePath(getEmmaAgentJar()))
              .replaceAll("%emmaClassPath%", getEmmaClasspath())
              .replaceAll("%reflectClassPath%", getReflectClasspath())
              .replaceAll("\\\\", "/");
        assertEquals(expected, log.getContent().replaceAll("\\\\", "/"));
    }


    private String toUnixStylePath(File path) {
        return path.getAbsolutePath().replaceAll("\\\\", "/");
    }


    private String getEmmaClasspath() {
        String emmaJarName = new File(ReflectUtil.determinePathFrom(EMMAProperties.class)).getName();
        String emmaAgentJarName = getEmmaAgentJar().getName();
        return emmaJarName + ", " + emmaAgentJarName;
    }


    protected String getReflectClasspath() {
        return new File(ReflectUtil.determinePathFrom(ClassCollector.class)).getName();
    }


    private File getEmmaAgentJar() {
        return new File(ReflectUtil.determinePathFrom(RunnerAgent.class));
    }


    private static class ShellCommandFailure extends OneShellCommandMock {
        private final IOException failure;


        ShellCommandFailure(IOException failure) {
            this.failure = failure;
        }


        public void execute(String shellCommand) throws IOException {
            throw failure;
        }
    }
    private static class DeployerCommandFailure extends DeployerCommandMock {
        private final IOException failure;


        DeployerCommandFailure(IOException failure) {
            this.failure = failure;
        }


        public void deploy(File zipFile, String applicationDirectory, String zipRootDirectory) throws IOException {
            throw failure;
        }
    }
}
