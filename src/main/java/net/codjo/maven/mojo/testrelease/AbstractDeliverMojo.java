/*
 * Team : CODJO AM / OSI / SI / BO
 *
 * Copyright (c) 2001 CODJO Asset Management.
 */
package net.codjo.maven.mojo.testrelease;
import net.codjo.maven.common.artifact.ArtifactDescriptor;
import net.codjo.maven.common.resources.FilteredManager;
import net.codjo.maven.mojo.testrelease.command.DeployerCommand;
import net.codjo.maven.mojo.util.AntUtil;
import net.codjo.maven.mojo.util.ArtifactGetter;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Zip;
/**
 * Abstract Mojo pour livrer un artifact sur l'environnement local ou remote.
 */
public abstract class AbstractDeliverMojo extends AbstractTestReleaseMojo {
    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     * @noinspection UNUSED_SYMBOL
     */
    protected MavenProject project;
    /**
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    protected ArtifactRepository localRepository;
    /**
     * @parameter expression="${component.org.apache.maven.artifact.factory.ArtifactFactory}"
     * @required
     * @readonly
     */
    protected ArtifactFactory artifactFactory;
    /**
     * Repertoire ou le goal place le livrable unzippé (ex: SERVEUR, BATCH)
     *
     * @parameter expression="${releaseDirectory}" default-value="${project.basedir}/target"
     * @noinspection UNUSED_SYMBOL
     */
    protected File releaseDirectory;
    /**
     * @parameter expression="${component.org.apache.maven.artifact.manager.WagonManager}"
     * @required
     * @readonly
     * @noinspection UNUSED_SYMBOL
     */
    protected WagonManager wagonManager;

    private String tmpDir = System.getProperty("java.io.tmpdir") + "/testReleaseDeploy";


    protected boolean canExecuteGoal() {
        return getDelivery() != null;
    }


    protected void localExecute() throws MojoExecutionException {
        try {
            File instantiatedDeliveryZipFile = instanciateTemplateInZip(getDeliveryZipFile());
            logDelivery(releaseDirectory.getPath());
            AntUtil.unzip(instantiatedDeliveryZipFile, releaseDirectory);
        }
        catch (Exception exception) {
            throw new MojoExecutionException(exception.getLocalizedMessage(), exception);
        }
    }


    protected void remoteExecute() throws MojoExecutionException {
        try {
            if (isRemoteWindows()) {
                remoteExecuteImpl(getWindowsCommandFactory().createDeployer(),
                                  getWindowsApplicationDirectory());
            }
            else {
                remoteExecuteImpl(getUnixCommandFactory().createDeployer(createSessionFactory()),
                                  getUnixApplicationDirectory());
            }
        }
        catch (Exception exception) {
            throw new MojoExecutionException(exception.getLocalizedMessage(), exception);
        }
    }


    private void remoteExecuteImpl(DeployerCommand command, String applicationDirectory) throws Exception {
        File instantiatedDeliveryZipFile = instanciateTemplateInZip(getDeliveryZipFile());
        logDelivery(applicationDirectory);
        command.setLog(getLog());

        String directoryName = findFirstDirectory(instantiatedDeliveryZipFile);
        command.deploy(instantiatedDeliveryZipFile, applicationDirectory, directoryName);
    }


    protected File instanciateTemplateInZip(File srcFile)
          throws IOException {

        File destinationDirectory = new File(tmpDir);
        if (destinationDirectory.exists()) {
            deleteDirectory(destinationDirectory);
        }
        destinationDirectory.mkdir();
        unzipFile(destinationDirectory, srcFile);
        instanciateConfigFiles(destinationDirectory, ".template");
        return rezipDeliveryFile(destinationDirectory, srcFile);
    }


    private File rezipDeliveryFile(File destinationDirectory, File srcFile) {
        Zip zipTask = new Zip();
        zipTask.setBasedir(destinationDirectory);
        File zipFile = new File(destinationDirectory, srcFile.getName());
        zipTask.setDestFile(zipFile);
        zipTask.setProject(new Project());
        zipTask.execute();

        return zipFile;
    }


    private void instanciateConfigFiles(File destinationDirectory, String suffixPattern) throws IOException {
        // methode 3 recuperer tous les fichiers ayant le ".template"
        String[] includes = {"**\\*.template"};

        DirectoryScanner directoryScanner = new DirectoryScanner();
        directoryScanner.setIncludes(includes);
        directoryScanner.setBasedir(destinationDirectory);
        directoryScanner.setCaseSensitive(true);
        directoryScanner.scan();

        String[] extractedFiles = directoryScanner.getIncludedFiles();
        FilteredManager filteredManager = new FilteredManager(project);
        for (int i = 0; i < extractedFiles.length; i++) {
            File file = new File(destinationDirectory + "/" + extractedFiles[i]);
            int endIndex = file.getName().indexOf(suffixPattern);
            if (endIndex >= 0) {
                String newFileName = file.getName().substring(0, endIndex);
                filteredManager.copyFile(file, new File(file.getParent(), newFileName),
                                         true);
                file.delete();
            }
        }
    }


    private void unzipFile(File destinationDirectory, File srcFile) {// methode 2 dézippage
        Expand expand = new Expand();
        expand.setDest(destinationDirectory);
        expand.setSrc(srcFile);
        Project ant = new Project();
        expand.setProject(ant);
        ant.init();
        expand.execute();
    }


    public static boolean deleteDirectory(File path) {
        boolean resultat = true;
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    resultat &= deleteDirectory(files[i]);
                }
                else {
                    resultat &= files[i].delete();
                }
            }
        }
        resultat &= path.delete();
        return (resultat);
    }


    protected abstract ArtifactDescriptor getDelivery();


    protected File getDeliveryZipFile() throws TransferFailedException, ResourceDoesNotExistException {
        ArtifactGetter artifactGetter = new ArtifactGetter(artifactFactory,
                                                           localRepository,
                                                           project.getRemoteArtifactRepositories(),
                                                           wagonManager);

        ArtifactDescriptor delivery = getDelivery();
        delivery.resolveType("zip");
        delivery.resolveIncludeVersion(project.getDependencyManagement());

        Artifact artifact = artifactGetter.getArtifact(delivery);

        File zipFile = artifact.getFile();

        if (!zipFile.exists()) {
            throw new BuildException("Deploiement sur release-test impossible pour le module : '"
                                     + delivery.getArtifactId()
                                     + "' car il n'existe pas ou n'a pas ete genere.");
        }
        return zipFile;
    }


    protected void logDelivery(String dest) {
        getLog().info("Deploiement du livrable '" + getDelivery().getArtifactId()
                      + "' en " + (isRemote() ? "Remote" : "Local")
                      + " dans le repertoire " + dest);
    }


    public void setTmpDir(String tmpDir) {
        this.tmpDir = tmpDir;
    }


    static String findFirstDirectory(File zipFile) throws IOException {
        ZipFile zip = new ZipFile(zipFile);
        try {
            Enumeration enumeration = zip.entries();
            while (enumeration.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry)enumeration.nextElement();
                if (zipEntry.isDirectory()) {
                    return new File(zipEntry.getName()).getName();
                }
            }
        }
        finally {
            zip.close();
        }
        return DeployerCommand.NO_DIRECTORY;
    }
}
