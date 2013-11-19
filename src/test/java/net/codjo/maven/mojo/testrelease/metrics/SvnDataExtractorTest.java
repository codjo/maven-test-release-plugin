package net.codjo.maven.mojo.testrelease.metrics;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import junit.framework.TestCase;
import net.codjo.util.file.FileUtil;
/**
 *
 */
public class SvnDataExtractorTest extends TestCase {
    private SvnDataExtractor svnDataExtractor = new SvnDataExtractor();


    public void test_extract() {
        String entriesFileContent = createEntries("https://wp-subversion.am.agf.fr/myapp/trunk", "15193");
        SvnData data = svnDataExtractor.extract(entriesFileContent);

        assertEquals("15193", data.getRevision());
        assertEquals("https://wp-subversion.am.agf.fr/myapp/trunk", data.getUrl());
    }


    public void test_extract_fromFile() throws IOException {
        SvnData data = svnDataExtractor.extract(getSvnEntriesFile());

        assertEquals("La revision est un nombre",
                     "" + Long.parseLong(data.getRevision()), data.getRevision());
        assertTrue(data.getUrl(), data.getUrl().startsWith(
              "https://wp-subversion.am.agf.fr/development/framework/codjo/maven/plugins/maven-test-release-plugin/"));
    }


    public void test_extract_svn15Format() throws IOException {
        SvnData data
              = svnDataExtractor.extract(FileUtil.loadContent(getClass().getResource("entries-svn1-5")));

        assertEquals("15217", data.getRevision());
        assertEquals("https://wp-subversion.am.agf.fr/maven-test-release-plugin/trunk", data.getUrl());
    }


    public void test_extract_svnEntriesInvalid() {
        try {
            svnDataExtractor.extract("new SVN entries file format");
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals(SvnDataExtractor.INVALID_ENTRIES_FORMAT, ex.getMessage());
        }
    }


    private String createEntries(String scmUrl, String revision) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
               + "<wc-entries\n   xmlns=\"svn:\">\n"
               + "<entry\n"
               + "   committed-rev=\"15149\"\n"
               + "   name=\"\"\n"
               + "   committed-date=\"2009-03-11T14:58:42.911489Z\"\n"
               + "   url=\"" + scmUrl + "\"\n"
               + "   last-author=\"galaber\"\n"
               + "   kind=\"dir\"\n"
               + "   uuid=\"d9e0c693-6127-0410-a9b6-f47348635327\"\n"
               + "   repos=\"https://wp-subversion.am.agf.fr/development/framework\"\n"
               + "   prop-time=\"2009-03-12T08:32:07.031952Z\"\n"
               + "   revision=\"" + revision + "\"/>\n"
               + "<entry\n"
               + "   name=\"src\"\n"
               + "   kind=\"dir\"/>\n"
               + "<entry\n"
               + "   committed-rev=\"15149\"\n"
               + "   name=\"pom.xml\"\n"
               + "   text-time=\"2009-03-12T08:32:07.031952Z\"\n"
               + "   committed-date=\"2009-03-11T14:58:42.911489Z\"\n"
               + "   checksum=\"52ebf614f83fe1ae031fba3d1a6f2819\"\n"
               + "   last-author=\"galaber\"\n"
               + "   kind=\"file\"\n"
               + "   prop-time=\"2009-03-12T08:32:07.031952Z\"/>\n"
               + "</wc-entries>";
    }


    public static String currentSvnRevision() throws IOException {
        SvnData data = new SvnDataExtractor().extract(getSvnEntriesFile());
        return data.getRevision();
    }


    public static String currentSvnUrl() throws IOException {
        SvnData data = new SvnDataExtractor().extract(getSvnEntriesFile());
        return data.getUrl();
    }


    public static String getSvnEntriesPath() throws IOException, URISyntaxException {
        return new File(SvnDataExtractorTest.class.getResource("/dotsvn/svnentries").toURI()).getCanonicalPath();
    }


    private static String getSvnEntriesFile() throws IOException {
        try {
            return FileUtil.loadContent(new File(getSvnEntriesPath()));
        }
        catch (URISyntaxException e) {
            e.printStackTrace();  // Todo
        }
        return null;
    }
}
