/*
 * Team : CODJO AM / OSI / SI / BO
 *
 * Copyright (c) 2001 CODJO Asset Management.
 */
package net.codjo.maven.mojo.testrelease;
import net.codjo.maven.common.test.DirectoryFixture;
import net.codjo.maven.mojo.testrelease.command.DeployerCommand;
import java.io.File;
import java.io.IOException;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
/**
 *
 */
public class DeliverTestResourcesMojoTest extends AbstractTestReleaseMojoTestCase {

    DirectoryFixture fixture = DirectoryFixture.newTemporaryDirectoryFixture();


    protected void setUp() throws Exception {
        super.setUp();
        fixture.doSetUp();
    }


    protected void tearDown() throws Exception {
        super.tearDown();
        fixture.doTearDown();
    }


    public void test_deliverInputData_local() throws Exception {
        Mojo mojo = getDeliveryMojo("deliverInputData/pom-default.xml");
        mojo.execute();

        assertExists("destination/dir/aFileToDeploy.txt");
        assertExists("aDirectoryToDeploy\\firstFile.txt");
        assertExists("aDirectoryToDeploy\\secondFile.txt");
    }


    private void assertExists(String s) {
        assertTrue(new File("target", s).exists());
    }


    public void test_deliverInputData_remote() throws Exception {
        DeliverTestResourcesMojo mojo = getDeliveryMojo("deliverInputData/pom-remote.xml");
        mockShellCommand(mojo);

        mojo.execute();

        log.assertContent("createDeployer(my-login, dummyServer), "
                          + "setLog(...), "
                          + "deploy(aFileToDeploy.txt.zip, target/destination/dir, "
                          + DeployerCommand.NO_DIRECTORY + "), "
                          + "setLog(...), "
                          + "deploy(aDirectoryToDeploy.zip, /unix/directory, aDirectoryToDeploy)");
    }


    public void test_deliverInputData_remoteFailure() throws Exception {
        DeliverTestResourcesMojo mojo = getDeliveryMojo("deliverInputData/pom-remote.xml");

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


    public void test_deliverInputData_remoteWindows() throws Exception {
        DeliverTestResourcesMojo mojo = getDeliveryMojo("deliverInputData/pom-windows-remote.xml");
        mockDosCommand(mojo);

        mojo.execute();

        log.assertContent("createDeployer(), "
                          + "setLog(...), "
                          + "deploy(aFileToDeploy.txt.zip, target/destination/dir, "
                          + DeployerCommand.NO_DIRECTORY + "), "
                          + "setLog(...), "
                          + "deploy(aDirectoryToDeploy.zip, \\\\dummy-server\\delreco$\\APP, aDirectoryToDeploy)");
    }


    protected DeliverTestResourcesMojo getDeliveryMojo(String pomFilePath) throws Exception {
        return (DeliverTestResourcesMojo)initMojo("deliver-test-resources", pomFilePath);
    }
}