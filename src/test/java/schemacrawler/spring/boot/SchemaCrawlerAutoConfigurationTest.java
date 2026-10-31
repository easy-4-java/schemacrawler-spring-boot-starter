package schemacrawler.spring.boot;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;
import schemacrawler.spring.boot.ext.DatabaseSchemaCrawlerOptions;
import schemacrawler.spring.boot.ext.RuleType;
import schemacrawler.spring.boot.ext.SchemaCrawlerInclusionRule;
import schemacrawler.spring.boot.ext.SchemaCrawlerInclusionRules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SchemaCrawlerAutoConfigurationTest {

    @Test
    void registersTemplateProviderAndKeepsApplicationContext() throws Exception {
        SchemaCrawlerAutoConfiguration configuration = new SchemaCrawlerAutoConfiguration();
        SchemaCrawlerProperties properties = new SchemaCrawlerProperties();
        ReflectionTestUtils.setField(configuration, "properties", properties);
        ApplicationContext context = mock(ApplicationContext.class);
        configuration.setApplicationContext(context);

        assertThat(configuration.getApplicationContext()).isSameAs(context);
        assertThat(configuration.disruptorTemplate()).isNotNull();
        JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setUrl("jdbc:hsqldb:mem:schemacrawler_config");
        dataSource.setUser("sa");
        assertThat(configuration.connectionProvider(dataSource).getConnection()).isNotNull();
        assertThat(configuration.databaseConnectorRegistry()).isNotNull();
    }

    @Test
    void appliesConfiguredInclusionRulesToCrawlerOptions() throws Exception {
        SchemaCrawlerAutoConfiguration configuration = new SchemaCrawlerAutoConfiguration();
        SchemaCrawlerProperties properties = new SchemaCrawlerProperties();
        DatabaseSchemaCrawlerOptions database = new DatabaseSchemaCrawlerOptions();
        database.setType("hsqldb");
        database.setUrl("jdbc:hsqldb:mem:schemacrawler_rules");
        SchemaCrawlerInclusionRules rules = new SchemaCrawlerInclusionRules();
        SchemaCrawlerInclusionRule all = new SchemaCrawlerInclusionRule();
        all.setType(RuleType.INCLUDE_ALL);
        rules.setSchemaInclusionRule(all);
        rules.setSequenceInclusionRule(all);
        rules.setSynonymInclusionRule(all);
        rules.setTableInclusionRule(all);
        rules.setColumnInclusionRule(all);
        rules.setRoutineInclusionRule(all);
        rules.setGrepColumnInclusionRule(all);
        rules.setGrepDefinitionInclusionRule(all);
        database.setRules(rules);
        properties.getCrawlerOptions().add(database);
        ReflectionTestUtils.setField(configuration, "properties", properties);

        assertThat(database.toConnectionOptions()).isNotNull();
        assertThat(configuration.databaseConnectorRegistry()).isNotNull();
        assertThat(database.getOptions().getGrepOptions()).isNotNull();
        assertThat(database.getOptions().getLimitOptions()).isNotNull();
    }
}
