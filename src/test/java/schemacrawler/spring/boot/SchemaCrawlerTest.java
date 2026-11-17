/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2018, Sualeh Fatehi <sualeh@hotmail.com>.
All rights reserved.
------------------------------------------------------------------------

SchemaCrawler is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

SchemaCrawler and the accompanying materials are made available under
the terms of the Eclipse Public License v1.0, GNU General Public License
v3 or GNU Lesser General Public License v3.

You may elect to redistribute this code under any of these licenses.

The Eclipse Public License is available at:
http://www.eclipse.org/legal/epl-v10.html

The GNU General Public License v3 and the GNU Lesser General Public
License v3 are available at:
http://www.gnu.org/licenses/

========================================================================
*/

package schemacrawler.spring.boot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.util.StringUtils;

import schemacrawler.inclusionrule.IncludeAll;
import schemacrawler.inclusionrule.InclusionRule;
import schemacrawler.inclusionrule.RegularExpressionExclusionRule;
import schemacrawler.inclusionrule.RegularExpressionInclusionRule;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.DataTypeType;
import schemacrawler.schema.EventManipulationType;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Sequence;
import schemacrawler.schema.Synonym;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableConstraint;
import schemacrawler.schema.TableConstraintColumn;
import schemacrawler.schema.TableRelationshipType;
import schemacrawler.schema.Trigger;
import schemacrawler.schema.View;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaInfoLevel;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.schemacrawler.SchemaReference;
import schemacrawler.spring.boot.utility.TestName;
import schemacrawler.spring.boot.utility.TestWriter;
import schemacrawler.spring.boot.utils.SchemaCrawlerOptionBuilder;
import schemacrawler.utility.NamedObjectSort;

public class SchemaCrawlerTest extends BaseDatabaseTest {

	@Rule
	public TestName testName = new TestName();

	@Before
	public void setUp() throws Exception {
		// Create a database connection
		super.setUp("jdbc:sqlserver://192.168.0.118:1433;DatabaseName=91118net;integratedSecurity=false");
	}

	@Test
	public void schemas() throws Exception {

		final InclusionRule schemaInclusionRule = new RegularExpressionInclusionRule("91118net");
		final Catalog catalog = this.getCatalog("sa", "sa", schemaInclusionRule);
		for (final Schema schema : catalog.getSchemas()) {
			System.out.println(schema);
			for (final Table table : catalog.getTables(schema)) {
				System.out.println("o--> " + table + " pk " + table.getPrimaryKey() + " fks " + table.getForeignKeys()
						+ " type " + table.getTableType());
				for (final Column column : table.getColumns()) {
					System.out.println("     o--> " + column + " pk: " + column.isPartOfPrimaryKey() + " fk: "
							+ column.isPartOfForeignKey());
				}
			}
		}
	}

	@Test
	public void columns() throws Exception {
		try (final TestWriter out = new TestWriter("text");) {

			final InclusionRule schemaInclusionRule = new RegularExpressionInclusionRule("91118net");

			final SchemaCrawlerOptions options = SchemaCrawlerOptionBuilder
					.tablecolumns(new IncludeAll(), "TABLE", "VIEW");

			// Get the schema definition
			final Catalog catalog = getCatalog("sa", "sa", options);
			final Schema[] schemas = catalog.getSchemas().toArray(new Schema[0]);
			// assertEquals("Schema count does not match", 5, schemas.length);
			for (final Schema schema : schemas) {
				final Table[] tables = catalog.getTables(schema).toArray(new Table[0]);
				Arrays.sort(tables, NamedObjectSort.alphabetical);
				for (final Table table : tables) {
					final Column[] columns = table.getColumns().toArray(new Column[0]);
					Arrays.sort(columns);
					for (final Column column : columns) {
						out.println(String.format("%s [%s] default=\"%s\"", column.getFullName(),
								column.getColumnDataType(), column.getDefaultValue()));
						final SortedMap<String, Object> columnAttributes = new TreeMap<>(column.getAttributes());
						for (final Entry<String, Object> columnAttribute : columnAttributes.entrySet()) {
							out.println(
									String.format("  ~ %s=%s", columnAttribute.getKey(), columnAttribute.getValue()));
						}
					}

					out.println();
				}
			}

			out.assertEquals(testName.currentMethodFullName());
		}
	}

