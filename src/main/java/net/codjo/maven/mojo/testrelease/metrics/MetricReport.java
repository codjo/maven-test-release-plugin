package net.codjo.maven.mojo.testrelease.metrics;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.codehaus.plexus.util.FileUtils;

public class MetricReport {
    private Date reportDate;
    private File metricsTargetDirectory;
    private String svnEntriePath;


    public MetricReport(Date reportDate, File metricsTargetDirectory) {
        this(reportDate, metricsTargetDirectory, ".svn/entries");
    }


    public MetricReport(Date reportDate, File metricsTargetDirectory, String svnEntriePath) {
        this.reportDate = reportDate;
        this.metricsTargetDirectory = metricsTargetDirectory;
        this.svnEntriePath = svnEntriePath;
    }


    public void writeCoverageReport(File emmaOutputDirectory) throws IOException {
        create(new CoverageReport(emmaOutputDirectory));
    }


    public void writeBasicReport(File testReleaseSuiteLogFile) throws IOException {
        create(new BasicReport(testReleaseSuiteLogFile));
    }


    private void create(Report report) throws IOException {
        File metricsFile = new File(metricsTargetDirectory, report.getMetricFileName());
        FileChannel fileChannel = new FileOutputStream(metricsFile, true).getChannel();
        try {
            fileChannel.lock();
            if (fileChannel.size() == 0) {
                fileChannel.write(ByteBuffer.wrap(report.getHeader().getBytes()));
                fileChannel.write(ByteBuffer.wrap("\n".getBytes()));
            }
            report.writeReport(fileChannel,
                               new SvnDataExtractor().extract(FileUtils.fileRead(new File(svnEntriePath))));
        }
        finally {
            fileChannel.close();
        }
    }


    private static String toHumanReadableDate(Date date) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(date);
    }


    private interface Report {
        public String getMetricFileName();


        public String getHeader();


        public void writeReport(FileChannel fileChannel, SvnData data) throws IOException;
    }
    private class BasicReport implements Report {
        private File testReleaseSuiteLogFile;


        private BasicReport(File testReleaseSuiteLogFile) {
            this.testReleaseSuiteLogFile = testReleaseSuiteLogFile;
        }


        public String getMetricFileName() {
            return "metrics-basic.xls";
        }


        public String getHeader() {
            return "where\trevision\twhen\tNb\ttime\tMemory (total after)";
        }


        public void writeReport(FileChannel fileChannel, SvnData svn) throws IOException {
            BasicMetric metric
                  = new BasicMetricExtractor().extract(FileUtils.fileRead(testReleaseSuiteLogFile));
            if (metric != null) {
                StringBuffer stringBuffer = new StringBuffer(svn.getUrl()).append("\t")
                      .append(svn.getRevision())
                      .append("\t")
                      .append(toHumanReadableDate(reportDate))
                      .append("\t")
                      .append(Integer.toString(metric.getTestCount()))
                      .append("\t")
                      .append(Long.toString(metric.getTimeElapsed()))
                      .append("\t")
                      .append(metric.getUsedMemory())
                      .append("\n");
                fileChannel.write(ByteBuffer.wrap(stringBuffer.toString().getBytes()), fileChannel.size());
            }
        }
    }

    private class CoverageReport implements Report {
        private File emmaOutputDirectory;


        private CoverageReport(File emmaOutputDirectory) {
            this.emmaOutputDirectory = emmaOutputDirectory;
        }


        public String getMetricFileName() {
            return "metrics-coverage.xls";
        }


        public String getHeader() {
            return "where\trevision\twhen"
                   + "\tmethod coverage\tblock coverage\tline coverage"
                   + "\tpackages\texecutable files\tclasses\tmethods\tlines";
        }


        public void writeReport(FileChannel fileChannel, SvnData svn) throws IOException {
            String htmlIndexContent = FileUtils.fileRead(new File(emmaOutputDirectory, "index.html"));
            CoverageMetric metric = new CoverageMetricExtractor().extract(htmlIndexContent);

            StringBuffer stringBuffer = new StringBuffer(svn.getUrl())
                  .append("\t")
                  .append(svn.getRevision())
                  .append("\t")
                  .append(toHumanReadableDate(reportDate))
                  .append("\t")
                  .append(metric.getMethodCoverage())
                  .append("\t")
                  .append(metric.getBlockCoverage())
                  .append("\t")
                  .append(metric.getLineCoverage())
                  .append("\t")
                  .append(metric.getPackagesCount())
                  .append("\t")
                  .append(metric.getFilesCount())
                  .append("\t")
                  .append(metric.getClassesCount())
                  .append("\t")
                  .append(metric.getMethodsCount())
                  .append("\t")
                  .append(metric.getLinesCount())
                  .append("\n");
            fileChannel.write(ByteBuffer.wrap(stringBuffer.toString().getBytes()), fileChannel.size());
        }
    }
}
