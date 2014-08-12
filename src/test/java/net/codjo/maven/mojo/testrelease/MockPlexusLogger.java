package net.codjo.maven.mojo.testrelease;
import org.codehaus.plexus.logging.AbstractLogger;
import org.codehaus.plexus.logging.Logger;
import org.junit.Assert;
/**
 *
 */
public class MockPlexusLogger extends AbstractLogger {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private final StringBuilder buffer = new StringBuilder();


    public MockPlexusLogger() {
        super(LEVEL_DEBUG, "MockPlexusLogger");
    }


    private MockPlexusLogger(int threshold, String name) {
        super(threshold, name);
    }


    public StringBuilder getBuffer() {
        return buffer;
    }


    public void debug(String message, Throwable throwable) {
        logMessage("DEBUG", message, throwable);
    }


    public void info(String message, Throwable throwable) {
        logMessage("INFO", message, throwable);
    }


    public void warn(String message, Throwable throwable) {
        logMessage("WARN", message, throwable);
    }


    public void error(String message, Throwable throwable) {
        logMessage("ERROR", message, throwable);
    }


    public void fatalError(String message, Throwable throwable) {
        logMessage("FATAL", message, throwable);
    }


    public Logger getChildLogger(String name) {
        return new MockPlexusLogger(getThreshold(), getName() + '.' + name);
    }


    private void logMessage(String levelName, String message, Throwable throwable) {
        buffer.append('[').append(levelName).append("] ");
        if (message != null) {
            buffer.append(message);
        }
        if (throwable != null) {
            buffer.append(throwable.getClass()).append(" : ").append(throwable.getMessage());
        }
        buffer.append(LINE_SEPARATOR);
    }


    public void assertContains(String expectedLog) {
        StringBuilder message = new StringBuilder("The message '");
        message.append(expectedLog).append("' was expected in the log.").append(LINE_SEPARATOR);
        message.append("Actual log :").append(LINE_SEPARATOR);
        message.append(buffer.toString());
        Assert.assertTrue(message.toString(), buffer.indexOf(expectedLog) >= 0);
    }
}