	@Test
	public void counts() throws Exception {
		try (final TestWriter out = new TestWriter("text");) {

			final InclusionRule schemaInclusionRule = new RegularExpressionInclusionRule("91118net");

			final LimitOptionsBuilder limitOptionsBuilder = LimitOptionsBuilder.builder()
					// Set what details are required in the schema - this affects the
					.includeSchemas(schemaInclusionRule)
					.includeTables(new IncludeAll())
					.tableTypes("TABLE", "VIEW");

			// Create the options
			final SchemaCrawlerOptions options = SchemaCrawlerOptionBuilder
					.maximum()
					.withLimitOptions(limitOptionsBuilder.toOptions());

			// Get the schema definition
			final Catalog catalog = getCatalog("sa", "sa", options);
			final Schema[] schemas = catalog.getSchemas().toArray(new Schema[0]);
			assertEquals("Schema count does not match", 5, schemas.length);
			for (final Schema schema : schemas) {
				out.println("schema: " + schema.getFullName());
				final Table[] tables = catalog.getTables(schema).toArray(new Table[0]);
				Arrays.sort(tables, NamedObjectSort.alphabetical);
				for (final Table table : tables) {
					out.println("  table: " + table.getFullName());
					out.println("    # columns: " + table.getColumns().size());
					out.println("    # constraints: " + table.getTableConstraints().size());
					out.println("    # indexes: " + table.getIndexes().size());
					out.println("    # foreign keys: " + table.getForeignKeys().size());
					out.println("    # imported foreign keys: " + table.getExportedForeignKeys().size());
					out.println("    # exported: " + table.getImportedForeignKeys().size());
					out.println("    # privileges: " + table.getPrivileges().size());
				}
			}

			out.assertEquals(testName.currentMethodFullName());
		}
	}

	@Test
	public void relatedTables() throws Exception {
		try (final TestWriter out = new TestWriter("text");) {
			final InclusionRule schemaInclusionRule = new RegularExpressionInclusionRule("91118net");

			final LimitOptionsBuilder limitOptionsBuilder = LimitOptionsBuilder.builder()
					// Set what details are required in the schema - this affects the
					.includeSchemas(schemaInclusionRule)
					.includeTables(new IncludeAll())
					.tableTypes("TABLE", "VIEW");

			// Create the options
			final SchemaCrawlerOptions options = SchemaCrawlerOptionBuilder
					.standard()
					.withLimitOptions(limitOptionsBuilder.toOptions());

			// Get the schema definition
			final Catalog catalog = getCatalog("sa", "sa", options);

			final Table[] tables = catalog.getTables().toArray(new Table[0]);
			assertEquals("Table count does not match", 13, tables.length);
			Arrays.sort(tables, NamedObjectSort.alphabetical);
			for (final Table table : tables) {
				out.println("  table: " + table.getFullName());
				out.println("    # columns: " + table.getColumns().size());
				out.println("    # child tables: " + table.getRelatedTables(TableRelationshipType.child));
				out.println("    # parent tables: " + table.getRelatedTables(TableRelationshipType.parent));
			}

			out.assertEquals(testName.currentMethodFullName());
		}
	}

	@Test
	public void relatedTablesWithTableRestriction() throws Exception {
		try (final TestWriter out = new TestWriter("text");) {
			final InclusionRule schemaInclusionRule = new RegularExpressionInclusionRule("91118net");

			final LimitOptionsBuilder limitOptionsBuilder = LimitOptionsBuilder.builder()
					// Set what details are required in the schema - this affects the
					.includeSchemas(schemaInclusionRule)
					.includeTables(new IncludeAll())
					.tableTypes("TABLE", "VIEW");

			// Create the options
			final SchemaCrawlerOptions options = SchemaCrawlerOptionBuilder
					.standard()
					.withLimitOptions(limitOptionsBuilder.toOptions());

			// Get the schema definition
			final Catalog catalog = getCatalog("sa", "sa", options);
			final Table[] tables = catalog.getTables().toArray(new Table[0]);
			assertEquals("Table count does not match", 1, tables.length);
			Arrays.sort(tables, NamedObjectSort.alphabetical);
			for (final Table table : tables) {
				out.println("  table: " + table.getFullName());
				out.println("    # columns: " + table.getColumns().size());
				out.println("    # child tables: " + table.getRelatedTables(TableRelationshipType.child));
				out.println("    # parent tables: " + table.getRelatedTables(TableRelationshipType.parent));
			}

			out.assertEquals(testName.currentMethodFullName());
		}
	}

