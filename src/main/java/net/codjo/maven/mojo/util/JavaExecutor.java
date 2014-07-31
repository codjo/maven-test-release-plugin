package net.codjo.maven.mojo.util;
import java.io.File;
import org.apache.maven.plugin.MojoExecutionException;
import org.joda.time.Duration;
/**
 *
 */
public interface JavaExecutor {
    void execute(String mainClass, File[] classpath, String arguments)
          throws MojoExecutionException;


    void setWorkingDir(File workingDir);


    void setFailOnError(boolean failOnError);


    public void setTimeout(Duration timeout);


    void setSpawnProcess(boolean spawnProcess);


    public void setJvmArg(String jvmArg);


    void setDisplayProcessOutput(boolean displayProcessOutput);
}
