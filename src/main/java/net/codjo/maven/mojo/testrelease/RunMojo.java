package net.codjo.maven.mojo.testrelease;
import java.io.File;
import net.codjo.maven.common.artifact.ArtifactDescriptor;
import net.codjo.maven.mojo.util.DefaultJavaExecutor;
import net.codjo.maven.mojo.util.JavaExecutor;
import net.codjo.maven.mojo.util.TimeUtil;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.joda.time.Duration;
import org.joda.time.Hours;
/**
 * Goal pour démarrer les test release.
 *
 * @goal run
 * @requiresDependencyResolution test
 */
public class RunMojo extends AbstractTestReleaseMojo {

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     * @noinspection UNUSED_SYMBOL
     */
    protected MavenProject project;

    /**
     * @parameter expression="${releaseTestRunnerClass}" default-value="net.codjo.test.release.ReleaseTestRunner"
     * @noinspection UNUSED_SYMBOL
     */
    protected String releaseTestRunnerClass = "net.codjo.test.release.ReleaseTestRunner";

    /**
     * @parameter expression="${releaseTestDirectory}" default-value="src/main/usecase"
     * @required
     * @noinspection UNUSED_SYMBOL, UnusedDeclaration
     */
    private File releaseTestDirectory;

    /**
     * @parameter expression="${test}"
     * @noinspection UNUSED_SYMBOL, UnusedDeclaration
     */
    private File test;

    /**
     * Pour le shutdown WEB en local.
     *
     * @parameter expression="${shutdownWebMainClass}" default-value="net.codjo.plugin.server.AdministrationWebShutdowner"
     * @noinspection UNUSED_SYMBOL
     */
    protected String shutdownWebMainClass;

    /**
     * Pour le shutdown en local.
     *
     * @parameter expression="${shutdownMainClass}" default-value="net.codjo.plugin.server.AdministrationShutdowner"
     * @noinspection UNUSED_SYMBOL
     */
    protected String shutdownMainClass;

    /**
     * Pour le shutdown en local.
     *
     * @parameter expression="${serverPort}" default-value="15700"
     * @noinspection UNUSED_SYMBOL
     */
    protected int serverPort;
    /**
     * Livrable du serveur.
     *
     * @parameter
     * @noinspection UnusedDeclaration
     */
    private ArtifactDescriptor server;

    /**
     * Livrable du serveur WEB.
     *
     * @parameter
     * @noinspection UnusedDeclaration
     */
    private ArtifactDescriptor web;

    /**
     * @parameter expression="${webHost}"
     * @noinspection UNUSED_SYMBOL
     */
    protected String webHost;
    /**
     * @parameter expression="${webPort}"
     * @noinspection UNUSED_SYMBOL
     */
    protected int webPort;

    /**
     * @parameter expression="${applicationName}"
     * @noinspection UNUSED_SYMBOL
     */
    protected String applicationName;

    /**
     * Arguments JVM pour l'execution des tests.
     *
     * @parameter expression="${jvmArgs}" default-value="-Xmx512m"
     * @noinspection UNUSED_SYMBOL
     */
    protected String jvmArgs;

    /**
     * @parameter expression="${timeout}"
     * @noinspection UNUSED_SYMBOL
     */
    protected Duration timeout = DEFAULT_TIMEOUT;

    private JavaExecutor runJavaExecutor = new DefaultJavaExecutor();
    private JavaExecutor shutdownJavaExecutor = new DefaultJavaExecutor();
    static final Duration DEFAULT_TIMEOUT = Hours.FOUR.toStandardDuration();


    public void setRunJavaExecutor(JavaExecutor runJavaExecutor) {
        this.runJavaExecutor = runJavaExecutor;
    }


    public void setTimeout(String timeout) {
        this.timeout = TimeUtil.parseDuration(timeout);
    }


    public void setShutdownJavaExecutor(JavaExecutor shutdownJavaExecutor) {
        this.shutdownJavaExecutor = shutdownJavaExecutor;
    }


    protected void preExecute() throws MojoExecutionException {
        super.preExecute();
        manageCustomTestDirectory();
    }


    private void manageCustomTestDirectory() throws MojoExecutionException {
        if (test != null) {
            if (test.exists()) {
                releaseTestDirectory = test;
            }
            else {
                throw new MojoExecutionException("Le répertoire ou fichier '"
                                                 + test.getAbsolutePath()
                                                 + "' de TestRelease n'existe pas.");
            }
        }
    }


    protected void localExecute() throws MojoExecutionException {
        try {
            executeAllTest(isCoverage(), jvmArgs);
        }
        catch (MojoExecutionException e) {
            // Si l'utilisateur demande un test explicitement, on ne kille pas le serveur
            if (test == null && server != null) {
                AbstractStopMojo.executeLocalServerShutdown(shutdownMainClass, serverHost, serverPort,
                                                            shutdownJavaExecutor, project, getLog());
            }
            if (test == null && web != null) {
                AbstractStopMojo.executeLocalWebServerShutdown(shutdownWebMainClass, webHost, webPort,
                                                               applicationName,
                                                               shutdownJavaExecutor, project, getLog());
            }
            throw e;
        }
    }


    protected void remoteExecute() throws MojoExecutionException {
        executeAllTest(false, jvmArgs + " -Dagf.test.remote=yes");
    }


    private void executeAllTest(boolean isCoverage, String jvmArg) throws MojoExecutionException {
        getLog().info("");
        getLog().info("Execution des test contenus dans " + releaseTestDirectory.getAbsolutePath());

        if (isCoverage) {
            runJavaExecutor = new EmmaJavaExecutor(runJavaExecutor,
                                                   packageToInclude,
                                                   packagesToExclude,
                                                   coverageClientOutputFile);
        }

        shutdownJavaExecutor.setWorkingDir(project.getBasedir());
        shutdownJavaExecutor.setJvmArg(jvmArg);

        StringBuffer message = new StringBuffer();
        message.append("The global timeout of release tests execution is ");
        TimeUtil.printTo(message, timeout).append('.');
        getLog().info(message.toString());
        runJavaExecutor.setTimeout(timeout);
        runJavaExecutor.setWorkingDir(project.getBasedir());
        runJavaExecutor.setJvmArg(jvmArg);
        try {
            runJavaExecutor.execute(releaseTestRunnerClass,
                                    AbstractStopMojo.createClassPath(project.getTestArtifacts()),
                                    releaseTestDirectory.getAbsolutePath());
        }
        catch (MojoExecutionException e) {
            Throwable throwable = e.getCause();
            if ((throwable != null) &&
                (throwable.getMessage() != null) &&
                (throwable.getMessage().startsWith("Timeout:"))) {
                throw new MojoExecutionException(computeTimeoutMessage(), e);
            }
            throw e;
        }
        getLog().info("Bravo !");
    }


    private String computeTimeoutMessage() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("The global timeout of release tests execution (");
        TimeUtil.printTo(buffer, timeout).append(") has expired !!");
        return buffer.toString();
    }
}