	@Test
	public void routineDefinitions() throws Exception {

		final InclusionRule schemaInclusionRule = new RegularExpressionInclusionRule("91118net");

		final LimitOptionsBuilder limitOptionsBuilder = LimitOptionsBuilder.builder()
				// Set what details are required in the schema - this affects the
				.includeSchemas(schemaInclusionRule)
				.includeTables(new IncludeAll())
				.tableTypes("TABLE", "VIEW");

		// Create the options
		final SchemaCrawlerOptions options = SchemaCrawlerOptionBuilder
				.maximum()
				.withLimitOptions(limitOptionsBuilder.toOptions());

		// Get the schema definition
		final Catalog catalog = getCatalog("sa", "sa", options);

		final Schema schema = new SchemaReference("PUBLIC", "BOOKS");
		final Routine[] routines = catalog.getRoutines(schema).toArray(new Routine[0]);
		assertEquals("Wrong number of routines", 4, routines.length);
		for (final Routine routine : routines) {
			assertFalse("Routine definition not found, for " + routine, !StringUtils.hasText(routine.getDefinition()));
		}
	}

	@Test
	public void schemaEquals() throws Exception {

		final InclusionRule schemaInclusionRule = new RegularExpressionInclusionRule("91118net");
		
		final LimitOptionsBuilder limitOptionsBuilder = LimitOptionsBuilder.builder()
				// Set what details are required in the schema - this affects the
				.includeSchemas(schemaInclusionRule)
				.includeTables(new IncludeAll())
				.tableTypes("TABLE", "VIEW");

		// Create the options
		final SchemaCrawlerOptions options = SchemaCrawlerOptionBuilder
				.detailed()
				.withLimitOptions(limitOptionsBuilder.toOptions());
		
		// Get the schema definition
		final Catalog catalog = getCatalog("sa", "sa", options);

		final Schema schema1 = new SchemaReference("PUBLIC", "BOOKS");
		assertTrue("Could not find any tables", catalog.getTables(schema1).size() > 0);
		assertEquals("Wrong number of routines", 4, catalog.getRoutines(schema1).size());

		final Schema schema2 = new SchemaReference("PUBLIC", "BOOKS");

		assertEquals("Schema not not match", schema1, schema2);
		assertEquals("Tables do not match", catalog.getTables(schema1), catalog.getTables(schema2));
		assertEquals("Routines do not match", catalog.getRoutines(schema1), catalog.getRoutines(schema2));

		// Try negative test
		final Table table1 = catalog.getTables(schema1).toArray(new Table[0])[0];
		final Table table2 = catalog.getTables(schema1).toArray(new Table[0])[1];
		assertFalse("Tables should not be equal", table1.equals(table2));

	}

	@Test
	public void sequences() throws Exception {
		try (final TestWriter out = new TestWriter("text");) {

			final SchemaInfoLevel minimum = SchemaInfoLevelBuilder.minimum();

			final LimitOptionsBuilder limitOptionsBuilder = LimitOptionsBuilder.builder()
					// Set what details are required in the schema - this affects the
					.includeTables(new IncludeAll())
					.tableTypes("TABLE", "VIEW");

			// Create the options
			final SchemaCrawlerOptions options = SchemaCrawlerOptionBuilder
					.custom(minimum)
					.withLimitOptions(limitOptionsBuilder.toOptions());
			
			// Get the schema definition
			final Catalog catalog = getCatalog("sa", "sa", options);
			final Schema schema = catalog.lookupSchema("PUBLIC.BOOKS").get();
			assertNotNull("BOOKS Schema not found", schema);
			final Sequence[] sequences = catalog.getSequences(schema).toArray(new Sequence[0]);
			assertEquals("Sequence count does not match", 1, sequences.length);
			for (final Sequence sequence : sequences) {
				assertNotNull(sequence);
				out.println("sequence: " + sequence.getName());
				out.println("  increment: " + sequence.getIncrement());
				out.println("  minimum value: " + sequence.getMinimumValue());
				out.println("  maximum value: " + sequence.getMaximumValue());
				out.println("  cycle?: " + sequence.isCycle());
			}

			out.assertEquals(testName.currentMethodFullName());
		}
	}

