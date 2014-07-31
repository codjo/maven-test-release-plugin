package net.codjo.maven.mojo.testrelease;
import com.intellij.rt.execution.emma.RunnerAgent;
import com.vladium.emma.EMMAProperties;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.codjo.maven.mojo.util.JavaExecutor;
import net.codjo.reflect.collect.ClassCollector;
import net.codjo.reflect.collect.PreloadClassesMainWrapper;
import net.codjo.reflect.collect.ReflectUtil;
import org.apache.maven.plugin.MojoExecutionException;
import org.joda.time.Duration;
/**
 *
 */
class EmmaJavaExecutor implements JavaExecutor {
    private JavaExecutor java;
    private String packageToInclude;
    private String packagesToExclude;
    private String coverageOutput;
    private String jvmArg;


    EmmaJavaExecutor(JavaExecutor java, String packageToInclude,
                     String packagesToExclude, String coverageOutput) {
        this.java = java;
        this.packageToInclude = packageToInclude;
        this.packagesToExclude = packagesToExclude;
        this.coverageOutput = coverageOutput;
    }


    public void execute(String mainClass, File[] classpath, String arguments) throws MojoExecutionException {
        if (jvmArg == null) {
            java.setJvmArg(getEmmaAgentJvmArg());
        }
        else {
            java.setJvmArg(jvmArg + " " + getEmmaAgentJvmArg());
        }
        java.execute(PreloadClassesMainWrapper.class.getName(),
                     toEmmaClasspath(classpath),
                     toMainWrapperArguments(mainClass, arguments));
    }


    public void setWorkingDir(File workingDir) {
        java.setWorkingDir(workingDir);
    }


    public void setFailOnError(boolean failOnError) {
        java.setFailOnError(failOnError);
    }


    public void setTimeout(Duration timeout) {
        java.setTimeout(timeout);
    }


    public void setSpawnProcess(boolean spawnProcess) {
        java.setSpawnProcess(spawnProcess);
    }


    public void setJvmArg(String jvmArg) {
        this.jvmArg = jvmArg;
    }


    public void setDisplayProcessOutput(boolean displayProcessOutput) {
        java.setDisplayProcessOutput(displayProcessOutput);
    }


    public static File[] toEmmaClasspath(File[] realClasspath) {
        List fullClasspath = new ArrayList();
        if (realClasspath != null) {
            fullClasspath.addAll(Arrays.asList(realClasspath));
        }

        addCodjoReflectIfNeeded(fullClasspath);
        fullClasspath.add(new File(ReflectUtil.determinePathFrom(EMMAProperties.class)));
        fullClasspath.add(getEmmaAgentJar());

        return (File[])fullClasspath.toArray(new File[fullClasspath.size()]);
    }


    private static void addCodjoReflectIfNeeded(List fullClasspath) {
        File codjoReflectJar = new File(ReflectUtil.determinePathFrom(ClassCollector.class));

        for (int i = 0; i < fullClasspath.size(); i++) {
            File file = (File)fullClasspath.get(i);
            if (file.getName().startsWith("codjo-reflect-")) {
                return;
            }
        }

        fullClasspath.add(codjoReflectJar);
    }


    private String toMainWrapperArguments(String realMainClass, String realArguments) {
        StringBuilder arguments = new StringBuilder();
        arguments
              .append(packageToInclude)
              .append(' ');

        if ((packagesToExclude != null) && (packagesToExclude.length() > 0)) {
            arguments.append("-exclude ").append(packagesToExclude).append(' ');
        }

        arguments.append(realMainClass)
              .append(' ')
              .append(realArguments);
        return arguments.toString();
    }


    private String getEmmaAgentJvmArg() {
        return "\"-javaagent:" + getEmmaAgentJar() + "=" + getFilter() + " " + getCoverageOutputFile() + "\""
               + " -Demma.rt.control=false"
               + " -Dcoverage.out.merge=true";
    }


    private String getFilter() {
        return "-f " + packageToInclude.replaceAll("\\.", "/") + "/*";
    }


    private String getCoverageOutputFile() {
        return "-o " + coverageOutput;
    }


    private static File getEmmaAgentJar() {
        return new File(ReflectUtil.determinePathFrom(RunnerAgent.class));
    }
}
