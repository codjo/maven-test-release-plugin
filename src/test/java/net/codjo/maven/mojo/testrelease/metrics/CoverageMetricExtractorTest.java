package net.codjo.maven.mojo.testrelease.metrics;
import net.codjo.util.file.FileUtil;
import java.io.IOException;
import junit.framework.TestCase;
/**
 *
 */
public class CoverageMetricExtractorTest extends TestCase {
    private CoverageMetricExtractor extractor = new CoverageMetricExtractor();


    public void test_extract_coveragePart() throws Exception {

        String content = CoverageBuilder.create()
              .methodCoverage("21%")
              .blockCoverage("12%")
              .lineCoverage("5%")
              .get();

        CoverageMetric result = extractor.extract(content);

        assertEquals("21", result.getMethodCoverage());
        assertEquals("12", result.getBlockCoverage());
        assertEquals("5", result.getLineCoverage());
    }


    public void test_extract_countPart() throws Exception {

        String content = CoverageBuilder.create()
              .packagesCount("1")
              .filesCount("2")
              .classesCount("3")
              .methodsCount("4")
              .linesCount("5")
              .get();

        CoverageMetric result = extractor.extract(content);

        assertEquals("1", result.getPackagesCount());
        assertEquals("2", result.getFilesCount());
        assertEquals("3", result.getClassesCount());
        assertEquals("4", result.getMethodsCount());
        assertEquals("5", result.getLinesCount());
    }


    private static class CoverageBuilder {
        private String content;


        private CoverageBuilder() throws IOException {
            content = FileUtil.loadContent(getClass().getResource("CoverageExtractorTest_sample.html"));
        }


        public static CoverageBuilder create() throws IOException {
            return new CoverageBuilder();
        }


        public CoverageBuilder methodCoverage(String value) {
            content = content.replaceFirst("%method_coverage%", value);
            return this;
        }


        public CoverageBuilder blockCoverage(String value) {
            content = content.replaceFirst("%block_coverage%", value);
            return this;
        }


        public CoverageBuilder lineCoverage(String value) {
            content = content.replaceFirst("%line_coverage%", value);
            return this;
        }


        public CoverageBuilder packagesCount(String value) {
            content = content.replaceFirst("%packages_count%", value);
            return this;
        }


        public CoverageBuilder filesCount(String value) {
            content = content.replaceFirst("%files_count%", value);
            return this;
        }


        public CoverageBuilder classesCount(String value) {
            content = content.replaceFirst("%classes_count%", value);
            return this;
        }


        public CoverageBuilder methodsCount(String value) {
            content = content.replaceFirst("%methods_count%", value);
            return this;
        }


        public CoverageBuilder linesCount(String value) {
            content = content.replaceFirst("%lines_count%", value);
            return this;
        }


        public String get() {
            return content;
        }
    }
}