	@Test
	public void synonyms() throws Exception {
		try (final TestWriter out = new TestWriter("text");) {

			final SchemaInfoLevel minimum = SchemaInfoLevelBuilder.minimum();

			final LimitOptionsBuilder limitOptionsBuilder = LimitOptionsBuilder.builder()
					// Set what details are required in the schema - this affects the
					.includeTables(new IncludeAll())
					.tableTypes("TABLE", "VIEW");

			// Create the options
			final SchemaCrawlerOptions options = SchemaCrawlerOptionBuilder
					.custom(minimum)
					.withLimitOptions(limitOptionsBuilder.toOptions());
				
			// Get the schema definition
			final Catalog catalog = getCatalog("sa", "sa", options);
			final Schema schema = catalog.lookupSchema("PUBLIC.BOOKS").get();
			assertNotNull("BOOKS Schema not found", schema);
			final Synonym[] synonyms = catalog.getSynonyms(schema).toArray(new Synonym[0]);
			assertEquals("Synonym count does not match", 1, synonyms.length);
			for (final Synonym synonym : synonyms) {
				assertNotNull(synonym);
				out.println("synonym: " + synonym.getName());
				out.println("  class: " + synonym.getReferencedObject().getClass().getSimpleName());
			}

			out.assertEquals(testName.currentMethodFullName());
		}
	}

	@Test
	public void tableConstraints() throws Exception {
		try (final TestWriter out = new TestWriter("text");) {
			
			final LimitOptionsBuilder limitOptionsBuilder = LimitOptionsBuilder.builder()
					// Set what details are required in the schema - this affects the
					.includeTables(new IncludeAll())
					.tableTypes("TABLE", "VIEW");

			// Create the options
			final SchemaCrawlerOptions options = SchemaCrawlerOptionBuilder
					.custom(SchemaInfoLevelBuilder.maximum())
					.withLimitOptions(limitOptionsBuilder.toOptions());

			// Get the schema definition
			final Catalog catalog = getCatalog("sa", "sa", options);

			final Schema[] schemas = catalog.getSchemas().toArray(new Schema[0]);
			assertEquals("Schema count does not match", 6, schemas.length);
			for (final Schema schema : schemas) {
				out.println("schema: " + schema.getFullName());
				final Table[] tables = catalog.getTables(schema).toArray(new Table[0]);
				for (final Table table : tables) {
					out.println("  table: " + table.getFullName());
					final TableConstraint[] tableConstraints = table.getTableConstraints().toArray(new TableConstraint[0]);
					for (final TableConstraint tableConstraint : tableConstraints) {
						out.println("    constraint: " + tableConstraint.getName());
						out.println("      type: " + tableConstraint.getType());
						if (tableConstraint instanceof TableConstraint) {
							final TableConstraint dependentTableConstraint = (TableConstraint) tableConstraint;
							final List<TableConstraintColumn> columns = dependentTableConstraint.getColumns();
							for (final TableConstraintColumn tableConstraintColumn : columns) {
								out.println("      on column: " + tableConstraintColumn.getName());
							}
						}
					}
				}
			}

			out.assertEquals(testName.currentMethodFullName());
		}
	}

