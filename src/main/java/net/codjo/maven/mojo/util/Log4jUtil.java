package net.codjo.maven.mojo.util;
/**
 *
 */
public class Log4jUtil {
    public static final String CONFIGURATION_KEY = "log4j.configuration";


    private Log4jUtil() {
    }


    public static String getLog4JConfiguration() {
        return "-D" + CONFIGURATION_KEY + "=\"" + getConfigurationFile() + "\"";
    }


    public static String getConfigurationFile() {
        return ("file:///" + System.getProperty("user.home") + "/log4j.properties").replace('\\', '/');
    }
}
