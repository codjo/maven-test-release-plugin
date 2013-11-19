package net.codjo.maven.mojo.testrelease;
/**
 * Goal pour evaluer le statut du serveur web en local ou remote.
 *
 * @goal status-web
 */
public class StatusWebMojo extends AbstractStatusMojo {


    protected String getServerDir() {
        return "WEB";
    }



    protected String getScriptName() {
        return "web.sh";
    }
}
