package net.codjo.maven.mojo.testrelease.windows;
import net.codjo.maven.mojo.testrelease.command.AbstractCommand;
import net.codjo.maven.mojo.testrelease.command.DeployerCommand;
import net.codjo.maven.mojo.util.AntUtil;
import java.io.File;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.Delete;
/**
 *
 */
class WindowsDeployerCommand extends AbstractCommand implements DeployerCommand {

    public void deploy(File zipFile, String applicationDirectory, String zipRootDirectory) throws Exception {
        info("  Nettoyage de l'ancien livrable : " + applicationDirectory);
        deleteContent(applicationDirectory, zipRootDirectory);

        info("  Unzip '" + zipFile + "' dans " + applicationDirectory);
        AntUtil.unzip(zipFile, new File(applicationDirectory));
    }


    private void deleteContent(String applicationDirectory, String zipRootDirectory) {
        Delete delete = new Delete();
        Project antProject = AntUtil.createAntProject(new Target(), "default");
        antProject.init();
        delete.setProject(antProject);
        delete.setTaskName("delete");
        delete.setFailOnError(false);
        delete.setDir(new File(applicationDirectory, zipRootDirectory));

        delete.execute();
    }
}
