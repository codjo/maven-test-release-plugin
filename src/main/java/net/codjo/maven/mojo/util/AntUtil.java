package net.codjo.maven.mojo.util;
import java.io.File;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Expand;
/**
 *
 */
public class AntUtil {
    private AntUtil() {
    }


    public static Project createAntProject(Target defaultTarget, String defaultTargetName) {
        Project project = new Project();
        defaultTarget.setName(defaultTargetName);
        defaultTarget.setProject(project);
        project.addTarget(defaultTarget);
        return project;
    }


    public static Task getUnzipTaskForArtifactClassifier(List buildPlugins,
                                                         ArtifactFactory artifactFactory,
                                                         ArtifactRepository localRepository,
                                                         String artifactModule,
                                                         String classifier, String unizipDestinationDirectory
    ) {
        for (java.util.Iterator it = buildPlugins.iterator(); it.hasNext();) {
            Plugin plugin = (Plugin)it.next();
            List pluginDeps = plugin.getDependencies();
            for (java.util.Iterator it1 = pluginDeps.iterator(); it1.hasNext();) {
                Dependency dependency = (Dependency)it1.next();
                if (dependency.getArtifactId().indexOf(artifactModule) != -1
                    && classifier.equals(dependency.getClassifier()) && "zip".equals(dependency.getType())) {

                    Artifact artifact =
                          artifactFactory.createArtifactWithClassifier(dependency.getGroupId(),
                                                                       dependency.getArtifactId(),
                                                                       dependency.getVersion(), "zip",
                                                                       classifier);

                    String artifactLocalPath =
                          localRepository.getBasedir() + File.separator
                          + localRepository.pathOf(artifact);

                    File zipFile = new File(artifactLocalPath);

                    Expand unzipTask = new Expand();
                    unzipTask.setSrc(zipFile);
                    unzipTask.setDest(new File(unizipDestinationDirectory));

                    return unzipTask;
                }
            }
        }
        return null;
    }


    public static void initAnt(Task task, boolean withLog) {
        Project ant = new Project();
        task.setProject(ant);

        if (withLog) {
            DefaultLogger listener = new DefaultLogger();
            //noinspection UseOfSystemOutOrSystemErr
            listener.setOutputPrintStream(System.out);
            //noinspection UseOfSystemOutOrSystemErr
            listener.setErrorPrintStream(System.err);
            listener.setMessageOutputLevel(Project.MSG_INFO);
            ant.addBuildListener(listener);
        }
        ant.init();
    }


    public static void unzip(File zipFile, File dest) {
        Project antProject = createAntProject(new Target(), "default");
        antProject.init();
        Expand unzipTask = new Expand();
        unzipTask.setTaskName("unzip");
        unzipTask.setSrc(zipFile);
        unzipTask.setDest(dest.getAbsoluteFile());
        unzipTask.setProject(antProject);
        unzipTask.execute();
    }
}
