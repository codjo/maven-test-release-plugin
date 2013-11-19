package net.codjo.maven.mojo.testrelease;
import net.codjo.maven.common.artifact.ArtifactDescriptor;
/**
 * Goal pour arreter un serveur Agent sur l'environnement local ou remote.
 *
 * @goal stop-server
 * @requiresDependencyResolution
 */
public class StopServerMojo extends AbstractStopMojo {

    /**
     * @parameter expression="${shutdownMainClass}" default-value="net.codjo.plugin.server.AdministrationShutdowner"
     * @noinspection UNUSED_SYMBOL
     */
    protected String shutdownMainClass;
    /**
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


    protected String getShutdownMainClass() {
        return shutdownMainClass;
    }


    protected String getServerHost() {
        return serverHost;
    }


    protected int getServerPort() {
        return serverPort;
    }


    protected ArtifactDescriptor getDelivery() {
        return server;
    }
}
