package net.codjo.maven.mojo.testrelease.windows;
import net.codjo.maven.mojo.testrelease.command.AbstractCommand;
import net.codjo.maven.mojo.testrelease.command.OneShellCommand;
import net.codjo.maven.mojo.util.AntUtil;
import org.apache.tools.ant.taskdefs.ExecTask;
/**
 *
 */
class OneDosCommand extends AbstractCommand implements OneShellCommand {

    public void execute(String shellCommand) throws Exception {
        info("Execution : " + shellCommand);
        ExecTask execute = new ExecTask();
        execute.setTaskName("exec");
        AntUtil.initAnt(execute, true);

        execute.setExecutable("cmd.exe");
        execute.setFailonerror(true);
        execute.setTimeout(new Long(5000));
        execute.createArg().setLine("/c " + shellCommand);
        execute.execute();
    }
}
