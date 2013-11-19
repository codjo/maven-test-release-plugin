package net.codjo.maven.mojo.testrelease;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
/**
 *
 */
public class DeliverWebMojoTest extends AbstractDeliverMojoTestCase {

    public void test_deliverServer_local() throws Exception {
        Mojo mojo = initMojo("deliverWeb/pom-default.xml");

        copyZipArtifact();

        TestUtil.addDependencyManagement("icomp",
                                         "icomp-web",
                                         "1.0-SNAPSHOT",
                                         "delreco",
                                         MockUtil.singleton.getProject());

        MockUtil.singleton.getArtifactRepository()
              .setUrl(MockUtil.toUrl("target/test-classes/mojos/deliverWeb"));

        mojo.execute();

        assertExists("WEB\\icomp-web-1.00.00.00-a-SNAPSHOT.jar");
        assertExists("WEB\\jetty-util-6.1.3.jar");
        assertExists("WEB\\web.sh");
        assertExists("WEB\\web-config.properties");
    }


    public void test_deliverWeb_local_Exception() throws Exception {
        Mojo mojo = initMojo("deliverWeb/pom-error.xml");
        TestUtil.addDependencyManagement("icomp",
                                         "icomp-web",
                                         "1.0-SNAPSHOT",
                                         "INEXISTANT",
                                         MockUtil.singleton.getProject());
        TestUtil.addDependencyManagement("icomp",
                                         "icomp-web",
                                         "1.0-SNAPSHOT",
                                         "delreco",
                                         MockUtil.singleton.getProject());

        MockUtil.singleton.getArtifactRepository()
              .setUrl(MockUtil.toUrl("target/test-classes/mojos/deliverWeb"));

        try {
            mojo.execute();
            fail("Exception attendue.");
        }
        catch (MojoExecutionException exception) {
            assertEquals(
                  "Deploiement sur release-test impossible pour le module : 'icomp-web' car il n'existe pas ou n'a pas ete genere.",
                  exception.getLocalizedMessage());
        }
    }


    protected String getSourceDir() {
        return "deliverWeb/icomp/icomp-web/1.0-SNAPSHOT/icomp-web-1.0-SNAPSHOT-delreco.zip";
    }


    protected String getDestinationDir() {
        return "target/test-classes/mojos/deliverWeb/icomp/icomp-web/1.0-SNAPSHOT";
    }


    protected String getMojoGoal() {
        return "deliver-web";
    }


    protected AbstractDeliverMojo getDeliveryMojo() throws Exception {
        return initMojo("deliverWeb/pom-default.xml");
    }
}
