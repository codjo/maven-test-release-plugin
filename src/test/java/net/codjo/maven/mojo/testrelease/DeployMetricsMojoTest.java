package net.codjo.maven.mojo.testrelease;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import net.codjo.maven.mojo.testrelease.metrics.SvnDataExtractorTest;
import net.codjo.test.common.fixture.CompositeFixture;
import net.codjo.test.common.fixture.DirectoryFixture;
import net.codjo.util.file.FileUtil;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;
/**
 *
 */
public class DeployMetricsMojoTest extends AbstractTestReleaseMojoTestCase {
    private DirectoryFixture expandedZipDirectory = new DirectoryFixture("expandedZipDir");
    private DirectoryFixture metricsDirectory = new DirectoryFixture("target/kaizen-tr");
    private CompositeFixture fixture = new CompositeFixture(expandedZipDirectory, metricsDirectory);
    private DeployMetricsMojo mojo;


    public void test_generateReport() throws Exception {
        mojo = initMojo("deployMetrics/pom-default.xml");

        mojo.execute();

        assertContentOf(metricZip("myapp-release-test/metrics-<timestamp>-full.zip"), new String[]{
              "emma-report/emma-report-tr/index.html",
              "emma-report/emma-report-all/index.html",
              "testreleaseSuite.log.xls"
        });
    }


    public void test_generateReport_noCoverage() throws Exception {
        mojo = initMojo("deployMetrics/pom-default.xml");
        mojo.setCoverage(false);

        mojo.execute();

        assertContentOf(metricZip("myapp-release-test/metrics-<timestamp>.zip"), new String[]{
              "testreleaseSuite.log.xls"
        });
    }


    public void test_generateReport_cleanUpOldMetrics() throws Exception {
        File projectMetricsDirectory = new File(metricsDirectory, "myapp-release-test");

        mojo = initMojo("deployMetrics/pom-default.xml");
        mojo.setCoverage(false);
        mojo.setHistoricDepth(2);

        // Creation d'un historique
        projectMetricsDirectory.mkdirs();
        new File(projectMetricsDirectory, "metrics-200800000000-full.zip").createNewFile();
        new File(projectMetricsDirectory, "metrics-200700000000.zip").createNewFile();
        new File(projectMetricsDirectory, "untouched.zip").createNewFile();

        mojo.execute();

        assertContentOf(metricZip("myapp-release-test/metrics-<timestamp>.zip"), new String[]{
              "testreleaseSuite.log.xls"
        });

        assertContent(listDirectoryContent(projectMetricsDirectory), new String[]{
              metricZip("metrics-<timestamp>.zip").getName(),
              "metrics-200800000000-full.zip",
              "untouched.zip",
        });
    }


    public void test_generateSyntheticReport_basic() throws Exception {
        mojo = initMojo("deployMetrics/pom-default.xml");

        mojo.execute();

        assertContentOf(file("metrics-basic.xls"),
                        "where\trevision\twhen\tNb\ttime\tMemory (total after)\n"
                        + "<svn-url>\t<svn-revision>\t<human-readable-timestamp>\t2\t22468\t17.73828125\n");
    }


    public void test_generateSyntheticReport_coverage() throws Exception {
        mojo = initMojo("deployMetrics/pom-default.xml");
        mojo.setCoverage(true);

        mojo.execute();

        assertContentOf(file("metrics-coverage.xls"),
                        "where\trevision\twhen\tmethod coverage\tblock coverage\tline coverage\tpackages\texecutable files\tclasses\tmethods\tlines\n"
                        + "<svn-url>\t<svn-revision>\t<human-readable-timestamp>\t64\t69\t68\t110\t1407\t4130\t13049\t40738\n");
    }


    public void test_generateSyntheticReport_append() throws Exception {
        mojo = initMojo("deployMetrics/pom-default.xml");
        mojo.setCoverage(false);

        FileUtil.saveContent(file("metrics-basic.xls"), "before\n");

        mojo.execute();

        assertContentOf(file("metrics-basic.xls"),
                        "before\n"
                        + "<svn-url>\t<svn-revision>\t<human-readable-timestamp>\t2\t22468\t17.73828125\n");
    }


    private File metricZip(String name) {
        return file(name.replaceAll("<timestamp>", getTimestamp("yyyyMMddHHmm")));
    }


    private String getTimestamp(String pattern) {
        long time = mojo.getTestReleaseSuiteLogFile().lastModified();
        return new SimpleDateFormat(pattern).format(new Date(time));
    }


    private File file(String fileName) {
        return new File("target/kaizen-tr/", fileName);
    }


    private DeployMetricsMojo initMojo(String pomFilePath) throws Exception {
        DeployMetricsMojo deployMetricsMojo = (DeployMetricsMojo)initMojo("deploy-metrics", pomFilePath);
        deployMetricsMojo.setSvnEntriesPath(SvnDataExtractorTest.getSvnEntriesPath());
        return deployMetricsMojo;
    }


    private void assertContentOf(File file, String expectedContent) throws IOException {
        assertEquals(
              expectedContent
                    .replaceAll("<svn-revision>", SvnDataExtractorTest.currentSvnRevision())
                    .replaceAll("<svn-url>", SvnDataExtractorTest.currentSvnUrl())
                    .replaceAll("<human-readable-timestamp>", getTimestamp("dd/MM/yyyy HH:mm:ss"))
              , FileUtil.loadContent(file));
    }


    protected void assertContentOf(File zipFile, String[] expectedFiles) throws IOException {
        expand(zipFile);
        String[] actualFiles = listDirectoryContent(expandedZipDirectory);
        assertContent(actualFiles, expectedFiles);
    }


    private void assertContent(String[] actualFiles, String[] expectedFiles) {
        Arrays.sort(expectedFiles);
        Arrays.sort(actualFiles);
        assertEquals(toString(expectedFiles), toString(actualFiles));
    }


    private String[] listDirectoryContent(File directory) {
        FileScanner scanner = new DirectoryScanner();
        scanner.setBasedir(directory);
        scanner.scan();
        return scanner.getIncludedFiles();
    }


    private String toString(String[] files) {
        StringBuffer expected = new StringBuffer();
        for (int i = 0; i < files.length; i++) {
            String file = files[i];
            if (expected.length() != 0) {
                expected.append("\n");
            }
            expected.append(file.replaceAll("\\[.*]", "").replaceAll("\\\\", "/").trim());
        }
        return expected.toString();
    }


    protected File expand(File zipFile) {
        assertFileExists(zipFile);
        Expand expand = new Expand();
        expand.setProject(new Project());
        expand.setSrc(zipFile);
        expand.setDest(expandedZipDirectory);
        expand.execute();
        return expandedZipDirectory;
    }


    private void assertFileExists(File file) {
        assertTrue(toLabel(file) + " should exists", file.exists());
    }


    private String toLabel(File file) {
        return "'" + file.getParentFile().getName() + "/" + file.getName() + "'";
    }


    public void setUp() throws Exception {
        super.setUp();
        fixture.doSetUp();
    }


    public void tearDown() throws Exception {
        super.tearDown();
        fixture.doTearDown();
    }
}
