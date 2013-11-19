package net.codjo.maven.mojo.testrelease;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
/**
 *
 */
public class AdministrationShutdownerMock {
    public static final String SHUTDOWN_LOG_FILE = "target/AdministrationShutdownerMock.txt";


    private AdministrationShutdownerMock() {}


    public static void main(String[] args) throws IOException {
        FileWriter writer = new FileWriter(SHUTDOWN_LOG_FILE);
        try {
            writer.write("AdministrationShutdownerMock.main(" + Arrays.asList(args) + ")");
        }
        finally {
            writer.close();
        }
    }
}
