package net.codjo.maven.mojo.testrelease.util;
import net.codjo.test.common.LogString;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import java.io.InputStream;
import java.util.Vector;
/**
 *
 */
public class ChannelSftpMock extends ChannelSftp {
    private LogString log = new LogString();
    private SftpException putFailure;
    private SftpException rmFailure;
    private SftpException lsFailure;


    public ChannelSftpMock() {
        this(new LogString());
    }


    public ChannelSftpMock(LogString log) {
        this.log = log;
    }



    public void connect() throws JSchException {
        log.call("connect");
    }



    public void disconnect() {
        log.call("disconnect");
    }



    public void cd(String string) throws SftpException {
        log.call("cd", string);
    }


    /**
     * @noinspection CollectionDeclaredAsConcreteClass,UseOfObsoleteCollectionType
     */

    public Vector ls(String path) throws SftpException {
        if (lsFailure != null) {
            throw lsFailure;
        }
        return new Vector(0);
    }



    public void rm(String path) throws SftpException {
        if (rmFailure != null) {
            throw rmFailure;
        }
        log.call("rm", path);
    }



    public void put(InputStream src, String dst, int mode) throws SftpException {
        if (putFailure != null) {
            throw putFailure;
        }
        log.call("put",
                 "inputStream",
                 dst,
                 (mode == ChannelSftp.OVERWRITE ? "ChannelSftp.OVERWRITE" : "" + mode));
    }



    public void put(String src, String dst, SftpProgressMonitor monitor, int mode) throws SftpException {
        if (putFailure != null) {
            throw putFailure;
        }
        log.call("put",
                 src,
                 dst,
                 "monitor",
                 (mode == ChannelSftp.OVERWRITE ? "ChannelSftp.OVERWRITE" : "" + mode));
    }



    public void exit() {
        log.call("exit");
    }


    public void mockPutFailure(SftpException putFailureMock) {
        this.putFailure = putFailureMock;
    }


    public void mockRmFailure(SftpException exception) {
        rmFailure = exception;
    }


    public void mockLsFailure(SftpException failure) {
        lsFailure = failure;
    }
}
