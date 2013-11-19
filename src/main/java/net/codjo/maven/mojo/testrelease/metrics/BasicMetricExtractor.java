package net.codjo.maven.mojo.testrelease.metrics;
/**
 *
 */
class BasicMetricExtractor {
    static final String HEADER = "Test\tTotal before\tUsed before\tFree before\t"
                                 + "Total after\tUsed after\tFree after\tTime\t";


    public BasicMetric extract(String testReleaseLogFileContent) {
        if (!testReleaseLogFileContent.startsWith(HEADER)) {
            throw new IllegalArgumentException("En-tête incorrecte");
        }
        String[] rows = testReleaseLogFileContent.split("\n");

        if (rows.length < 2) {
            return null;
        }

        BasicMetric metric = new BasicMetric();
        long elapse = 0;
        for (int i = 1; i < rows.length; i++) {
            String[] columns = rows[i].split("\t");
            metric.setUsedMemory(columns[4]);
            elapse += Long.parseLong(columns[7]);
        }
        metric.setTimeElapsed(elapse);
        metric.setTestCount(rows.length - 1);
        return metric;
    }
}
