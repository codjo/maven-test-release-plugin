package net.codjo.maven.mojo.testrelease;
import java.io.File;
import net.codjo.maven.mojo.testrelease.command.OneShellCommand;
import net.codjo.maven.mojo.testrelease.unix.UnixCommandFactory;
import net.codjo.maven.mojo.testrelease.windows.WindowsCommandFactory;
import net.codjo.test.release.util.ssh.UnixSessionFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
/**
 *
 */
public abstract class AbstractTestReleaseMojo extends AbstractMojo {
    /**
     * server Host (local ou serveur unix en mode remote).
     *
     * @parameter expression="${serverHost}" default-value="localhost"
     * @noinspection UNUSED_SYMBOL
     */
    protected String serverHost;

    /**
     * Commutateur permettant de basculer en mode unix.
     *
     * @parameter expression="${remote}" default-value="false"
     * @noinspection UNUSED_SYMBOL, UnusedDeclaration
     */
    private boolean remote;

    /**
     * Commutateur permettant d'activer la mesure de couverture de code par les tests.
     *
     * @parameter expression="${coverage}" default-value="false"
     * @noinspection UNUSED_SYMBOL, UnusedDeclaration
     */
    private boolean coverage;

    /**
     * Package racine pour la couverture.
     *
     * @parameter expression="${package}" default-value="${project.groupId}"
     * @noinspection UNUSED_SYMBOL
     */
    protected String packageToInclude;

    /**
     * Packages à exclure de la couverture.
     *
     * @parameter expression="${packagesToExclude}"
     * @noinspection UNUSED_SYMBOL
     */
    protected String packagesToExclude;

    /**
     * Repertoire destination de la couverture de test pour la partie serveur.
     *
     * @parameter expression="${coverageServerOutputFile}" default-value="${project.basedir}/target/server.es"
     * @noinspection UNUSED_SYMBOL
     */
    protected String coverageServerOutputFile;

    /**
     * Repertoire destination de la couverture de test pour la partie cliente.
     *
     * @parameter expression="${coverageClientOutputFile}" default-value="${project.basedir}/target/client.es"
     * @noinspection UNUSED_SYMBOL
     */
    protected String coverageClientOutputFile;

    /**
     * Repertoire destination de la couverture de test.
     *
     * @parameter expression="${reportCoverageOutputDirectory}" default-value="${project.basedir}/target/emma-report"
     * @noinspection UNUSED_SYMBOL, UnusedDeclaration
     */
    protected File reportCoverageOutputDirectory;
    /**
     * Repertoire destination de la couverture de test-release.
     *
     * @parameter expression="${reportCoverageTrRelativePath}" default-value="emma-report-tr/index.html"
     * @noinspection UNUSED_SYMBOL, UnusedDeclaration
     */
    private String reportCoverageTrRelativePath;
    /**
     * Repertoire destination de la couverture de test-release + unit-test.
     *
     * @parameter expression="${reportCoverageRelativePath}" default-value="emma-report-all/index.html"
     * @noinspection UNUSED_SYMBOL, UnusedDeclaration
     */
    private String reportCoverageRelativePath;

    /**
     * Repertoire Unix racine de l'application. Par exemple pour mint le repertoire est
     * '/CODJOAM/DEV/DAF_WEB1/MINT/APP1' (i.e. la variable Unix '$MINT_APP').
     *
     * @parameter expression="${unixApplicationDirectory}"
     * @noinspection UNUSED_SYMBOL, UnusedDeclaration
     */
    private String unixApplicationDirectory;

    /**
     * Repertoire Windows racine de l'application. Par exemple pour delreco le repertoire est
     * '\\bi-delreco\delreco$\APP'.
     *
     * @parameter expression="${windowsApplicationDirectory}"
     * @noinspection UNUSED_SYMBOL, UnusedDeclaration
     */
    private String windowsApplicationDirectory;

    /**
     * Nom du service Windows de l'application.
     *
     * @parameter expression="${windowsServiceName}"
     * @noinspection UNUSED_SYMBOL, UnusedDeclaration
     */
    private String windowsServiceName;

