package schemacrawler.spring.boot;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;

import schemacrawler.inclusionrule.ExcludeAll;
import schemacrawler.inclusionrule.IncludeAll;
import schemacrawler.inclusionrule.RegularExpressionInclusionRule;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schema.View;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaInfoLevel;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.spring.boot.utils.SchemaCrawlerOptionBuilder;
import schemacrawler.tools.databaseconnector.DatabaseConnectionSource;
import schemacrawler.tools.databaseconnector.SingleUseUserCredentials;
import schemacrawler.tools.utility.SchemaCrawlerUtility;

public final class ConnectionOptionsExample {

	public static void main(final String[] args) throws Exception {

		// Create a database connection
		final String connectionUrl = "jdbc:oracle:thin:@127.0.0.1:1521:orcl";
	    final DatabaseConnectionSource dataSource = new DatabaseConnectionSource(
	      connectionUrl, Collections.singletonMap("remarksReporting", "true"));
	    dataSource.setUserCredentials(new SingleUseUserCredentials("username", "password"));
		
		final Connection connection =  dataSource.get();

		// Set what details are required in the schema - this affects the
		// time taken to crawl the schema

		SchemaInfoLevel schemaInfoLevel = SchemaInfoLevelBuilder.detailed();

		final LimitOptionsBuilder limitOptionsBuilder = LimitOptionsBuilder.builder()
				.includeSchemas(new RegularExpressionInclusionRule("schemaName"))
				.tableTypes(Arrays.asList("TABLE","VIEW"))
				.includeRoutines(new ExcludeAll())
				.includeColumns(new IncludeAll());

		// Create the options
		final SchemaCrawlerOptions options = SchemaCrawlerOptionBuilder
				.custom(schemaInfoLevel)
				.withLimitOptions(limitOptionsBuilder.toOptions());

		// Get the schema definition
		final Catalog catalog = SchemaCrawlerUtility.getCatalog(connection, options);

		for (final Schema schema : catalog.getSchemas()) {
			System.out.println(schema);

			for (final Table table : catalog.getTables(schema)) {
				System.out.print("o--> " + table.getName());
				if (table instanceof View) {
					System.out.println(" (VIEW)");
				} else {
					System.out.println();
				}
				System.out.println(String.format("  Attrs [%s]",table.getAttributes()));
				System.out.println(table.getSchema().getRemarks());
				System.out.println(String.format("  %s [%s] [%s]", table.getName(), table.getTableType(), table.getRemarks()));
				final Column[] columns = table.getColumns().toArray(new Column[0]);
				Arrays.sort(columns);
				for (final Column column : columns) {
					System.out.println("     o--> " + column.getName() + " (" + column.getColumnDataType() + ") ："
							+ column.getRemarks());
				}

			}
		}

	}

}