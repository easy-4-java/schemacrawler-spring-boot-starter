package schemacrawler.spring.boot;

import org.junit.jupiter.api.Test;
import schemacrawler.inclusionrule.IncludeAll;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.spring.boot.utils.SchemaCrawlerOptionBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaCrawlerOptionBuilderTest {

    @Test
    void preservesRequestedMetadataDetailLevel() {
        assertThat(SchemaCrawlerOptionBuilder.custom(SchemaInfoLevelBuilder.minimum())
                .getLoadOptions().getSchemaInfoLevel()).isEqualTo(SchemaInfoLevelBuilder.minimum());
        assertThat(SchemaCrawlerOptionBuilder.minimum()
                .getLoadOptions().getSchemaInfoLevel()).isEqualTo(SchemaInfoLevelBuilder.minimum());
        assertThat(SchemaCrawlerOptionBuilder.standard()
                .getLoadOptions().getSchemaInfoLevel()).isEqualTo(SchemaInfoLevelBuilder.standard());
        assertThat(SchemaCrawlerOptionBuilder.detailed()
                .getLoadOptions().getSchemaInfoLevel()).isEqualTo(SchemaInfoLevelBuilder.detailed());
        assertThat(SchemaCrawlerOptionBuilder.maximum()
                .getLoadOptions().getSchemaInfoLevel()).isEqualTo(SchemaInfoLevelBuilder.maximum());
    }

    @Test
    void tableColumnsDefaultsToStandardTableKindsAndHonorsExplicitTypes() {
        assertThat(SchemaCrawlerOptionBuilder.tablecolumns(new IncludeAll())
                .getLimitOptions().getTableTypes().toArray())
                .contains("BASE TABLE", "TABLE", "VIEW");
        assertThat(SchemaCrawlerOptionBuilder.tablecolumns(new IncludeAll(), "VIEW")
                .getLimitOptions().getTableTypes().toArray())
                .containsExactly("VIEW");
    }
}
