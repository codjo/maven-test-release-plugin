package net.codjo.maven.mojo.testrelease;
import net.codjo.maven.common.ant.AntUtil;
import net.codjo.maven.mojo.testrelease.command.DeployerCommand;
import net.codjo.util.file.FileUtil;
import java.io.File;
import java.io.FilenameFilter;
import org.apache.maven.plugin.MojoExecutionException;
/**
 * @goal deliver-test-resources
 */
public class DeliverTestResourcesMojo extends AbstractTestReleaseMojo {
    private String tmpDir = System.getProperty("java.io.tmpdir") + "/testReleaseDeploy";

    /**
     * Données utilisés par le serveur.
     *
     * @parameter
     * @noinspection UnusedDeclaration
     */
    private Include[] includeTestResources;


    protected boolean canExecuteGoal() {
        return includeTestResources != null && includeTestResources.length != 0;
    }


    protected void localExecute() throws MojoExecutionException {
        try {
            for (int i = 0; i < includeTestResources.length; i++) {
                Include resource = includeTestResources[i];
                resource.resolveOutput(getWindowsApplicationDirectory());
                localExecuteImpl(resource);
            }
        }
        catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }


    protected void remoteExecute() throws MojoExecutionException {
        try {
            DeployerCommand deployerCommand;
            String applicationDirectory;

            if (isRemoteWindows()) {
                deployerCommand = getWindowsCommandFactory().createDeployer();
                applicationDirectory = getWindowsApplicationDirectory();
            }
            else {
                deployerCommand = getUnixCommandFactory().createDeployer(createSessionFactory());
                applicationDirectory = getUnixApplicationDirectory();
            }

            for (int i = 0; i < includeTestResources.length; i++) {
                Include resource = includeTestResources[i];
                resource.resolveOutput(applicationDirectory);
                remoteExecuteImpl(resource, deployerCommand);
            }
        }
        catch (Exception exception) {
            throw new MojoExecutionException(exception.getLocalizedMessage(), exception);
        }
    }


    private void localExecuteImpl(Include resource) {
        logDelivery(resource.getFile());
        copyRecursively(new File(resource.getFile()), new File(resource.getOutput()), new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return !name.startsWith(".");
            }
        });
    }


    private void remoteExecuteImpl(Include resource, DeployerCommand command) throws Exception {
        logDelivery(resource.getFile());
        command.setLog(getLog());
        File zipFile = getZipFile(resource);

        String resourceDirectory = DeployerCommand.NO_DIRECTORY;
        File file = new File(resource.getFile());
        if (file.isDirectory()) {
            resourceDirectory = file.getName();
        }
        command.deploy(zipFile, resource.getOutput(), resourceDirectory);
    }


    private File getZipFile(Include resource) {
        File zipFile = new File(tmpDir, new File(resource.getFile()).getName() + ".zip");
        if (zipFile.exists()) {
            zipFile.delete();
        }

        File temp = new File(tmpDir, "toZip");
        if (temp.exists()) {
            FileUtil.deleteRecursively(temp);
        }

        copyRecursively(new File(resource.getFile()), temp, new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return !name.startsWith(".");
            }
        });
        AntUtil.zip(temp, zipFile);

        return zipFile;
    }


    private void copyRecursively(File src, File dest, FilenameFilter filter) {
        if (src.isDirectory()) {
            File[] files = src.listFiles(filter);
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                copyRecursively(file, new File(dest.getPath() + "/" + src.getName()), filter);
            }
        }
        else {
            AntUtil.copyFile(src, dest);
        }
    }


    private void logDelivery(String dest) {
        getLog().info("Déploiement des fichiers en "
                      + (isRemote() ? "Remote" : "Local")
                      + " dans le répertoire : "
                      + dest);
    }
}
