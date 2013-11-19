package net.codjo.maven.mojo.testrelease.command;
import net.codjo.test.common.LogString;
import org.apache.maven.plugin.logging.Log;
/**
 *
 */
public class OneShellCommandMock implements OneShellCommand {
    private LogString log;


    public OneShellCommandMock() {
        this(new LogString());
    }


    public OneShellCommandMock(LogString log) {
        this.log = log;
    }


    public void execute(String shellCommand) throws Exception {
        log.call("execute", shellCommand);
    }


    public void setLog(Log logger) {
        log.call("setLog", "...");
    }
}
