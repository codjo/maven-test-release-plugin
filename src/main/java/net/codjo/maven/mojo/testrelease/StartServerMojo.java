package net.codjo.maven.mojo.testrelease;
import net.codjo.maven.common.artifact.ArtifactDescriptor;
import java.io.File;
/**
 * Goal pour démarrer un serveur Agent sur l'environnement local ou remote.
 *
 * @goal start-server
 */
public class StartServerMojo extends AbstractStartMojo {
    /**
     * @parameter expression="${serverConfigProperties}" default-value="target/SERVEUR/server-config.properties"
     * @required
     * @noinspection UnusedDeclaration
     */
    private File serverConfigProperties;
    /**
     * @parameter expression="${serverMainClass}"
     * @noinspection UnusedDeclaration
     */
    private String serverMainClass;
    /**
     * @parameter expression="${releaseDirectory}" default-value="target/SERVEUR" Repertoire ou le goal a
     * placé le livrable unzippé (SERVEUR)
     * @required
     * @noinspection UnusedDeclaration
     */
    private File releaseDirectory;
    /**
     * Spawn le process du serveur (mode local).
     *
     * @parameter expression="${spawn}" default-value="true"
     * @required
     * @noinspection UnusedDeclaration
     */
    private boolean spawn = true;
    /**
     * Livrable du serveur.
     *
     * @parameter
     * @noinspection UnusedDeclaration
     */
    private ArtifactDescriptor server;


    protected File getConfigProperties() {
        return serverConfigProperties;
    }


    protected String getMainClass() {
        return serverMainClass;
    }


    protected File getReleaseDirectory() {
        return releaseDirectory;
    }


    protected boolean isSpawn() {
        return spawn;
    }


    protected ArtifactDescriptor getDelivery() {
        return server;
    }
}
