package net.codjo.maven.mojo.testrelease.command;
import java.io.File;
import org.apache.maven.plugin.logging.Log;
/**
 *
 */
public interface DeployerCommand {
    public static final String NO_DIRECTORY = "no-directory";


    public void setLog(Log log);


    public void deploy(File zipFile, String applicationDirectory, String zipRootDirectory) throws Exception;
}
