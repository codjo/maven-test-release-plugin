package net.codjo.maven.mojo.testrelease.metrics;
import junit.framework.TestCase;
/**
 *
 */
public class BasicMetricExtractorTest extends TestCase {
    private BasicMetricExtractor extractor = new BasicMetricExtractor();


    public void test_extract_badHeader() throws Exception {
        try {
            extractor.extract("badHeader");
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("En-tête incorrecte", ex.getMessage());
        }
    }


    public void test_extract_noTestRunned() throws Exception {
        String logContent = ReleaseLogBuilder.create()
              .header()
              .get();

        BasicMetric result = extractor.extract(logContent);

        assertNull(result);
    }


    public void test_extract_oneTest() throws Exception {
        String logContent = ReleaseLogBuilder.create()
              .header()
              .test("test1", "12.5", "200")
              .get();

        BasicMetric result = extractor.extract(logContent);

        assertMetric(1, "12.5", 200, result);
    }


    public void test_extract_twoTest() throws Exception {
        String logContent = ReleaseLogBuilder.create()
              .header()
              .test("test1", "12.5", "200")
              .test("test1", "22.5", "300")
              .get();

        BasicMetric result = extractor.extract(logContent);

        assertMetric(2, "22.5", 500, result);
    }


    private void assertMetric(int testCount,
                              String expectedMemory,
                              int expectedElapsedTime,
                              BasicMetric result) {
        assertNotNull(result);
        assertEquals(expectedMemory, result.getUsedMemory());
        assertEquals(expectedElapsedTime, result.getTimeElapsed());
        assertEquals(testCount, result.getTestCount());
    }


    private static class ReleaseLogBuilder {
        private StringBuilder content = new StringBuilder();


        public static ReleaseLogBuilder create() {
            return new ReleaseLogBuilder();
        }


        public ReleaseLogBuilder header() {
            content.append(BasicMetricExtractor.HEADER).append("\r\n");
            return this;
        }


        public ReleaseLogBuilder test(String testName, String memoryTotalAfter, String time) {
            content.append(testName)
                  .append("\t0.0\t0.0\t0.0\t")
                  .append(memoryTotalAfter)
                  .append("\t0.0\t0.0\t")
                  .append(time)
                  .append("\t\r\n");
            return this;
        }


        public String get() {
            return content.toString();
        }
    }
}
