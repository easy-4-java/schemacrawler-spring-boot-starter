package schemacrawler.spring.boot;

import org.junit.jupiter.api.Test;
import schemacrawler.spring.boot.ext.command.ProcessExecutor;

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
        assertThat(executor.getProcessError()).isEmpty();
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
}
