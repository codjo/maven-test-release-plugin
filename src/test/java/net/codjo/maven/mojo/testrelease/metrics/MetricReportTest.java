package net.codjo.maven.mojo.testrelease.metrics;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import junit.framework.TestCase;
import net.codjo.test.common.PathUtil;
import net.codjo.test.common.fixture.DirectoryFixture;
import net.codjo.util.file.FileUtil;
import org.junit.Assert;

public class MetricReportTest extends TestCase {
    private DirectoryFixture directoryFixture = DirectoryFixture.newTemporaryDirectoryFixture();


    protected void setUp() throws Exception {
        super.setUp();
        directoryFixture.doSetUp();
    }


    protected void tearDown() throws Exception {
        directoryFixture.doTearDown();
        super.tearDown();
    }


    public void test_writeCoverageReport_simulteanously() throws Exception {
        assertReport(createCoverageReportRunnable(),
                     createCoverageReportRunnable(),
                     "metrics-coverage_etalon.xls",
                     "metrics-coverage.xls");
    }


    public void test_writeBasicReport_simulteanously() throws Exception {
        assertReport(createBasicReportRunnable(),
                     createBasicReportRunnable(),
                     "metrics-basic_etalon.xls",
                     "metrics-basic.xls");
    }


    public void test_writeBasicReport_noRows() throws Exception {
        new MetricReport(Date.valueOf("2009-03-01"), directoryFixture,SvnDataExtractorTest.getSvnEntriesPath())
              .writeBasicReport(PathUtil.find(getClass(), "testreleaseSuite_noRows.log.xls"));
    }


    private void assertReport(Runnable runnable1, Runnable runnable2, String etalon, String generated)
          throws InterruptedException, ExecutionException, IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future future1 = executorService.submit(runnable1);
        Future future2 = executorService.submit(runnable2);

        future1.get();
        future2.get();

        File expectedFile = PathUtil.find(getClass(), etalon);
        File actualFile = new File(directoryFixture.getPath(), generated);

        assertContentOf(actualFile, FileUtil.loadContent(expectedFile));
    }


    private void assertContentOf(File file, String expectedContent) throws IOException {
        assertEquals(
              expectedContent
                    .replaceAll("<svn-revision>", SvnDataExtractorTest.currentSvnRevision())
                    .replaceAll("<svn-url>", SvnDataExtractorTest.currentSvnUrl())
              , FileUtil.loadContent(file));
    }


    private Runnable createCoverageReportRunnable() {
        return new Runnable() {
            public void run() {
                try {
                    new MetricReport(Date.valueOf("2009-03-01"),
                                     directoryFixture,
                                     SvnDataExtractorTest.getSvnEntriesPath())
                          .writeCoverageReport(PathUtil.find(getClass(), "emma-report-tr"));
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Assert.fail();
                }
            }
        };
    }


    private Runnable createBasicReportRunnable() {
        return new Runnable() {
            public void run() {
                try {
                    new MetricReport(Date.valueOf("2009-03-01"),
                                     directoryFixture,
                                     SvnDataExtractorTest.getSvnEntriesPath())
                          .writeBasicReport(PathUtil.find(getClass(), "testreleaseSuite.log.xls"));
                }
                catch (Exception e) {
                    Assert.fail();
                }
            }
        };
    }
}