	@Test
	public void tables() throws Exception {
		try (final TestWriter out = new TestWriter("text");) {
			
			final LimitOptionsBuilder limitOptionsBuilder = LimitOptionsBuilder.builder()
					// Set what details are required in the schema - this affects the
					.includeSchemas(new RegularExpressionExclusionRule(".*\\.FOR_LINT"))
					.includeTables(new IncludeAll())
					.tableTypes("TABLE", "VIEW");

			// Create the options
			final SchemaCrawlerOptions options = SchemaCrawlerOptionBuilder
					.custom(SchemaInfoLevelBuilder.maximum())
					.withLimitOptions(limitOptionsBuilder.toOptions());
			
			// Get the schema definition
			final Catalog catalog = getCatalog("sa", "sa", options);

			final Schema[] schemas = catalog.getSchemas().toArray(new Schema[0]);
			assertEquals("Schema count does not match", 5, schemas.length);
			for (final Schema schema : schemas) {
				final Table[] tables = catalog.getTables(schema).toArray(new Table[0]);
				Arrays.sort(tables, NamedObjectSort.alphabetical);
				for (final Table table : tables) {
					out.println(String.format("o--> %s [%s]", table.getFullName(), table.getTableType()));
					final SortedMap<String, Object> tableAttributes = new TreeMap<>(table.getAttributes());
					for (final Entry<String, Object> tableAttribute : tableAttributes.entrySet()) {
						out.println(String.format("      ~ %s=%s", tableAttribute.getKey(), tableAttribute.getValue()));
					}
				}
			}

			out.assertEquals(testName.currentMethodFullName());
		}
	}

	@Test
	public void tablesSort() throws Exception {

		final String[] tableNames = { "AUTHORS", "BOOKS", "COUPONS", "CUSTOMERDATA", "CUSTOMERS", "Global Counts",
				"PUBLISHERS", "BOOKAUTHORS", "ΒΙΒΛΊΑ", "AUTHORSLIST" };
		final Random rnd = new Random();

		final LimitOptionsBuilder limitOptionsBuilder = LimitOptionsBuilder.builder()
				// Set what details are required in the schema - this affects the
				.includeTables(new IncludeAll())
				.tableTypes("TABLE", "VIEW");

		// Create the options
		final SchemaCrawlerOptions options = SchemaCrawlerOptionBuilder
				.custom(SchemaInfoLevelBuilder.maximum())
				.withLimitOptions(limitOptionsBuilder.toOptions());
		
		// Get the schema definition
		final Catalog catalog = getCatalog("sa", "sa", options);
		final Schema[] schemas = catalog.getSchemas().toArray(new Schema[0]);
		assertEquals("Schema count does not match", 5, schemas.length);
		final Schema schema = schemas[0];

		for (int i = 0; i < tableNames.length; i++) {
			final String tableName1 = tableNames[i];
			for (int j = 0; j < tableNames.length; j++) {
				final String tableName2 = tableNames[j];
				assertEquals(tableName1 + " <--> " + tableName2,
						Math.signum(catalog.lookupTable(schema, tableName1).orElse(null)
								.compareTo(catalog.lookupTable(schema, tableName2).orElse(null))),
						Math.signum(i - j), 1e-100);
			}
		}

		final Table[] tables = catalog.getTables(schema).toArray(new Table[0]);
		for (int i = 0; i < 10; i++) {
			for (int tableIdx = 0; tableIdx < tables.length; tableIdx++) {
				final Table table = tables[tableIdx];
				assertEquals("Table name does not match in iteration " + i, tableNames[tableIdx], table.getName());
			}

			// Shuffle array, and sort it again
			for (int k = tables.length; k > 1; k--) {
				final int i1 = k - 1;
				final int i2 = rnd.nextInt(k);
				final Table tmp = tables[i1];
				tables[i1] = tables[i2];
				tables[i2] = tmp;
			}
			Arrays.sort(tables);
		}
	}

