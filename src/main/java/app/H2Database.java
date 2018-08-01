package app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class H2Database {

	private final static Logger LOGGER = LoggerFactory.getLogger(POP3Server.class);

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
		}catch(ClassNotFoundException e){
			e.printStackTrace();

		}catch(SQLException e){
			e.printStackTrace();
		}
		LOGGER.info("H2 CONNECTION SUCCESS");
		return connection;
	}
}