    /**
     * Compte unix applicatif. Ce compte est utilisé pour le déploiement et l'execution de l'application.
     *
     * @parameter expression="${unixLogin}"
     * @noinspection UNUSED_SYMBOL, UnusedDeclaration
     */
    private String unixLogin;

    /**
     * Mot de passe unix applicatif.
     *
     * @parameter expression="${unixPassword}"
     * @noinspection UNUSED_SYMBOL, UnusedDeclaration
     */
    private String unixPassword;

    /**
     * port ssh pour se connecter au serveur.
     *
     * @parameter expression="${sshPort}" default-value="22"
     * @noinspection UNUSED_SYMBOL, UnusedDeclaration, FieldCanBeLocal
     */
    private int sshPort = 22;
    private UnixCommandFactory unixCommandFactory = new UnixCommandFactory();
    private WindowsCommandFactory windowsCommandFactory = new WindowsCommandFactory();


    public boolean isRemote() {
        return remote;
    }


    public boolean isCoverage() {
        return coverage;
    }


    public void setCoverage(boolean coverage) {
        this.coverage = coverage;
    }


    protected boolean isRemoteWindows() {
        return windowsServiceName != null;
    }


    public UnixCommandFactory getUnixCommandFactory() {
        return unixCommandFactory;
    }


    public void setUnixCommandFactory(UnixCommandFactory unixCommandFactory) {
        this.unixCommandFactory = unixCommandFactory;
    }


    public WindowsCommandFactory getWindowsCommandFactory() {
        return windowsCommandFactory;
    }


    public void setWindowsCommandFactory(WindowsCommandFactory windowsCommandFactory) {
        this.windowsCommandFactory = windowsCommandFactory;
    }


    public String getUnixApplicationDirectory() {
        return unixApplicationDirectory;
    }


    public String getWindowsApplicationDirectory() {
        return windowsApplicationDirectory;
    }


    protected UnixSessionFactory createSessionFactory() {
        getLog().info("Unix connection on " + serverHost + ":" + sshPort + " with " + unixLogin);
        return new UnixSessionFactory(unixLogin, serverHost, sshPort);
    }


    public final void execute() throws MojoExecutionException {
        if (!canExecuteGoal()) {
            return;
        }
        preExecute();
        if (isRemote()) {
            remoteExecute();
        }
        else {
            localExecute();
        }
    }


    protected boolean canExecuteGoal() {
        return true;
    }


    protected void preExecute() throws MojoExecutionException {
    }


    protected abstract void localExecute() throws MojoExecutionException;


    protected abstract void remoteExecute() throws MojoExecutionException;


    protected void executeRemoteServerScript(String argument, String label) throws MojoExecutionException {
        if (isRemoteWindows()) {
            if ("status".equals(argument)) {
                argument = "query";
            }
            executeShellCommand(label,
                                getWindowsCommandFactory().createShellCommand(),
                                "sc \\\\" + serverHost + " " + argument + " " + windowsServiceName);
        }
        else {
            executeShellCommand(label,
                                getUnixCommandFactory().createShellCommand(createSessionFactory()),
                                ". ~/.profile"
                                + ";cd " + getUnixApplicationDirectory() + "/" + getServerDir()
                                + ";./" + getScriptName() + " " + argument);
        }
    }


    private void executeShellCommand(String label, OneShellCommand command, String shellCommand)
          throws MojoExecutionException {
        command.setLog(getLog());
        try {
            command.execute(shellCommand);
        }
        catch (Exception cause) {
            throw new MojoExecutionException(label + " en erreur: \n" + cause.getLocalizedMessage(), cause);
        }
    }


    protected File getTrReportOutputDirectory() {
        return new File(reportCoverageOutputDirectory, reportCoverageTrRelativePath);
    }


    protected File getAllReportOutputDirectory() {
        return new File(reportCoverageOutputDirectory, reportCoverageRelativePath);
    }


    protected String getServerDir() {
        return "SERVEUR";
    }


    protected String getScriptName() {
        return "server.sh";
    }
}
