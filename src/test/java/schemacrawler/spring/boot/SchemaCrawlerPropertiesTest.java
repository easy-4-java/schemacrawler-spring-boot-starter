package schemacrawler.spring.boot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SchemaCrawlerProperties}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
class SchemaCrawlerPropertiesTest {

    @Test
    void defaultValues() {
        SchemaCrawlerProperties props = new SchemaCrawlerProperties();
        assertThat(props.isEnabled()).isFalse();
        assertThat(props.getCrawlerOptions()).isNotNull().isEmpty();
    }

    @Test
    void prefix() {
        assertThat(SchemaCrawlerProperties.PREFIX).isEqualTo("spring.schemacrawler");
    }

    @Test
    void setterGetter() {
        SchemaCrawlerProperties props = new SchemaCrawlerProperties();
        props.setEnabled(true);
        assertThat(props.isEnabled()).isTrue();

        props.setEnabled(false);
        assertThat(props.isEnabled()).isFalse();

        schemacrawler.spring.boot.ext.DatabaseSchemaCrawlerOptions options =
                new schemacrawler.spring.boot.ext.DatabaseSchemaCrawlerOptions();
        props.setCrawlerOptions(java.util.List.of(options));
        assertThat(props.getCrawlerOptions()).containsExactly(options);
    }
}
