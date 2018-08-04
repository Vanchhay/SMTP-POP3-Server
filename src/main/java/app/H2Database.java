package app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;

public class H2Database {

	private final static Logger LOGGER = LoggerFactory.getLogger(H2Database.class);

	static final String JDBC_DRIVER = "org.h2.Driver";
	static final String DB_URL = "jdbc:h2:tcp://localhost/~/test";

	//  Database credentials
	static final String USER = "admin";
	static final String PASS = "admin123";

	public Connection connection = null;

	public Connection getConnection(){

		try{
			Class.forName(JDBC_DRIVER);
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
		}catch(Exception e){
			LOGGER.info("Exception H2 : " + e.getMessage());
		}
		LOGGER.info("H2 CONNECTION SUCCESS");
		return connection;
	}
}
