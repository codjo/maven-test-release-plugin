package net.codjo.maven.mojo.testrelease.command;
import net.codjo.test.common.LogString;
import java.io.File;
import org.apache.maven.plugin.logging.Log;
/**
 *
 */
public class DeployerCommandMock implements DeployerCommand {
    private LogString log;


    public DeployerCommandMock() {
        this(new LogString());
    }


    public DeployerCommandMock(LogString log) {
        this.log = log;
    }


    public void setLog(Log logger) {
        log.call("setLog", "...");
    }


    public void deploy(File zipFile, String applicationDirectory, String zipRootDirectory) throws Exception {
        log.call("deploy", zipFile.getName(), applicationDirectory, zipRootDirectory);
    }
}
