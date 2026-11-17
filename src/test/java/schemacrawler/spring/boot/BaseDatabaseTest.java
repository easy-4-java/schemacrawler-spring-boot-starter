package schemacrawler.spring.boot;

import java.sql.Connection;
import java.sql.SQLException;

import schemacrawler.inclusionrule.IncludeAll;
import schemacrawler.inclusionrule.InclusionRule;
import schemacrawler.schema.Catalog;
import schemacrawler.schemacrawler.exceptions.SchemaCrawlerException;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.spring.boot.utils.SchemaCrawlerOptionBuilder;
import schemacrawler.tools.databaseconnector.DatabaseConnectionSource;
import schemacrawler.tools.databaseconnector.SingleUseUserCredentials;
import schemacrawler.tools.utility.SchemaCrawlerUtility;

public abstract class BaseDatabaseTest {

	protected DatabaseConnectionSource dataSource;

	protected void setUp(String connectionUrl) throws Exception {
		// Create a DataSource
		dataSource = new DatabaseConnectionSource(connectionUrl);
	}

	protected Connection getConnection(String username, String password) throws SchemaCrawlerException, SQLException {
		// Create a database connection
		dataSource.setUserCredentials(new SingleUseUserCredentials(username, password));

		final Connection connection = dataSource.get();
		return connection;
	}

	protected Catalog getCatalog(String username, String password, InclusionRule schemaInclusionRule) throws Exception {

		final SchemaCrawlerOptions options = SchemaCrawlerOptionBuilder.tablecolumns(new IncludeAll(), "TABLE", "VIEW");

		// Get the schema definition
		final Catalog catalog = SchemaCrawlerUtility.getCatalog(getConnection(username, password), options);
		return catalog;
	}

	protected Catalog getCatalog(String username, String password, SchemaCrawlerOptions options)
			throws SchemaCrawlerException, SQLException {
		// Get the schema definition
		return SchemaCrawlerUtility.getCatalog(getConnection(username, password), options);
	}

}
