package net.codjo.maven.mojo.testrelease;
import net.codjo.maven.mojo.util.DefaultJavaExecutor;
import net.codjo.maven.mojo.util.JavaExecutor;
import java.io.File;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
/**
 * Goal pour créer le rapport de la couverture de code en HTML.
 *
 * @goal coverage-report
 */
public class CoverageReportMojo extends AbstractTestReleaseMojo {
    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     * @noinspection UNUSED_SYMBOL
     */
    protected MavenProject project;

    private JavaExecutor javaExecutor = new DefaultJavaExecutor();
    private int waitTime = 3000;


    public void setJavaExecutor(JavaExecutor javaExecutor) {
        this.javaExecutor = javaExecutor;
    }


    protected boolean canExecuteGoal() {
        return isCoverage();
    }


    protected void localExecute() throws MojoExecutionException {
        getLog().info("Generation du rapport de couverture de code...");

        // TODO: attention, on patiente quelques secondes pour être sûr que le serveur est bien stoppé
        // et que le fichier de couverture du serveur a été correctement généré. On peut faire mieux.
        try {
            Thread.sleep(waitTime);
        }
        catch (InterruptedException e) {
            getLog().error("Erreur durant le sleep...", e);
        }

        javaExecutor.setWorkingDir(project.getFile().getParentFile());
        generateTrReport();
        generateAllTestsReport();
    }


    private void generateTrReport() throws MojoExecutionException {
        javaExecutor.execute("emma",
                             EmmaJavaExecutor.toEmmaClasspath(new File[0]),
                             "report -r html "
                             + "-in " + coverageClientOutputFile
                             + addCoverageServerOutputFile()
                             + getSourcePath()
                             + " -Dreport.html.out.file=" + getTrReportOutputDirectory());
    }


    private void generateAllTestsReport() throws MojoExecutionException {
        String allJUnitTests = addAllJUnitTests();
        if (allJUnitTests.length() > 0) {
            javaExecutor.execute("emma",
                                 EmmaJavaExecutor.toEmmaClasspath(new File[0]),
                                 "report -r html "
                                 + "-in " + coverageClientOutputFile
                                 + addCoverageServerOutputFile()
                                 + allJUnitTests
                                 + getSourcePath()
                                 + " -Dreport.html.out.file=" + getAllReportOutputDirectory());
        }
    }


    private String addCoverageServerOutputFile() {
        if (new File(coverageServerOutputFile).exists()) {
            return " -in " + coverageServerOutputFile;
        }
        return "";
    }


    private String addAllJUnitTests() {
        MavenProject parent = project.getParent();
        File parentFile = parent.getFile().getParentFile();
        List modules = parent.getModules();

        StringBuilder jUnitTests = new StringBuilder();
        for (int i = 0; i < modules.size(); i++) {
            String module = (String)modules.get(i);
            File report = new File(parentFile, module + "/target/tu.es");
            if (report.exists()) {
                jUnitTests.append(" -in ").append(report.getAbsolutePath());
            }
        }

        return jUnitTests.toString();
    }


    protected void remoteExecute() throws MojoExecutionException {
        getLog().info("Pas de generation de rapport de couverture de code par les tests.");
    }


    private String getSourcePath() {
        MavenProject parent = project.getParent();
        File parentFile = parent.getFile().getParentFile();
        List modules = parent.getModules();

        StringBuilder sourcePath = new StringBuilder();
        for (int i = 0; i < modules.size(); i++) {
            String module = (String)modules.get(i);
            File source = new File(parentFile, module + "/src/main/java");
            if (source.exists()) {
                sourcePath.append(" -sp ").append(source.getAbsolutePath());
            }
        }

        return sourcePath.toString();
    }


    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }
}
