/*
 * Copyright (c) 2018, hiwepy (https://github.com/hiwepy).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package schemacrawler.spring.boot;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import schemacrawler.inclusionrule.InclusionRule;
import schemacrawler.inclusionrule.RegularExpressionInclusionRule;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.exceptions.SchemaCrawlerException;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.spring.boot.utils.SchemaCrawlerOptionBuilder;
import schemacrawler.tools.utility.SchemaCrawlerUtility;

/**
 * TODO
 * 
 * @author ： <a href="https://github.com/hiwepy">hiwepy</a>
 */
public class ExtractTableTest extends BaseDatabaseTest {

	@Before
	public void setUp() throws Exception {
		// Create a database connection
		super.setUp("jdbc:sqlserver://192.168.0.118:1433;DatabaseName=91118net;integratedSecurity=false");
	}

	@Test
	public void extractTables() throws SchemaCrawlerException, SQLException {

		final Connection connection = dataSource.get();

		System.out.println("my catalog =" + connection.getCatalog());
		final String schemaName = "91118net";

		final InclusionRule schemaInclusionRule = new RegularExpressionInclusionRule(schemaName + ".*");

		
		final LimitOptionsBuilder limitOptionsBuilder = LimitOptionsBuilder.builder()
				// Set what details are required in the schema - this affects the
				.includeSchemas(schemaInclusionRule);

		// Create the options
		final SchemaCrawlerOptions options = SchemaCrawlerOptionBuilder
				.standard()
				.withLimitOptions(limitOptionsBuilder.toOptions());
		
		final Catalog catalog = SchemaCrawlerUtility.getCatalog(connection, options);
		System.out.println("schem ! ");

		final Optional<? extends Schema> schema = catalog.lookupSchema(schemaName);
		System.out.println("Schema: " + schema);

		for (final Table table : catalog.getTables()) {
			String tableName = table.getName();

			System.out.println(table + " pk " + table.getPrimaryKey() + " fks " + table.getForeignKeys() + " type "
					+ table.getTableType());
			if (/* rules.skipTable(tableName) || */ table.getTableType().isView()) {
				System.out.println("SKIPPED");
				continue;
			}
			List<Column> columns = table.getColumns();
			List<String> fields = new ArrayList<>(columns.size());
			for (final Column column : columns) {
				// System.out.println(" o--> " + column + " pk: "+ column.isPartOfPrimaryKey() +
				// " fk: " + column.isPartOfForeignKey());
				String columnName = column.getName();
				if (column.isPartOfPrimaryKey() /* && rules.skipPrimaryKey(tableName, columnName) */) {
					// skip, todo strategy
				} else if (column.isPartOfForeignKey()) {
					// skip, todo strategy
				} else {
					fields.add(columnName);
				}
			}
		}

	}

}
