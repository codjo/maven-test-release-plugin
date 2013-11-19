package net.codjo.maven.mojo.testrelease;
import net.codjo.maven.common.artifact.ArtifactDescriptor;
import java.io.File;
import org.apache.maven.plugin.MojoExecutionException;
/**
 * Goal pour démarrer un serveur Web sur l'environnement local ou remote.
 *
 * @goal start-web
 */
public class StartWebMojo extends AbstractStartMojo {

    /**
     * @parameter expression="${webConfigProperties}" default-value="target/WEB/web-config.properties"
     * @noinspection UnusedDeclaration
     */
    private File webConfigProperties;

    /**
     * @parameter expression="${webMainClass}"
     * @noinspection UnusedDeclaration
     */
    private String webMainClass;

    /**
     * @parameter expression="${releaseDirectory}" default-value="target/WEB" Repertoire ou le goal a placé le
     * livrable unzippé (WEB)
     * @noinspection UnusedDeclaration
     */
    private File releaseDirectory;

    /**
     * Spawn le process du serveur (mode local).
     *
     * @parameter expression="${spawn}" default-value="true"
     * @noinspection UnusedDeclaration
     */
    private boolean spawn = true;

    /**
     * Livrable du serveur web.
     *
     * @parameter
     * @noinspection UnusedDeclaration
     */
    private ArtifactDescriptor web;


    protected void localExecute() throws MojoExecutionException {
        getLog().info("Lancement de la classe '" + getMainClass() + "'");
        javaExecutor.setSpawnProcess(isSpawn());
        javaExecutor.execute(getMainClass(), getClasspath(),
                             "-configuration " + getConfigProperties().getAbsolutePath()
                             + " -testMode true");
    }


    protected File getConfigProperties() {
        return webConfigProperties;
    }


    protected String getMainClass() {
        return webMainClass;
    }


    protected File getReleaseDirectory() {
        return releaseDirectory;
    }


    protected boolean isSpawn() {
        return spawn;
    }


    protected ArtifactDescriptor getDelivery() {
        return web;
    }


    protected String getServerDir() {
        return "WEB";
    }


    protected String getScriptName() {
        return "web.sh";
    }
}
