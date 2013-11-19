package net.codjo.maven.mojo.testrelease.windows;
import net.codjo.maven.mojo.testrelease.command.DeployerCommand;
import net.codjo.maven.mojo.testrelease.command.OneShellCommand;
/**
 *
 */
public class WindowsCommandFactory {
    public OneShellCommand createShellCommand() {
        return new OneDosCommand();
    }


    public DeployerCommand createDeployer() {
        return new WindowsDeployerCommand();
    }
}
