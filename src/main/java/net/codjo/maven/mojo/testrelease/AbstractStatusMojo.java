package net.codjo.maven.mojo.testrelease;
import org.apache.maven.plugin.MojoExecutionException;
/**
 *
 */
public class AbstractStatusMojo extends AbstractTestReleaseMojo {

    protected void localExecute() throws MojoExecutionException {
        getLog().info("Cette fonction n'est pas implante en mode local.");
    }



    protected void remoteExecute() throws MojoExecutionException {
        try {
            executeRemoteServerScript("status", "Etat du serveur");
        }
        catch (MojoExecutionException e) {
            // Les scripts renvoie une erreur si il est ON ..!?!
            //  donc la on est probablement sur que le serveur est ON... ou pas
            getLog().debug("Erreur lors de l'extraction du status du serveur (si ON c'est normal)");
        }
    }
}
