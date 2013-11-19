package net.codjo.maven.mojo.testrelease;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.project.MavenProject;
/**
 *
 */
public class TestUtil {
    private TestUtil() {
    }


    public static void addDependencyManagement(String groupId,
                                               String artifactId,
                                               String version,
                                               String classifier,
                                               MavenProject project) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        dependency.setVersion(version);
        dependency.setClassifier(classifier);
        if (project.getDependencyManagement() == null) {
            project.getModel().setDependencyManagement(new DependencyManagement());
        }
        project.getDependencyManagement().addDependency(dependency);
    }


    public static Dependency addDependencyToProject(String groupId,
                                                    String artifactId,
                                                    String version,
                                                    MavenProjectMock project) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        dependency.setVersion(version);
        project.addPluginDependency(dependency);
        return dependency;
    }
}