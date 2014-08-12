package net.codjo.maven.mojo.testrelease;
import java.io.File;
import java.util.Collection;
import net.codjo.maven.common.artifact.ArtifactDescriptor;
import net.codjo.maven.mojo.util.DefaultJavaExecutor;
import net.codjo.maven.mojo.util.JavaExecutor;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.joda.time.Minutes;
/**
 *
 */
public abstract class AbstractStopMojo extends AbstractTestReleaseMojo {
    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     * @noinspection UNUSED_SYMBOL
     */
    protected MavenProject project;


    protected boolean canExecuteGoal() {
        return getDelivery() != null;
    }


    protected void localExecute() throws MojoExecutionException {
        executeLocalServerShutdown(getShutdownMainClass(),
                                   getServerHost(),
                                   getServerPort(),
                                   new DefaultJavaExecutor(),
                                   project,
                                   getLog());
    }


    protected void remoteExecute() throws MojoExecutionException {
        try {
            executeRemoteServerScript("stop", "Arret du serveur");
        }
        catch (MojoExecutionException e) {
            getLog().debug("Erreur lors de l'arret du serveur. Le serveur est probablement deja arrete");
            getLog().debug(e);
        }
    }


    static void executeLocalServerShutdown(String shutdownMainClass,
                                           String serverHost,
                                           int serverPort,
                                           JavaExecutor javaExecutor,
                                           MavenProject project,
                                           Log log) {
        log.info("shutdown " + serverHost + ":" + serverPort);
        executeLocalServerShutdown(shutdownMainClass,
                                   serverHost + " " + serverPort,
                                   javaExecutor,
                                   project,
                                   log);
    }


    static void executeLocalWebServerShutdown(String shutdownMainClass,
                                              String serverHost,
                                              int serverPort,
                                              String appicationName,
                                              JavaExecutor javaExecutor,
                                              MavenProject project,
                                              Log log) {
        log.info("shutdown " + appicationName + " " + serverHost + ":" + serverPort);
        executeLocalServerShutdown(shutdownMainClass,
                                   serverHost + " " + serverPort + " " + appicationName,
                                   javaExecutor,
                                   project,
                                   log);
    }


    static private void executeLocalServerShutdown(String shutdownMainClass,
                                                   String parameters,
                                                   JavaExecutor javaExecutor,
                                                   MavenProject project,
                                                   Log log) {
        File[] classPath = createClassPath(project.getArtifacts());

        javaExecutor.setTimeout(Minutes.ONE.toStandardDuration());
        javaExecutor.setDisplayProcessOutput(true);

        boolean shutdownComplete = false;
        int nbRetry = 0;
        while (!shutdownComplete && nbRetry < 3) {
            nbRetry++;
            try {
                log.info("try " + nbRetry + "...");
                javaExecutor.execute(shutdownMainClass, classPath, parameters);
                shutdownComplete = true;
                log.info("exit-status: 0");
            }
            catch (Exception e) {
                log.info("exit-status: -1", e);
            }
        }
    }


    public static File[] createClassPath(Collection artifacts) {
        File[] classpathFiles = new File[artifacts.size()];
        int index = 0;
        for (java.util.Iterator it = artifacts.iterator(); it.hasNext(); ) {
            classpathFiles[index++] = ((Artifact)it.next()).getFile();
        }
        return classpathFiles;
    }


    protected abstract String getShutdownMainClass();


    protected abstract String getServerHost();


    protected abstract int getServerPort();


    protected abstract ArtifactDescriptor getDelivery();
}
