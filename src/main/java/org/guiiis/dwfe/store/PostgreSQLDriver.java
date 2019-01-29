package org.guiiis.dwfe.store;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lirmm.graphik.graal.store.rdbms.driver.AbstractInsertOrIgnoreRdbmsDriver;

public class PostgreSQLDriver extends AbstractInsertOrIgnoreRdbmsDriver {
	private static final Logger LOGGER = LoggerFactory
            .getLogger(PostgreSQLDriver.class);
	
	private static final String INSERT_IGNORE = "INSERT OR IGNORE INTO ";
	
	 /**
	  * 
	  * @param file
	  * @throws SQLException
	  */
	public PostgreSQLDriver(File file)
	    throws SQLException {
		super(openConnection(file), INSERT_IGNORE);
	}

	public PostgreSQLDriver(String filePath) throws SQLException {
		super(openConnection(new File(filePath)), INSERT_IGNORE);
	}
	 
	private static Connection openConnection(File file) throws SQLException {
		Connection connection;
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			LOGGER.error(e.getMessage(), e);
			throw new SQLException(e.getMessage(), e);
		}
		
		connection = DriverManager.getConnection("jdbc:postgresql:" + file);
		return connection;
	}
}
