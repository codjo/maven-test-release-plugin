/*
 * Team : CODJO AM / OSI / SI / BO
 *
 * Copyright (c) 2001 CODJO Asset Management.
 */
package net.codjo.maven.mojo.testrelease;
import java.io.IOException;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
/**
 *
 */
public class DeliverServerMojoTest extends AbstractDeliverMojoTestCase {

    public void test_deliverServer_local() throws Exception {
        Mojo mojo = deliverySetUp("deliverServer/pom-default.xml");

        mojo.execute();

        assertExists("SERVEUR\\codjo-agent-bootstrap-0.1-SNAPSHOT.jar");
        assertExists("SERVEUR\\codjo-aspect-1.03.jar");
        assertExists("SERVEUR\\codjo-broadcast-common-5.06.jar");
        assertExists("SERVEUR\\my-config.properties");
    }


    public void test_deliverServer_local_Exception() throws Exception {
        Mojo mojo = deliverySetUp("deliverServer/pom-error.xml");
        TestUtil.addDependencyManagement("mint",
                                         "mint-server",
                                         "1.0-SNAPSHOT",
                                         "INEXISTANT",
                                         MockUtil.singleton.getProject());

        try {
            mojo.execute();
            fail("Exception attendue.");
        }
        catch (MojoExecutionException exception) {
            assertEquals("Deploiement sur release-test impossible pour le module : 'mint-server' "
                         + "car il n'existe pas ou n'a pas ete genere.",
                         exception.getLocalizedMessage());
        }
    }


    public void test_deliverServer_remote() throws Exception {
        AbstractDeliverMojo mojo = deliverySetUp("deliverServer/pom-remote.xml");
        mockShellCommand(mojo);

        mojo.execute();

        log.assertContent("createDeployer(my-login, dummyServer)"
                          + ", setLog(...)"
                          + ", deploy(mint-server-1.0-SNAPSHOT-delreco.zip, /unix/directory, SERVEUR)");
    }


    public void test_deliverServer_remoteFailure() throws Exception {
        AbstractDeliverMojo mojo = deliverySetUp("deliverServer/pom-remote.xml");

        IOException expectedFailure = new IOException("failed");
        mockShellCommandFailure(mojo, expectedFailure);

        try {
            mojo.execute();
            fail();
        }
        catch (MojoExecutionException ex) {
            assertSame(expectedFailure, ex.getCause());
        }
    }


    public void test_deliverServer_remoteWindows() throws Exception {
        AbstractDeliverMojo mojo = deliverySetUp("deliverServer/pom-windows-remote.xml");
        mockDosCommand(mojo);

        mojo.execute();

        log.assertContent("createDeployer()"
                          + ", setLog(...)"
                          + ", deploy(mint-server-1.0-SNAPSHOT-delreco.zip, \\\\dummy-server\\delreco$\\APP, SERVEUR)");
    }


    private AbstractDeliverMojo deliverySetUp(String pomFilePath) throws Exception {
        AbstractDeliverMojo mojo = initMojo(pomFilePath);

        copyZipArtifact();

        TestUtil.addDependencyManagement("mint",
                                         "mint-server",
                                         "1.0-SNAPSHOT",
                                         "delreco",
                                         MockUtil.singleton.getProject());

        MockUtil.singleton
              .getArtifactRepository()
              .setUrl(MockUtil.toUrl("target/test-classes/mojos/deliverServer"));
        return mojo;
    }


    protected String getSourceDir() {
        return "deliverServer/mint/mint-server/1.0-SNAPSHOT/mint-server-1.0-SNAPSHOT-delreco.zip";
    }


    protected String getDestinationDir() {
        return "target/test-classes/mojos/deliverServer/mint/mint-server/1.0-SNAPSHOT";
    }


    protected String getMojoGoal() {
        return "deliver-server";
    }


    protected AbstractDeliverMojo getDeliveryMojo() throws Exception {
        return initMojo("deliverServer/pom-default.xml");
    }
}
