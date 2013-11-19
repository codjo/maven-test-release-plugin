package net.codjo.maven.mojo.testrelease;
import net.codjo.test.common.FileComparator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
/**
 *
 */
public abstract class AbstractDeliverMojoTestCase extends AbstractTestReleaseMojoTestCase {
    private static final String TARGET_DIRECTORY = ".\\target\\";


    public void test_instanciateTemplateInZip() throws Exception {
        AbstractDeliverMojo mojo = getDeliveryMojo();
        String tmpDir = System.getProperty("java.io.tmpdir") + "/" + getClass().getSimpleName();
        mojo.setTmpDir(tmpDir);

        mojo.instanciateTemplateInZip(new File((getClass()
              .getResource("zipToInstanciate.zip")
              .getFile())));

        ZipFile expectedZipFile =
              new ZipFile(
                    getClass().getResource("zipToInstanciate.expected.zip").getFile());

        File actualFile = new File(tmpDir, "zipToInstanciate.zip");
        assertTrue(actualFile.exists());

        ZipFile actualZipFile = new ZipFile(actualFile);

        assertZipEquals(expectedZipFile, actualZipFile);
    }


    static void assertZipEquals(ZipFile expectedZipFile, ZipFile actualZipFile)
          throws IOException {

        Enumeration expectedEntries = expectedZipFile.getEntries();
        while (expectedEntries.hasMoreElements()) {
            ZipEntry expectedFirstEntry = (ZipEntry)expectedEntries.nextElement();
            ZipEntry expectedEntry = expectedZipFile
                  .getEntry(expectedFirstEntry.getName());
            ZipEntry actualEntry = actualZipFile.getEntry(expectedEntry.getName());
            if (actualEntry != null) {
                if (!(expectedEntry.isDirectory() && actualEntry.isDirectory())) {
                    InputStream expectedInputStream = expectedZipFile
                          .getInputStream(expectedEntry);
                    InputStream actualInputStream = actualZipFile
                          .getInputStream(actualEntry);

                    assertInputStreamEquals(expectedInputStream, actualInputStream);
                }
            }
            else {
                fail("missing zipEntry : " + expectedEntry.getName());
            }
        }
    }


    static void assertInputStreamEquals(InputStream expectedInputStream,
                                        InputStream actualInputStream) throws
                                                                       IOException {
        InputStreamReader expectedReader = new InputStreamReader(expectedInputStream);
        InputStreamReader actualReader = new InputStreamReader(actualInputStream);

        FileComparator fileComparator = new FileComparator("#");
        assertTrue(fileComparator.equals(expectedReader, actualReader));
    }


    protected void assertExists(String path) {
        assertTrue(new File(TARGET_DIRECTORY + path).exists());
    }


    protected AbstractDeliverMojo initMojo(String pomFilePath)
          throws Exception {
        MockUtil.setupEnvironment(pomFilePath);
        AbstractDeliverMojo mojo = (AbstractDeliverMojo)lookupMojo(getMojoGoal(), pomFilePath);
        MavenProjectMock projectMock = new MavenProjectMock();
        projectMock.getModel().addProperty("port", "15700");
        mojo.project = projectMock;
        return mojo;
    }


    protected void copyZipArtifact() {
        Copy copyTask = new Copy();
        File destDir = new File(getDestinationDir());
        copyTask.setTodir(destDir);
        File srcFile = new File(MockUtil.getInputFile(getSourceDir()));
        copyTask.setFile(srcFile);
        copyTask.setOverwrite(true);
        Project project = new Project();
        copyTask.setProject(project);
        project.init();

        copyTask.execute();
    }


    protected abstract String getSourceDir();


    protected abstract String getDestinationDir();


    protected abstract String getMojoGoal();


    protected abstract AbstractDeliverMojo getDeliveryMojo() throws Exception;
}
