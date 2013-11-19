package net.codjo.maven.mojo.testrelease.unix;
import com.jcraft.jsch.Session;
import junit.framework.TestCase;
/**
 *
 */
public class UnixSessionFactoryTest extends TestCase {
    private static final String SAM_LOGIN = "samexdev";
    private static final String SAM_HOST = "wd-sam";


    public void test_connect_to_wd_sam() throws Exception {
        UnixSessionFactory unixSessionFactory = new UnixSessionFactory(SAM_LOGIN, SAM_HOST);

        Session session = unixSessionFactory.createSession();

        session.connect();
        session.disconnect();
    }

/*
//    This test has been desactived because it locks the account (even with a successfull test before)
    public void test_connect_to_wd_sam_wrong_key() throws Exception {
        unixSessionFactory = new UnixSessionFactory(SAM_LOGIN, SAM_HOST,
                                                    getClass().getResource("ssh_test_key.txt"));

        Session session = unixSessionFactory.createSession();
        try {
            session.connect();
            fail();
        }
        catch (JSchException e) {
            assertEquals("Auth fail", e.getMessage());
        }
    }
*/
}
