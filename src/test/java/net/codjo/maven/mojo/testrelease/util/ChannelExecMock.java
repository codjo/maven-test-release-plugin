package net.codjo.maven.mojo.testrelease.util;
import net.codjo.test.common.LogString;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
/**
 *
 */
public class ChannelExecMock extends ChannelExec {
    private LogString log = new LogString();
    private String commandOutput = "command output";
    private String commandErrorOutput = "";
    private int exitStatus = 0;
    private boolean closed = true;


    public ChannelExecMock() {
        this(new LogString());
    }


    public ChannelExecMock(LogString log) {
        this.log = log;
    }


    public void mockCommandOutput(String output) {
        this.commandOutput = output;
    }


    public void mockCommandErrorOutput(String output) {
        this.commandErrorOutput = output;
    }


    public void mockExitStatus(int exitStatusMockk) {
        this.exitStatus = exitStatusMockk;
    }



    public int getExitStatus() {
        log.call("getExitStatus");
        return exitStatus;
    }



    public boolean isClosed() {
        log.call("isClosed");
        return closed;
    }



    public void disconnect() {
        log.call("disconnect");
    }



    public void setCommand(String foo) {
        log.call("setCommand", foo);
    }



    public InputStream getInputStream() throws IOException {
        log.call("getInputStream");
        return new ByteArrayInputStream(commandOutput.getBytes());
    }



    public InputStream getExtInputStream() throws IOException {
        log.call("getExtInputStream");
        return new ByteArrayInputStream(commandErrorOutput.getBytes());
    }



    public void connect() throws JSchException {
        log.call("connect");
    }
}
