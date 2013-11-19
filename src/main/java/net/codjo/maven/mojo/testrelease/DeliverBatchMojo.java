package net.codjo.maven.mojo.testrelease;
import net.codjo.maven.common.artifact.ArtifactDescriptor;
/**
 * Goal pour livrer un artifact BATCH sur l'environnement local ou remote.
 *
 * @goal deliver-batch
 */
public class DeliverBatchMojo extends AbstractDeliverMojo {
    /**
     * Livrable du batch.
     *
     * @parameter
     * @noinspection UnusedDeclaration
     */
    private ArtifactDescriptor batch;


    protected ArtifactDescriptor getDelivery() {
        return batch;
    }
}
