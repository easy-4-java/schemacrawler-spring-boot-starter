package schemacrawler.spring.boot;

import org.junit.jupiter.api.Test;
import schemacrawler.spring.boot.ext.command.ProcessExecutor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
