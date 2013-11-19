package net.codjo.maven.mojo.util;
import net.codjo.maven.common.artifact.ArtifactDescriptor;
import java.io.File;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
/**
 * TODO : ne pas toucher car provient de maven-database-plugin (en attente de refactoring)
 */
public class ArtifactGetter {
    private final ArtifactFactory artifactFactory;
    private final ArtifactRepository localRepository;
    private final WagonManager wagonManager;
    private final List remoteArtifactRepositories;


    public ArtifactGetter(ArtifactFactory artifactFactory,
                          ArtifactRepository localRepository,
                          List remoteArtifactRepositories,
                          WagonManager wagonManager) {
        this.artifactFactory = artifactFactory;
        this.localRepository = localRepository;
        this.wagonManager = wagonManager;
        this.remoteArtifactRepositories = remoteArtifactRepositories;
    }


    public Artifact getArtifact(ArtifactDescriptor include) throws TransferFailedException,
                                                                   ResourceDoesNotExistException {

        Artifact artifact = createArtifact(include);

        String artifactLocalPath = localRepository.getBasedir()
                                   + File.separator
                                   + localRepository.pathOf(artifact);
        artifact.setFile(new File(artifactLocalPath));

        if (!artifact.getFile().exists() && remoteArtifactRepositories != null) {
            wagonManager.getArtifact(artifact, remoteArtifactRepositories);
        }
        return artifact;
    }


    protected ArtifactFactory getArtifactFactory() {
        return artifactFactory;
    }


    protected Artifact createArtifact(ArtifactDescriptor include) {
        if (include.getClassifier() == null) {
            return getArtifactFactory().createArtifact(include.getGroupId(),
                                                       include.getArtifactId(),
                                                       include.getVersion(),
                                                       Artifact.SCOPE_COMPILE,
                                                       include.getType());
        }
        else {
            return getArtifactFactory().createArtifactWithClassifier(include.getGroupId(),
                                                                     include.getArtifactId(),
                                                                     include.getVersion(),
                                                                     include.getType(),
                                                                     include.getClassifier());
        }
    }
}
