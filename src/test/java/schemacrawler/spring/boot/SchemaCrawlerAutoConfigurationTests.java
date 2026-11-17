package schemacrawler.spring.boot;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import schemacrawler.spring.boot.ext.ConnectionProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SchemaCrawlerAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(SchemaCrawlerAutoConfiguration.class)
            .withBean(DataSource.class, () -> mock(DataSource.class));

    @Test
    public void disabledByDefault() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(SchemaCrawlerTemplate.class);
            assertThat(context).doesNotHaveBean(ConnectionProvider.class);
        });
    }

    @Test
    public void enabledCreatesStarterBeansWithoutConnectingToDatabase() {
        contextRunner.withPropertyValues("spring.schemacrawler.enabled=true").run(context -> {
            assertThat(context).hasSingleBean(SchemaCrawlerTemplate.class);
            assertThat(context).hasSingleBean(ConnectionProvider.class);
        });
    }
}
