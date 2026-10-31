package schemacrawler.spring.boot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import schemacrawler.spring.boot.ext.SchemaCrawlerInclusionRule;
import schemacrawler.spring.boot.ext.SchemaCrawlerInclusionRules;

/**
 * Tests for {@link SchemaCrawlerInclusionRules}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
class SchemaCrawlerInclusionRulesTest {

    @Test
    void defaultValues() {
        SchemaCrawlerInclusionRules rules = new SchemaCrawlerInclusionRules();
        assertThat(rules.getSchemaInclusionRule()).isNull();
        assertThat(rules.getTableInclusionRule()).isNull();
        assertThat(rules.getColumnInclusionRule()).isNull();
        assertThat(rules.getSynonymInclusionRule()).isNull();
        assertThat(rules.getSequenceInclusionRule()).isNull();
        assertThat(rules.getRoutineInclusionRule()).isNull();
        assertThat(rules.getRoutineColumnInclusionRule()).isNull();
        assertThat(rules.getGrepColumnInclusionRule()).isNull();
        assertThat(rules.getGrepRoutineColumnInclusionRule()).isNull();
        assertThat(rules.getGrepDefinitionInclusionRule()).isNull();
    }

    @Test
    void setterGetter() {
        SchemaCrawlerInclusionRules rules = new SchemaCrawlerInclusionRules();
        SchemaCrawlerInclusionRule rule = new SchemaCrawlerInclusionRule();

        rules.setSchemaInclusionRule(rule);
        assertThat(rules.getSchemaInclusionRule()).isEqualTo(rule);

        rules.setTableInclusionRule(rule);
        assertThat(rules.getTableInclusionRule()).isEqualTo(rule);

        rules.setColumnInclusionRule(rule);
        assertThat(rules.getColumnInclusionRule()).isEqualTo(rule);

        rules.setSynonymInclusionRule(rule);
        assertThat(rules.getSynonymInclusionRule()).isEqualTo(rule);

        rules.setSequenceInclusionRule(rule);
        assertThat(rules.getSequenceInclusionRule()).isEqualTo(rule);

        rules.setRoutineInclusionRule(rule);
        assertThat(rules.getRoutineInclusionRule()).isEqualTo(rule);

        rules.setRoutineColumnInclusionRule(rule);
        assertThat(rules.getRoutineColumnInclusionRule()).isEqualTo(rule);

        rules.setGrepColumnInclusionRule(rule);
        assertThat(rules.getGrepColumnInclusionRule()).isEqualTo(rule);

        rules.setGrepRoutineColumnInclusionRule(rule);
        assertThat(rules.getGrepRoutineColumnInclusionRule()).isEqualTo(rule);

        rules.setGrepDefinitionInclusionRule(rule);
        assertThat(rules.getGrepDefinitionInclusionRule()).isEqualTo(rule);

        rules.setRoutineTypes(java.util.List.of(schemacrawler.schema.RoutineType.procedure));
        assertThat(rules.getRoutineTypes()).containsExactly(schemacrawler.schema.RoutineType.procedure);
    }
}
