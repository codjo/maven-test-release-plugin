package net.codjo.maven.mojo.testrelease;
import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import net.codjo.maven.mojo.testrelease.metrics.MetricReport;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;
/**
 * Goal pour créer le rapport de la couverture de code en HTML.
 *
 * @goal deploy-metrics
 */
public class DeployMetricsMojo extends AbstractTestReleaseMojo {
    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     * @noinspection UNUSED_SYMBOL
     */
    protected MavenProject project;

    /**
     * Fichier construit par la librairie TR (testreleaseSuite.log.xls).
     *
     * @parameter expression="${testReleaseSuiteLogFile}" default-value="${project.basedir}/target/testreleaseSuite.log.xls"
     * @noinspection UNUSED_SYMBOL, UnusedDeclaration
     */
    private File testReleaseSuiteLogFile;

    /**
     * Repertoire destination des métrics sur Z:.
     *
     * @parameter expression="${metricsTargetDirectory}" default-value="Z:/equipes/kaizen-tr/metrics/"
     * @noinspection UNUSED_SYMBOL, UnusedDeclaration
     */
    private File metricsTargetDirectory;
    /**
     * ArtifactId du projet de test-release (utilisé pour déterminer le répertoire de stockage des metrics).
     *
     * @parameter expression="${projectArtifactId}" default-value="${project.artifactId}"
     * @noinspection UNUSED_SYMBOL, UnusedDeclaration
     */
    private String artifactId;
    /**
     * Profondeur de l'historique stockée.
     *
     * @parameter expression="${historicDepth}" default-value="30"
     * @noinspection UNUSED_SYMBOL, UnusedDeclaration
     */
    private int historicDepth = 30;

    private String svnEntriesPath=".svn/entries";


    protected void localExecute() throws MojoExecutionException {
        zipMetrics(getTargetZipFile());

        purgeHistoric();

        fillSyntheticReport();
    }


    protected void remoteExecute() throws MojoExecutionException {
        setCoverage(false);
        localExecute();
    }


    private void zipMetrics(File targetZipFile) {
        targetZipFile.getParentFile().mkdirs();
        targetZipFile.delete();

        Zip zipTask = new Zip();
        zipTask.setDestFile(targetZipFile);

        zipTask.addFileset(createFileSet(testReleaseSuiteLogFile.getParentFile(),
                                         testReleaseSuiteLogFile.getName()));

        if (isCoverage()) {
            zipTask.addFileset(createFileSet(reportCoverageOutputDirectory.getParentFile(),
                                             reportCoverageOutputDirectory.getName() + "/**"));
        }

        Project antProject = new Project();
        zipTask.setProject(antProject);
        antProject.init();
        zipTask.execute();
    }


    private void purgeHistoric() {
        File projectMetricDirectory = getProjectMetricDirectory();
        String[] historics = projectMetricDirectory.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith("metrics-");
            }
        });
        Arrays.sort(historics);

        if (historics.length > historicDepth) {
            getLog().info("Nettoyage de l'historique des metriques...");
            for (int i = 0; i < historics.length - historicDepth; i++) {
                String historic = historics[i];
                getLog().info("suppression de : " + historic);
                new File(projectMetricDirectory, historic).delete();
            }
        }
    }


    private void fillSyntheticReport() {
        getLog().info("Generation des rapports synthetique...");
        MetricReport report = new MetricReport(getTimestamp(testReleaseSuiteLogFile),
                                               metricsTargetDirectory,
                                               svnEntriesPath);
        try {
            getLog().info("\t->basic...");
            report.writeBasicReport(testReleaseSuiteLogFile);
            if (isCoverage()) {
                getLog().info("\t->coverage...");
                report.writeCoverageReport(getTrReportOutputDirectory().getParentFile());
            }
        }
        catch (Exception e) {
            getLog().warn("Generation des rapports synthetique en erreur", e);
        }
    }


    private File getProjectMetricDirectory() {
        return new File(metricsTargetDirectory, artifactId);
    }


    private FileSet createFileSet(File dir, String includes) {
        FileSet fileSet = new FileSet();
        fileSet.setDir(dir);
        fileSet.setIncludes(includes);
        return fileSet;
    }


    private File getTargetZipFile() {
        String name = "metrics-"
                      + getTimestamp(testReleaseSuiteLogFile, "yyyyMMddHHmm")
                      + (isCoverage() ? "-full.zip" : ".zip");
        return new File(getProjectMetricDirectory(), name);
    }


    public File getTestReleaseSuiteLogFile() {
        return testReleaseSuiteLogFile;
    }


    private String getTimestamp(File file, String pattern) {
        return new SimpleDateFormat(pattern).format(getTimestamp(file));
    }


    private Date getTimestamp(File file) {
        long timestamp = file.lastModified();
        return new Date(timestamp);
    }


    public void setHistoricDepth(int historicDepth) {
        this.historicDepth = historicDepth;
    }


    public void setSvnEntriesPath(String svnEntriesPath) {
        this.svnEntriesPath = svnEntriesPath;
    }
}
