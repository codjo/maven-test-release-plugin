package net.codjo.maven.mojo.testrelease.command;
import org.apache.maven.plugin.logging.Log;
/**
 *
 */
public interface OneShellCommand {
    public void setLog(Log log);


    void execute(String shellCommand) throws Exception;
}
