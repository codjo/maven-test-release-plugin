package net.codjo.maven.mojo.testrelease;
import net.codjo.maven.mojo.testrelease.command.DeployerCommand;
import net.codjo.maven.mojo.testrelease.command.DeployerCommandMock;
import net.codjo.maven.mojo.testrelease.unix.UnixCommandFactory;
import net.codjo.maven.mojo.testrelease.unix.UnixSessionFactory;
import net.codjo.test.common.LogString;
import java.io.File;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
/**
 *
 */
public class DeliverBatchMojoTest extends AbstractMojoTestCase {
    private static final String TARGET_DIRECTORY = ".\\target\\";
    private LogString log = new LogString();


    public void test_deliverBatch_unconfigured() throws Exception {
        DeliverBatchMojo mojo = initMojo("deliverBatch/pom-unconfigured.xml");
        mojo.execute();
    }


    public void test_deliverBatch_local() throws Exception {
        DeliverBatchMojo mojo = initMojo("deliverBatch/pom-default.xml");

        copyZipArtifactSql();

        TestUtil.addDependencyManagement("mint",
                                         "mint-batch",
                                         "1.0-SNAPSHOT",
                                         "delreco",
                                         MockUtil.singleton.getProject());

        MockUtil.singleton.getArtifactRepository()
              .setUrl(MockUtil.toUrl("target/test-classes/mojos/deliverBatch"));

        mojo.execute();

        assertExists("BATCH\\mint-batch-1.0-SNAPSHOT.jar");
    }


    public void test_deliverBatch_remote() throws Exception {
        DeliverBatchMojo mojo = initMojo("deliverBatch/pom-remote.xml");

        mockUnixDeployer(mojo);

        copyZipArtifactSql();

        TestUtil.addDependencyManagement("mint",
                                         "mint-batch",
                                         "1.0-SNAPSHOT",
                                         "delreco",
                                         MockUtil.singleton.getProject());

        MockUtil.singleton.getArtifactRepository()
              .setUrl(MockUtil.toUrl("target/test-classes/mojos/deliverBatch"));

        mojo.execute();

        log.assertContent("createDeployer(my-login, guadeloupe)"
                          + ", setLog(...)"
                          + ", deploy(mint-batch-1.0-SNAPSHOT-delreco.zip, /unix/directory, BATCH)");
    }


    private void mockUnixDeployer(DeliverBatchMojo mojo) {
        mojo.setUnixCommandFactory(new UnixCommandFactory() {

            public DeployerCommand createDeployer(UnixSessionFactory unixSessionFactory) {
                log.call("createDeployer",
                         unixSessionFactory.getLogin(),
                         unixSessionFactory.getHost());
                return new DeployerCommandMock(log);
            }
        });
    }


    private void assertExists(String path) {
        assertTrue(new File(DeliverBatchMojoTest.TARGET_DIRECTORY + path).exists());
    }


    private DeliverBatchMojo initMojo(String pomFilePath)
          throws Exception {
        MockUtil.setupEnvironment(pomFilePath);
        return (DeliverBatchMojo)lookupMojo("deliver-batch", pomFilePath);
    }


    private void copyZipArtifactSql() {
        Copy copyTask = new Copy();
        File destDir =
              new File("target/test-classes/mojos/deliverBatch/mint/mint-batch/1.0-SNAPSHOT");
        copyTask.setTodir(destDir);
        File srcFile =
              new File(MockUtil.getInputFile(
                    "deliverBatch/mint/mint-batch/1.0-SNAPSHOT/mint-batch-1.0-SNAPSHOT-delreco.zip"));
        copyTask.setFile(srcFile);
        copyTask.setOverwrite(true);
        Project project = new Project();
        copyTask.setProject(project);
        project.init();

        copyTask.execute();
    }


    protected Mojo lookupMojo(String goal, String pomFile)
          throws Exception {
        try {
            return super.lookupMojo(goal, getPomFile(pomFile));
        }
        catch (Exception e) {
            fail("lookup en echec : " + e.getLocalizedMessage());
        }
        return null;
    }


    protected File getPomFile(String path) {
        return getTestFile("target/test-classes/mojos/" + path);
    }
}