	@Test
	public void triggers() throws Exception {
		
		final LimitOptionsBuilder limitOptionsBuilder = LimitOptionsBuilder.builder()
				// Set what details are required in the schema - this affects the
				.includeTables(new IncludeAll())
				.tableTypes("TABLE", "VIEW");

		// Create the options
		final SchemaCrawlerOptions options = SchemaCrawlerOptionBuilder
				.custom(SchemaInfoLevelBuilder.maximum())
				.withLimitOptions(limitOptionsBuilder.toOptions());
		
		// Get the schema definition
		final Catalog catalog = getCatalog("sa", "sa", options);
		final Schema schema = new SchemaReference("PUBLIC", "BOOKS");
		final Table[] tables = catalog.getTables(schema).toArray(new Table[0]);
		boolean foundTrigger = false;
		for (final Table table : tables) {
			for (final Trigger trigger : table.getTriggers()) {
				foundTrigger = true;
				assertEquals("Triggers full name does not match", "PUBLIC.BOOKS.AUTHORS.TRG_AUTHORS",
						trigger.getFullName());
				assertEquals("Trigger EventManipulationType does not match", EventManipulationType.delete,
						trigger.getEventManipulationType());
			}
		}
		assertTrue("No triggers found", foundTrigger);
	}

	@Test
	public void viewDefinitions() throws Exception {
		
		final LimitOptionsBuilder limitOptionsBuilder = LimitOptionsBuilder.builder()
				// Set what details are required in the schema - this affects the
				.includeTables(new IncludeAll())
				.tableTypes("VIEW");

		// Create the options
		final SchemaCrawlerOptions options = SchemaCrawlerOptionBuilder
				.custom(SchemaInfoLevelBuilder.maximum())
				.withLimitOptions(limitOptionsBuilder.toOptions());

		// Get the schema definition
		final Catalog catalog = getCatalog("sa", "sa", options);

		final Schema schema = new SchemaReference("PUBLIC", "BOOKS");
		final View view = (View) catalog.lookupTable(schema, "AUTHORSLIST").get();
		assertNotNull("View not found", view);
		assertNotNull("View definition not found", view.getDefinition());
		assertFalse("View definition not found", view.getDefinition().trim().equals(""));
	}

	private String printColumnDataType(final ColumnDataType columnDataType) {
		final StringBuffer buffer = new StringBuffer();

		final boolean isUserDefined = columnDataType.getType() == DataTypeType.user_defined;
		final String typeName = columnDataType.getFullName();
		final String dataType = (isUserDefined ? "user defined " : "") + "column data-type";
		final String nullable = (columnDataType.isNullable() ? "" : "not ") + "nullable";
		final String autoIncrementable = (columnDataType.isAutoIncrementable() ? "" : "not ") + "auto-incrementable";

		final String createParameters = columnDataType.getCreateParameters();
		final String definedWith = "defined with " + (!StringUtils.hasText(createParameters) ? "no parameters" : createParameters);

		final String literalPrefix = columnDataType.getLiteralPrefix();
		final String literalPrefixText = (!StringUtils.hasText(literalPrefix) ? "no literal prefix"
				: "literal prefix " + literalPrefix);

		final String literalSuffix = columnDataType.getLiteralSuffix();
		final String literalSuffixText = (!StringUtils.hasText(literalSuffix) ? "no literal suffix"
				: "literal suffix " + literalSuffix);

		final String javaSqlType = "java.sql.Types: " + columnDataType.getJavaSqlType().getName();

		final String precision = "precision " + columnDataType.getPrecision();
		final String minimumScale = "minimum scale " + columnDataType.getMinimumScale();
		final String maximumScale = "maximum scale " + columnDataType.getMaximumScale();

		buffer.append(typeName).append("\n").append("  ").append(dataType).append("\n").append("  ").append(definedWith)
				.append("\n").append("  ").append(nullable).append("\n").append("  ").append(autoIncrementable)
				.append("\n").append("  ").append(literalPrefixText).append("\n").append("  ").append(literalSuffixText)
				.append("\n").append("  ").append(columnDataType.getSearchable().toString()).append("\n").append("  ")
				.append(precision).append("\n").append("  ").append(minimumScale).append("\n").append("  ")
				.append(maximumScale).append("\n").append("  ").append(javaSqlType);
		if (isUserDefined) {
			final String baseTypeName;
			final ColumnDataType baseColumnDataType = columnDataType.getBaseType();
			if (baseColumnDataType == null) {
				baseTypeName = "";
			} else {
				baseTypeName = baseColumnDataType.getFullName();
			}
			buffer.append("\n").append("  ").append("based on ").append(baseTypeName);
		}

		return buffer.toString();
	}

}
