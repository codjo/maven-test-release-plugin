package net.codjo.maven.mojo.testrelease;
import net.codjo.maven.common.artifact.ArtifactDescriptor;
import java.io.IOException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.maven.plugin.MojoExecutionException;
/**
 * Goal pour arreter un serveur web sur l'environnement local ou remote.
 *
 * @goal stop-web
 * @requiresDependencyResolution
 */
public class StopWebMojo extends AbstractStopMojo {

    /**
     * @parameter expression="${shutdownMainClass}" default-value="net.codjo.plugin.server.AdministrationShutdowner"
     * @noinspection UNUSED_SYMBOL
     */
    protected String shutdownMainClass;
    /**
     * @parameter expression="${webHost}"
     * @noinspection UNUSED_SYMBOL
     */
    protected String webHost;
    /**
     * @parameter expression="${webPort}"
     * @noinspection UNUSED_SYMBOL
     */
    protected int webPort;

    /**
     * @parameter expression="${applicationName}"
     * @noinspection UNUSED_SYMBOL
     */
    protected String applicationName;

    /**
     * Livrable du serveur web.
     *
     * @parameter
     * @noinspection UnusedDeclaration
     */
    private ArtifactDescriptor web;


    protected void localExecute() throws MojoExecutionException {
        HttpClient client = new HttpClient();
        String url = "http://" + webHost + ":" + webPort + "/" + applicationName + "/stop";
        HttpMethod method = new GetMethod(url);
        try {
            client.executeMethod(method);
        }
        catch (IOException e) {
            getLog().info("Impossible d'arreter le serveur web a l'adresse '" + url
                          + "' - il est peut-etre deja arrete", e);
        }
        finally {
            method.releaseConnection();
        }
    }


    protected String getServerDir() {
        return "WEB";
    }


    protected String getScriptName() {
        return "web.sh";
    }


    protected String getShutdownMainClass() {
        return shutdownMainClass;
    }


    protected String getServerHost() {
        return webHost;
    }


    protected int getServerPort() {
        return webPort;
    }


    protected ArtifactDescriptor getDelivery() {
        return web;
    }
}
