package net.codjo.maven.mojo.testrelease;
import net.codjo.util.file.FileUtil;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.versioning.VersionRange;
/**
 *
 */
public class StopServerMojoTest extends AbstractTestReleaseMojoTestCase {
    private static final File EXPECTED = new File(AdministrationShutdownerMock.SHUTDOWN_LOG_FILE);


    public void test_execute_local() throws Exception {
        EXPECTED.delete();

        StopServerMojo mojo = initMojo("stopServer/pom-default.xml");

        MockUtil singleton = MockUtil.singleton;
        singleton.getProject().setArtifacts(Collections.singleton(createTestArtifact()));

        mojo.execute();

        assertTrue(EXPECTED.exists());
        assertEquals("AdministrationShutdownerMock.main([localhost, 16969])", FileUtil.loadContent(EXPECTED));
    }


    public void test_execute_local_withFailure() throws Exception {
        EXPECTED.delete();
        EXPECTED.mkdir();

        StopServerMojo mojo = initMojo("stopServer/pom-default.xml");
        MockUtil singleton = MockUtil.singleton;
        singleton.getProject().setArtifacts(Collections.singleton(createTestArtifact()));

        mojo.execute();

        assertTrue(EXPECTED.exists());
        assertTrue(EXPECTED.isDirectory());
    }


    public void test_execute_remote() throws Exception {
        StopServerMojo mojo = initMojo("stopServer/pom-remote.xml");
        mockShellCommand(mojo);

        mojo.execute();

        log.assertContent("createShellCommand(my-login, dummyServer)"
                          + ", setLog(...)"
                          + ", execute(. ~/.profile;cd /unix/directory/SERVEUR;./server.sh stop)");
    }


    public void test_execute_remote_withFailure() throws Exception {
        StopServerMojo mojo = initMojo("stopServer/pom-remote.xml");

        mockShellCommandFailure(mojo, new IOException("failure"));

        mojo.execute();
    }


    public void test_execute_remoteWindows() throws Exception {
        StopServerMojo mojo = initMojo("stopServer/pom-windows-remote.xml");
        mockDosCommand(mojo);

        mojo.execute();

        log.assertContent("createDosCommand()"
                          + ", setLog(...)"
                          + ", execute(sc \\\\dummy-server stop DELRECO_INT)");
    }


    public void test_execute_remoteWindows_withFailure() throws Exception {
        StopServerMojo mojo = initMojo("stopServer/pom-windows-remote.xml");

        mockDosCommandFailure(mojo, new IOException("failure"));

        mojo.execute();
    }


    public void test_execute_withoutServer() throws Exception {
        StopServerMojo mojo = initMojo("stopServer/pom-withoutServer.xml");
        assertFalse(mojo.canExecuteGoal());
        mojo.execute();
    }


    private DefaultArtifact createTestArtifact() {
        DefaultArtifact artifact = new DefaultArtifact("gr", "id", VersionRange.createFromVersion("1.0"),
                                                       "runtime", "jar", "go", null);
        artifact.setFile(new File("target/test-classes"));
        return artifact;
    }


    private StopServerMojo initMojo(String pomFilePath) throws Exception {
        return (StopServerMojo)initMojo("stop-server", pomFilePath);
    }
}
