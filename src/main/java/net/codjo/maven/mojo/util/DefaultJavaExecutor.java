package net.codjo.maven.mojo.util;
import java.io.File;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Path;
import org.joda.time.Duration;
/**
 *
 */
public class DefaultJavaExecutor implements JavaExecutor {
    protected Duration timeout;
    private boolean spawnProcess = false;
    private boolean failOnError = true;
    private File workingDir = new File(".");
    private String jvmArg;
    private boolean displayProcessOutput = true;


    public void execute(String mainClass, File[] classpathFiles, String arguments)
          throws MojoExecutionException {
        Java java = new Java();
        AntUtil.initAnt(java, displayProcessOutput);
        java.setTaskName(mainClass.substring(mainClass.lastIndexOf(".") + 1, mainClass.length()));

        java.setClassname(mainClass);
        java.createArg().setLine(arguments);
        if (jvmArg != null) {
            java.createJvmarg().setLine(jvmArg);
        }
        java.setJvm(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
        java.setDir(workingDir);
        java.setFork(true);
        java.setTimeout(Long.valueOf(timeout.getMillis()));
        java.setSpawn(spawnProcess);

        java.addSysproperty(createVariable(Log4jUtil.CONFIGURATION_KEY, Log4jUtil.getConfigurationFile()));

        if (!spawnProcess) {
            java.setFailonerror(failOnError);
        }

        Path classpath = java.createClasspath();
        for (int i = 0; i < classpathFiles.length; i++) {
            File classpathFile = classpathFiles[i];
            Path path = classpath.createPath();
            path.setPath(classpathFile.getPath());
        }

        try {
            java.execute();
        }
        catch (BuildException buildException) {
            throw new MojoExecutionException("Erreur lors de l'exécution de la tâche Ant", buildException);
        }
    }


    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }


    public void setWorkingDir(File workingDir) {
        this.workingDir = workingDir;
    }


    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }


    public void setSpawnProcess(boolean spawnProcess) {
        this.spawnProcess = spawnProcess;
    }


    public void setJvmArg(String jvmArg) {
        this.jvmArg = jvmArg;
    }


    public void setDisplayProcessOutput(boolean displayProcessOutput) {
        this.displayProcessOutput = displayProcessOutput;
    }


    private Environment.Variable createVariable(String key, String value) {
        Environment.Variable sysp = new Environment.Variable();
        sysp.setKey(key);
        sysp.setValue(value);
        return sysp;
    }
}
