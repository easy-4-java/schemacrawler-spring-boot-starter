package schemacrawler.spring.boot;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import schemacrawler.inclusionrule.IncludeAll;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.exceptions.SchemaCrawlerException;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.spring.boot.utils.SchemaCrawlerOptionBuilder;
import schemacrawler.tools.utility.SchemaCrawlerUtility;

public class CatalogTest {
	
	public Catalog getCatalog(final Connection connection) throws SQLException, SchemaCrawlerException {
	
		final LimitOptionsBuilder limitOptionsBuilder = LimitOptionsBuilder.builder()
				 	.includeTables(new IncludeAll())
					.tableNamePattern("*");
			
		final SchemaCrawlerOptions options = SchemaCrawlerOptionBuilder
				.custom(SchemaInfoLevelBuilder.standard())
				.withLimitOptions(limitOptionsBuilder.toOptions());
			
		final Catalog catalog = SchemaCrawlerUtility.getCatalog(connection, options);

		for (final Schema schema : catalog.getSchemas()) {
			Collection<Table> tables = catalog.getTables(schema);
			//
			// The size of tables is always 0
			//
			System.out.println(tables);
		}

		return catalog;

	}

}
