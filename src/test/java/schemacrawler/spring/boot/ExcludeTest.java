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

import java.util.Arrays;
import java.util.logging.Level;

import org.junit.Rule;
import org.junit.Test;

import java.util.logging.Logger;
import schemacrawler.inclusionrule.RegularExpressionExclusionRule;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.spring.boot.utility.TestName;
import schemacrawler.spring.boot.utility.TestWriter;
import schemacrawler.spring.boot.utils.SchemaCrawlerOptionBuilder;
import schemacrawler.utility.NamedObjectSort;

public class ExcludeTest extends BaseDatabaseTest {

	private static final Logger LOGGER = Logger.getLogger(ExcludeTest.class.getName());
// https://blog.csdn.net/qq_34337272/article/details/78815547
	@Rule
	public TestName testName = new TestName();

	@Test
	public void excludeColumns() throws Exception {
		try (final TestWriter out = new TestWriter("text");) {
			
			final LimitOptionsBuilder limitOptionsBuilder = LimitOptionsBuilder.builder()
					.includeSchemas(new RegularExpressionExclusionRule(".*\\.FOR_LINT"))
					.includeColumns(new RegularExpressionExclusionRule(".*\\..*\\.ID"));

			// Create the options
			final SchemaCrawlerOptions schemaCrawlerOptions = SchemaCrawlerOptionBuilder
					.standard()
					.withLimitOptions(limitOptionsBuilder.toOptions());

			final Catalog catalog = getCatalog("sa", "sa", schemaCrawlerOptions);
			final Schema[] schemas = catalog.getSchemas().toArray(new Schema[0]);
			assertEquals("Schema count does not match", 5, schemas.length);
			for (final Schema schema : schemas) {
				out.println("schema: " + schema.getFullName());
				final Table[] tables = catalog.getTables(schema).toArray(new Table[0]);
				Arrays.sort(tables, NamedObjectSort.alphabetical);
				for (final Table table : tables) {
					out.println("  table: " + table.getFullName());
					final Column[] columns = table.getColumns().toArray(new Column[0]);
					Arrays.sort(columns);
					for (final Column column : columns) {
						LOGGER.log(Level.FINE, column.toString());
						out.println("    column: " + column.getFullName());
						out.println("    database type: " + column.getColumnDataType().getDatabaseSpecificTypeName());
						out.println("    type: " + column.getColumnDataType().getJavaSqlType().getName());
					}
				}
			}

			out.assertEquals(testName.currentMethodFullName());
		}
	}

}
