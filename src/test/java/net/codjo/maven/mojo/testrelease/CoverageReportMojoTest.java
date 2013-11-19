package net.codjo.maven.mojo.testrelease;
import net.codjo.test.common.PathUtil;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.apache.maven.project.MavenProject;
/**
 *
 */
public class CoverageReportMojoTest extends AbstractTestReleaseMojoTestCase {
    private CoverageReportMojo mojo;


    protected void setUp() throws Exception {
        super.setUp();
        mojo = initMojo("coverageReport/pom-default.xml");

        mojo.setJavaExecutor(new JavaExecutorMock(log));

        MavenProject parent = new MavenProject();
        parent.setFile(new File(PathUtil.findTargetDirectory(getClass()),
                                "/test-classes/mojos/coverageReport/mint/pom.xml"));
        parent.getModel().setModules(Arrays.asList(new Object[]{"mint-gui",
                                                                "mint-server",
                                                                "module-without-sources"}));

        MockUtil.singleton.getProject().setParent(parent);
        MockUtil.singleton.getProject().setFile(new File("./mint/mint-release-test/pom.xml"));
    }


    public void test_generateReportWithServer() throws Exception {
        new File(mojo.coverageServerOutputFile).createNewFile();

        mojo.execute();

        assertLog("setWorkingDir(./mint/mint-release-test)"
                  + ", execute(emma, [%reflectClassPath%, %emmaClassPath%], report -r html"
                  + " -in target/client.es -in target/server.es"
                  + " -sp %target%/test-classes/mojos/coverageReport/mint/mint-gui/src/main/java"
                  + " -sp %target%/test-classes/mojos/coverageReport/mint/mint-server/src/main/java"
                  + " -Dreport.html.out.file=target/report/index.html)");

        new File(mojo.coverageServerOutputFile).delete();
    }


    public void test_generateReportWithoutServer() throws Exception {
        mojo.execute();

        assertLog("setWorkingDir(./mint/mint-release-test)"
                  + ", execute(emma, [%reflectClassPath%, %emmaClassPath%], report -r html"
                  + " -in target/client.es"
                  + " -sp %target%/test-classes/mojos/coverageReport/mint/mint-gui/src/main/java"
                  + " -sp %target%/test-classes/mojos/coverageReport/mint/mint-server/src/main/java"
                  + " -Dreport.html.out.file=target/report/index.html)");
    }


    public void test_generateReportWithUnitaryTests() throws Exception {
        File guiReport = createUnitaryReport("mint-gui");
        File serverReport = createUnitaryReport("mint-server");

        mojo.execute();

        assertLog("setWorkingDir(./mint/mint-release-test)"
                  + ", execute(emma, [%reflectClassPath%, %emmaClassPath%], report -r html"
                  + " -in target/client.es"
                  + " -sp %target%/test-classes/mojos/coverageReport/mint/mint-gui/src/main/java"
                  + " -sp %target%/test-classes/mojos/coverageReport/mint/mint-server/src/main/java"
                  + " -Dreport.html.out.file=target/report/index.html)"
                  + ", execute(emma, [%reflectClassPath%, %emmaClassPath%], report -r html"
                  + " -in target/client.es"
                  + " -in %target%/test-classes/mojos/coverageReport/mint/mint-gui/target/tu.es"
                  + " -in %target%/test-classes/mojos/coverageReport/mint/mint-server/target/tu.es"
                  + " -sp %target%/test-classes/mojos/coverageReport/mint/mint-gui/src/main/java"
                  + " -sp %target%/test-classes/mojos/coverageReport/mint/mint-server/src/main/java"
                  + " -Dreport.html.out.file=target/report/indexAll.html)");

        guiReport.delete();
        serverReport.delete();
    }


    private File createUnitaryReport(String module) throws IOException {
        File newFile = new File(PathUtil.findTargetDirectory(getClass()),
                                "/test-classes/mojos/coverageReport/mint/" + module + "/target/tu.es");
        newFile.getParentFile().mkdirs();
        newFile.createNewFile();
        return newFile;
    }


    private CoverageReportMojo initMojo(String pomFilePath) throws Exception {
        CoverageReportMojo coverageMojo = (CoverageReportMojo)initMojo("coverage-report", pomFilePath);
        coverageMojo.setWaitTime(0);
        return coverageMojo;
    }
}
