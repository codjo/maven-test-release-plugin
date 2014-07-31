package net.codjo.maven.mojo.testrelease;
import java.io.File;
import org.joda.time.Duration;
/**
 *
 */
public class EmmaJavaExecutorTest extends AbstractTestReleaseMojoTestCase {
    private EmmaJavaExecutor emmaExecutor = new EmmaJavaExecutor(new JavaExecutorMock(log),
                                                                 "my.group.id", null, "./release-test.es");


    public void test_execute_simple() throws Exception {
        emmaExecutor.execute("net.codjo.blue.Belt", classPath("common-1.0.jar"), "arg1 arg2");

        assertLog(
              "setJvmArg(\"-javaagent:%emmaAgentJar%=-f my/group/id/* -o ./release-test.es\" -Demma.rt.control=false -Dcoverage.out.merge=true)"
              + ", execute(net.codjo.reflect.collect.PreloadClassesMainWrapper, [common-1.0.jar, %reflectClassPath%, %emmaClassPath%], my.group.id net.codjo.blue.Belt arg1 arg2)");
    }


    public void test_execute_withJvmArgs() throws Exception {
        emmaExecutor.setJvmArg("-Xmx512m");
        emmaExecutor.execute("net.codjo.blue.Belt", classPath("common-1.0.jar"), "arg1 arg2");

        assertLog(
              "setJvmArg(-Xmx512m \"-javaagent:%emmaAgentJar%=-f my/group/id/* -o ./release-test.es\" -Demma.rt.control=false -Dcoverage.out.merge=true)"
              + ", execute(net.codjo.reflect.collect.PreloadClassesMainWrapper, [common-1.0.jar, %reflectClassPath%, %emmaClassPath%], my.group.id net.codjo.blue.Belt arg1 arg2)");
    }


    public void test_execute_codjoReflectAlreadyExists() throws Exception {
        emmaExecutor.setJvmArg("-Xmx512m");
        emmaExecutor.execute("net.codjo.blue.Belt", classPath("codjo-reflect-5.0.jar"), "arg1 arg2");

        assertLog(
              "setJvmArg(-Xmx512m \"-javaagent:%emmaAgentJar%=-f my/group/id/* -o ./release-test.es\" -Demma.rt.control=false -Dcoverage.out.merge=true)"
              + ", execute(net.codjo.reflect.collect.PreloadClassesMainWrapper, [codjo-reflect-5.0.jar, %emmaClassPath%], my.group.id net.codjo.blue.Belt arg1 arg2)");
    }


    public void test_execute_withPackagesToExclude() throws Exception {
        emmaExecutor = new EmmaJavaExecutor(new JavaExecutorMock(log),
                                            "my.group.id", "net.codjo.truc;com.as.you.are", "./release-test.es");
        emmaExecutor.execute("net.codjo.blue.Belt", classPath("common-1.0.jar"), "arg1 arg2");

        assertLog(
              "setJvmArg(\"-javaagent:%emmaAgentJar%=-f my/group/id/* -o ./release-test.es\" -Demma.rt.control=false -Dcoverage.out.merge=true)"
              + ", execute(net.codjo.reflect.collect.PreloadClassesMainWrapper, [common-1.0.jar, %reflectClassPath%, %emmaClassPath%], my.group.id -exclude net.codjo.truc;com.as.you.are net.codjo.blue.Belt arg1 arg2)");
    }


    public void test_delegate_setWorkingDir() {
        emmaExecutor.setWorkingDir(new File("."));
        log.assertContent("setWorkingDir(.)");
    }


    public void test_delegate_setFailOnError() {
        emmaExecutor.setFailOnError(true);
        log.assertContent("setFailOnError(true)");
    }


    public void test_delegate_setTimeout() {
        emmaExecutor.setTimeout(new Duration(5));
        log.assertContent("setTimeout(5)");
    }


    public void test_delegate_setSpawnProcess() {
        emmaExecutor.setSpawnProcess(true);
        log.assertContent("setSpawnProcess(true)");
    }


    public void test_delegate_setDisplayProcessOutput() {
        emmaExecutor.setDisplayProcessOutput(true);
        log.assertContent("setDisplayProcessOutput(true)");
    }


    private File[] classPath(String pathname) {
        return new File[]{new File(pathname)};
    }
}
