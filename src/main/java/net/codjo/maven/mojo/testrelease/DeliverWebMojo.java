package net.codjo.maven.mojo.testrelease;
import net.codjo.maven.common.artifact.ArtifactDescriptor;
/**
 * Goal pour livrer un artifact WEB sur l'environnement local ou remote.
 *
 * @goal deliver-web
 */
public class DeliverWebMojo extends AbstractDeliverMojo {
    /**
     * Livrable du serveur web.
     *
     * @parameter
     * @noinspection UnusedDeclaration
     */
    private ArtifactDescriptor web;


    protected ArtifactDescriptor getDelivery() {
        return web;
    }
}
