package net.codjo.maven.mojo.testrelease;
import java.io.File;
import net.codjo.maven.mojo.util.JavaExecutor;
import net.codjo.test.common.LogString;
import org.apache.maven.plugin.MojoExecutionException;
import org.joda.time.Duration;
/**
 *
 */
class JavaExecutorMock implements JavaExecutor {
    private final LogString logger;
    private MojoExecutionException executeFailure;


    JavaExecutorMock(LogString logger) {
        this.logger = logger;
    }


    public void execute(String mainClass, File[] classpath, String arguments) throws MojoExecutionException {
        if (executeFailure != null) {
            MojoExecutionException failure = executeFailure;
            executeFailure = null;
            throw failure;
        }
        logger.call("execute", mainClass, toSimpleClassPath(classpath), arguments);
    }


    public void setWorkingDir(File workingDir) {
        logger.call("setWorkingDir", workingDir);
    }


    public void setFailOnError(boolean failOnError) {
        logger.call("setFailOnError", String.valueOf(failOnError));
    }


    public void setTimeout(Duration timeout) {
        logger.call("setTimeout", Long.valueOf(timeout.getMillis()));
    }


    private String toSimpleClassPath(File[] classpath) {
        StringBuffer buffer = new StringBuffer("[");
        for (int i = 0; i < classpath.length; i++) {
            File file = classpath[i];
            if (buffer.length() != 1) {
                buffer.append(", ");
            }
            buffer.append(file.getName());
        }
        return buffer.append("]").toString();
    }


    public void setSpawnProcess(boolean spawnProcess) {
        logger.call("setSpawnProcess", String.valueOf(spawnProcess));
    }


    public void setJvmArg(String jvmArg) {
        logger.call("setJvmArg", jvmArg);
    }


    public void setDisplayProcessOutput(boolean displayProcessOutput) {
        logger.call("setDisplayProcessOutput", Boolean.toString(displayProcessOutput));
    }


    public void mockExecuteFailure(MojoExecutionException failure) {
        executeFailure = failure;
    }
}
