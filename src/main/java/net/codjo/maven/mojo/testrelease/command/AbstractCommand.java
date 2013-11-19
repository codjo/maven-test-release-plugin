package net.codjo.maven.mojo.testrelease.command;
import org.apache.maven.plugin.logging.Log;
/**
 *
 */
public class AbstractCommand {
    private Log log;


    public void setLog(Log log) {
        this.log = log;
    }


    public Log getLog() {
        return log;
    }


    protected void info(String message) {
        if (log == null) {
            return;
        }
        log.info(message);
    }


    protected void error(String errors) {
        if (log == null) {
            return;
        }
        log.error(errors);
    }


    protected void debug(String message) {
        if (log == null) {
            return;
        }
        log.debug(message);
    }
}
