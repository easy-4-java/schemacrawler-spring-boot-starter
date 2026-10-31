package schemacrawler.spring.boot;

import org.junit.jupiter.api.Test;
import schemacrawler.spring.boot.ext.command.ProcessExecutor;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProcessExecutorTest {

    @Test
    void capturesCommandOutputWithoutDiscardingArguments() throws Exception {
        ProcessExecutor executor = new ProcessExecutor();
        executor.setCommandLine(List.of("/bin/sh", "-c", "printf 'hello world'"));

        assertThat(executor.getCommand())
                .containsExactly("/bin/sh", "-c", "printf 'hello world'");
        assertThat(executor.call()).isZero();
        assertThat(executor.getExitCode()).isZero();
        assertThat(executor.getProcessOutput()).isEqualTo("hello world");
                .isEqualTo("5\n[]\n[ ]\n[hello world]\n[\"quoted\"]\n[a;b]");
    }
    @Test
    void capturesStandardErrorAndExitStatus() throws Exception {
        ProcessExecutor executor = new ProcessExecutor();
        executor.setCommandLine(List.of("/bin/sh", "-c", "printf warning >&2; exit 7"));

        assertThat(executor.call()).isEqualTo(7);
        assertThat(executor.getExitCode()).isEqualTo(7);
        assertThat(executor.getProcessError()).isEqualTo("warning");
        assertThat(executor.getProcessOutput()).isEmpty();
    }

    @Test
    void rejectsMissingCommandInsteadOfStartingProcess() {
        ProcessExecutor executor = new ProcessExecutor();
        assertThatThrownBy(executor::call).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("No command provided");
        assertThatThrownBy(() -> executor.setCommandLine(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    public static final class ArgvEcho {
        public static void main(String[] args) {
            System.out.print(args.length);
            for (String arg : args) {
                System.out.print("\n[" + arg + "]");
            }
        }
    }

    @Test
    void preservesEachRawArgumentWhenLaunchingJavaDirectly() throws Exception {
        String java = System.getProperty("java.home") + File.separator + "bin"
                + File.separator + "java";
        String testClasses = Paths.get(ArgvEcho.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).toString();
        ProcessExecutor executor = new ProcessExecutor();
        executor.setCommandLine(List.of(java, "-cp", testClasses, ArgvEcho.class.getName(),
                "", " ", "hello world", "\"quoted\"", "a;b"));

        assertThat(executor.call()).isZero();
        assertThat(executor.getProcessOutput())
                "", " ", "hello world", "\"quoted\"", "a;b"));
        assertThat(executor.getProcessError()).isEmpty();
    }

    @Test
    void rejectsInvalidExecutableAndNullArgument() {
        ProcessExecutor executor = new ProcessExecutor();
        assertThatThrownBy(() -> executor.setCommandLine(List.of(" ", "arg")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> executor.setCommandLine(Arrays.asList("java", null)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
