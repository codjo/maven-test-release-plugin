package net.codjo.maven.mojo.testrelease.metrics;
/**
 *
 */
class CoverageMetricExtractor {
    public CoverageMetric extract(String content) {
        CoverageMetric metric = new CoverageMetric();

        HtmlReport report = new HtmlReport(content);

        HtmlTable coverageTable = report.table("OVERALL COVERAGE SUMMARY");
        metric.setMethodCoverage(extractPercentage(coverageTable.cell(2, 3)));
        metric.setBlockCoverage(extractPercentage(coverageTable.cell(2, 4)));
        metric.setLineCoverage(extractPercentage(coverageTable.cell(2, 5)));

        HtmlTable summaryTable = report.table("OVERALL STATS SUMMARY");
        metric.setPackagesCount(summaryTable.cell(1, 2));
        metric.setFilesCount(summaryTable.cell(2, 2));
        metric.setClassesCount(summaryTable.cell(3, 2));
        metric.setMethodsCount(summaryTable.cell(4, 2));
        metric.setLinesCount(summaryTable.cell(5, 2));

        return metric;
    }


    private String extractPercentage(String cellContent) {
        return cellContent.substring(0, cellContent.indexOf('%'));
    }


    private static class HtmlReport {
        private String content;


        private HtmlReport(String content) {
            this.content = content;
        }


        public HtmlTable table(String title) {
            int titleIndex = content.indexOf(title);
            int start = content.indexOf("<TABLE", titleIndex);
            int end = content.indexOf("</TABLE", start);
            return new HtmlTable(content.substring(start, end));
        }
    }
    private static class HtmlTable {
        private String content;


        private HtmlTable(String content) {
            this.content = content;
        }


        public String cell(int row, int column) {
            int rowIndex = findIndex("<TR", 0, row);
            int columnIndex = findIndex("<TD", rowIndex, column);

            return content.substring(content.indexOf(">", columnIndex) + 1,
                                     content.indexOf("</TD", columnIndex));
        }


        private int findIndex(String pattern, int fromIndex, int line) {
            int nextLineIndex = content.indexOf(pattern, fromIndex);
            if (line == 1) {
                return nextLineIndex;
            }
            return findIndex(pattern, nextLineIndex + 1, --line);
        }
    }
}
