package net.codjo.maven.mojo.testrelease;
import net.codjo.maven.common.artifact.ArtifactDescriptor;
import net.codjo.maven.mojo.util.DefaultJavaExecutor;
import net.codjo.maven.mojo.util.JavaExecutor;
import java.io.File;
import java.io.FileFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
/**
 *
 */
public abstract class AbstractStartMojo extends AbstractTestReleaseMojo {
    protected JavaExecutor javaExecutor = new DefaultJavaExecutor();

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     * @noinspection UNUSED_SYMBOL
     */
    protected MavenProject project;

    /**
     * @parameter expression="${localServerJvmArgs}"
     * @noinspection UnusedDeclaration
     */
    private String localServerJvmArgs;

    /**
     * Temps d'attente après le démarrage du serveur (Uniquement en local).
     *
     * @parameter expression="${sleepAfterLocalStart}" default-value="0"
     * @noinspection UnusedDeclaration
     */
    private int sleepAfterLocalStart;

    /**
     * Temps d'attente avant de verifer le statut du serveur (Uniquement en remote).
     *
     * @parameter expression="${waitTimeBeforeCheckStatus}" default-value="3"
     * @noinspection UnusedDeclaration
     */
    private int waitTimeBeforeCheckStatus = 3;


    public void setJavaExecutor(JavaExecutor javaExecutor) {
        this.javaExecutor = javaExecutor;
    }


    public File[] getClasspath() {
        return getReleaseDirectory().listFiles(new MyJarFilter());
    }


    public void setWaitTimeBeforeCheckStatus(int waitTimeBeforeCheckStatus) {
        this.waitTimeBeforeCheckStatus = waitTimeBeforeCheckStatus;
    }


    protected boolean canExecuteGoal() {
        return getDelivery() != null;
    }


    protected void localExecute() throws MojoExecutionException {
        JavaExecutor executor = javaExecutor;
        if (isCoverage()) {
            executor = new EmmaJavaExecutor(javaExecutor, packageToInclude,
                                            packagesToExclude, coverageServerOutputFile);
        }

        executor.setSpawnProcess(isSpawn());
        executor.setWorkingDir(getReleaseDirectory());
        String args = "-Dlog.dir=" + getReleaseDirectory().getPath();
        executor.setJvmArg(args + (localServerJvmArgs != null ? " " + localServerJvmArgs : ""));
        executor.execute(getMainClass(), getClasspath(),
                         "-configuration " + getConfigProperties().getAbsolutePath());

        if (sleepAfterLocalStart > 0) {
            waitSeconds(sleepAfterLocalStart);
        }
    }


    protected void remoteExecute() throws MojoExecutionException {
        executeRemoteServerScript("start", "Démarrage du serveur");
        displayServerStatus();
    }


    protected abstract File getConfigProperties();


    protected abstract String getMainClass();


    protected abstract File getReleaseDirectory();


    protected abstract boolean isSpawn();


    protected abstract ArtifactDescriptor getDelivery();


    private void displayServerStatus() {
        waitSeconds(waitTimeBeforeCheckStatus);

        try {
            executeRemoteServerScript("status", "Démarrage du serveur");
        }
        catch (MojoExecutionException e) {
            ;
        }
    }


    private void waitSeconds(int seconds) {
        getLog().info("Attente de " + seconds + " secondes que le serveur soit opérationnel !");
        try {
            Thread.sleep(seconds * 1000);
        }
        catch (InterruptedException e) {
            ;
        }
    }


    private class MyJarFilter implements FileFilter {

        public boolean accept(File pathname) {
            return pathname.isFile() && pathname.getName().indexOf(".jar") != -1;
        }
    }
}
