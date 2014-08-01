package net.codjo.maven.mojo.util;
import java.io.File;
import junit.framework.TestCase;
import org.joda.time.Duration;
/**
 *
 */
public class DefaultJavaExecutorTest extends TestCase {
    public void testExecute_defaultTimeout() throws Exception {
        executeWithTimeout(false, null);
    }


    public void testExecute_nullTimeout() throws Exception {
        executeWithTimeout(true, null);
    }


    private void executeWithTimeout(boolean useValue, Duration timeout) throws Exception {
        DefaultJavaExecutor executor = new DefaultJavaExecutor();
        executor.setSpawnProcess(true);
        if (useValue) {
            executor.setTimeout(timeout);
        }
        executor.execute(EmptyMain.class.getName(), new File[0], null);
    }
}
