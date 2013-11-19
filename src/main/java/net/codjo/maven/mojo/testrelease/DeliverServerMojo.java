package net.codjo.maven.mojo.testrelease;
import net.codjo.maven.common.artifact.ArtifactDescriptor;
/**
 * Goal pour livrer un artifact SERVEUR sur l'environnement local ou remote.
 *
 * @goal deliver-server
 */
public class DeliverServerMojo extends AbstractDeliverMojo {
    /**
     * Livrable du serveur.
     *
     * @parameter
     * @noinspection UnusedDeclaration
     */
    private ArtifactDescriptor server;


    protected ArtifactDescriptor getDelivery() {
        return server;
    }
}
