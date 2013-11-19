package net.codjo.maven.mojo.testrelease;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.monitor.logging.DefaultLog;
import org.codehaus.plexus.logging.AbstractLogger;
import org.codehaus.plexus.logging.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;
/**
 *
 */
public class StopWebMojoTest extends AbstractTestReleaseMojoTestCase {
    private static final int PORT = 8082;


    public void test_execute_local() throws Exception {

        Server jetty = new Server(PORT);
        Context context = new Context(jetty, "/", Context.SESSIONS);

        final boolean[] done = new boolean[1];

        ServletHolder holder = new ServletHolder(new DefaultServlet() {
            protected void doGet(HttpServletRequest request, HttpServletResponse response)
                  throws ServletException, IOException {
                done[0] = true;
            }
        });
        context.addServlet(holder, "/mywebapp/stop");
        jetty.start();

        StopWebMojo mojo = initMojo("stopWeb/pom-default.xml");
        MockUtil singleton = MockUtil.singleton;
        singleton.getProject().setArtifacts(Collections.singleton(createTestArtifact()));
        mojo.execute();

        for (int i = 0; i < 1000; i++) {
            if (done[0]) {
                break;
            }
            Thread.sleep(10);
        }

        jetty.stop();
        assertTrue(done[0]);
    }


    public void test_execute_local_withFailure() throws Exception {
        StopWebMojo mojo = initMojo("stopWeb/pom-default.xml");
        MockUtil singleton = MockUtil.singleton;
        singleton.getProject().setArtifacts(Collections.singleton(createTestArtifact()));

        StringLogger logger = new StringLogger();
        mojo.setLog(new DefaultLog(logger));

        mojo.execute();

        assertEquals("debug: Impossible d'arreter le serveur web a l'adresse"
                     + " 'http://localhost:8082/mywebapp/stop' - il est peut-etre deja arrete"
                     + " - Connection refused: connect", logger.toString());
    }


    public void test_execute_remote() throws Exception {
        StopWebMojo mojo = initMojo("stopWeb/pom-remote.xml");
        mockShellCommand(mojo);

        mojo.execute();

        log.assertContent("createShellCommand(my-login, dummyServer)"
                          + ", setLog(...)"
                          + ", execute(. ~/.profile;cd /unix/directory/WEB;./web.sh stop)");
    }


    public void test_execute_remote_withFailure() throws Exception {
        StopWebMojo mojo = initMojo("stopWeb/pom-remote.xml");

        IOException failure = new IOException("failure");
        mockShellCommandFailure(mojo, failure);

        mojo.execute();
    }


    public void test_execute_withoutWebModule() throws Exception {
        StopWebMojo mojo = initMojo("stopWeb/pom-withoutWeb.xml");
        assertFalse(mojo.canExecuteGoal());
    }


    private DefaultArtifact createTestArtifact() {
        DefaultArtifact artifact = new DefaultArtifact("gr", "id", VersionRange.createFromVersion("1.0"),
                                                       "runtime", "jar", "go", null);
        artifact.setFile(new File("target/test-classes"));
        return artifact;
    }


    private StopWebMojo initMojo(String pomFilePath) throws Exception {
        return (StopWebMojo)initMojo("stop-web", pomFilePath);
    }


    private static class StringLogger extends AbstractLogger {

        final StringBuilder builder = new StringBuilder();


        StringLogger() {
            super(Logger.LEVEL_DEBUG, "name");
        }


        public void debug(String message, Throwable throwable) {
            write("debug", message, throwable);
        }


        public void info(String message, Throwable throwable) {
            write("debug", message, throwable);
        }


        public void warn(String message, Throwable throwable) {
            write("debug", message, throwable);
        }


        public void error(String message, Throwable throwable) {
            write("debug", message, throwable);
        }


        public void fatalError(String message, Throwable throwable) {
            write("debug", message, throwable);
        }


        public Logger getChildLogger(String name) {
            return null;
        }


        private void write(String level, String message, Throwable throwable) {
            builder.append(level).append(": ").append(message);
            if (throwable != null) {
                builder.append(" - ").append(throwable.getMessage());
            }
        }


        public String toString() {
            return builder.toString();
        }
    }
}
