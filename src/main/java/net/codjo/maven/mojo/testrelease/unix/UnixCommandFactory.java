package net.codjo.maven.mojo.testrelease.unix;
import net.codjo.maven.mojo.testrelease.command.DeployerCommand;
import net.codjo.maven.mojo.testrelease.command.OneShellCommand;
import net.codjo.test.release.util.ssh.UnixSessionFactory;
/**
 *
 */
public class UnixCommandFactory {
    public DeployerCommand createDeployer(UnixSessionFactory unixSessionFactory) {
        return new UnixDeployerCommand(unixSessionFactory);
    }


    public OneShellCommand createShellCommand(UnixSessionFactory unixSessionFactory) {
        return new OneUnixCommand(unixSessionFactory);
    }
}
