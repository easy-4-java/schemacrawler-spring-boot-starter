package schemacrawler.spring.boot;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import schemacrawler.inclusionrule.IncludeAll;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.schemacrawler.SchemaRetrievalOptionsBuilder;
import schemacrawler.spring.boot.ext.DatabaseSchemaCrawlerOptions;
import schemacrawler.spring.boot.ext.SchemaCrawlerConnectionProvider;
import schemacrawler.spring.boot.utils.SchemaCrawlerOptionBuilder;

import java.sql.Connection;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaCrawlerTemplateIntegrationTest {

    private static final JDBCDataSource DATA_SOURCE = new JDBCDataSource();

    @BeforeAll
    static void createSchema() throws Exception {
        DATA_SOURCE.setUrl("jdbc:hsqldb:mem:schemacrawler_ci");
        DATA_SOURCE.setUser("sa");
        DATA_SOURCE.setPassword("");
        try (Connection connection = DATA_SOURCE.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE BOOKS (ID INTEGER PRIMARY KEY, TITLE VARCHAR(100))");
        }
    }

    private SchemaCrawlerTemplate template() {
        SchemaCrawlerTemplate template = new SchemaCrawlerTemplate();
        ReflectionTestUtils.setField(template, "properties", new SchemaCrawlerProperties());
        return template;
    }

    private void assertFindsBooks(Catalog catalog) {
        assertThat(catalog.getTables()).extracting(Table::getName).contains("BOOKS");
    }

    @Test
    void selectsConfiguredOptionsOrFallsBackToStandard() {
        SchemaCrawlerTemplate template = template();
        assertThat(template.getCrawlerOptions("missing")).isNotNull();

        DatabaseSchemaCrawlerOptions configured = new DatabaseSchemaCrawlerOptions();
        configured.setType("hsqldb");
        SchemaCrawlerProperties properties = new SchemaCrawlerProperties();
        properties.getCrawlerOptions().add(configured);
        ReflectionTestUtils.setField(template, "properties", properties);

        assertThat(template.getCrawlerOptions("hsqldb")).isSameAs(configured.getOptions());
        assertThat(template.getCrawlerOptions("another")).isNotNull();
    }

    @Test
    void crawlsRealTableThroughDataSourceAndConnectionOverloads() throws Exception {
        SchemaCrawlerTemplate template = template();
        assertFindsBooks(template.crawl(DATA_SOURCE, "hsqldb"));
        assertFindsBooks(template.crawl(DATA_SOURCE, SchemaInfoLevelBuilder.standard()));
        assertFindsBooks(template.crawl(DATA_SOURCE, new IncludeAll(), new IncludeAll()));

        try (Connection connection = DATA_SOURCE.getConnection()) {
            assertFindsBooks(template.crawl(connection, "hsqldb"));
            assertFindsBooks(template.crawl(connection, SchemaInfoLevelBuilder.standard()));
            assertFindsBooks(template.crawl(connection, new IncludeAll(), new IncludeAll()));
            assertFindsBooks(template.crawl(connection, SchemaCrawlerOptionBuilder.standard()));
            assertFindsBooks(template.crawl(connection,
                    SchemaRetrievalOptionsBuilder.newSchemaRetrievalOptions(),
                    SchemaCrawlerOptionBuilder.standard()));
        }
    }

    @Test
    void crawlsRealTableThroughConnectionProvider() throws Exception {
        SchemaCrawlerTemplate template = template();
        SchemaCrawlerConnectionProvider provider = new SchemaCrawlerConnectionProvider(DATA_SOURCE);
        try (Connection connection = provider.getConnection()) {
            assertThat(connection.isClosed()).isFalse();
        }
        assertFindsBooks(template.crawl(provider, SchemaInfoLevelBuilder.standard()));
        assertFindsBooks(template.crawl(provider, new IncludeAll(), new IncludeAll()));
    }
}
