package net.codjo.maven.mojo.testrelease.unix;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import net.codjo.maven.mojo.testrelease.command.AbstractCommand;
import net.codjo.test.release.util.ssh.UnixSessionFactory;
/**
 *
 */
class AbstractUnixCommand extends AbstractCommand {
    protected static final int CONNECT_TIMEOUT = 20000;
    private UnixSessionFactory sessionFactory;


    protected AbstractUnixCommand(UnixSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    public UnixSessionFactory getSessionFactory() {
        return sessionFactory;
    }


    protected Session connectSession() throws JSchException {
        Session session = sessionFactory.createSession();
        session.connect(CONNECT_TIMEOUT);
        return session;
    }
}
