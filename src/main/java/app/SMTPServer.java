package app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

class SMTPServer {
	private final static Logger LOGGER = LoggerFactory.getLogger(SMTPServer.class);

	static {
		Connection conn = null;
		Statement stmt = null;
		try{
			conn = new H2Database().getConnection();
			stmt = conn.createStatement();

			String table = "CREATE TABLE IF NOT EXISTS MAIL(" +
					"ID BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT," +
					"UID VARCHAR(225) UNIQUE NOT NULL," +
					"HEADER TEXT NOT NULL," +
					"SUBJECT VARCHAR(50)," +
					"MESSAGE TEXT," +
					"MAIL_FROM VARCHAR(50) NOT NULL," +
					"MAIL_TO VARCHAR(50) NOT NULL," +
					"CREATE_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
					")";
			stmt.executeUpdate(table);

			stmt.close();
			conn.close();

		}catch(Exception e){
			LOGGER.info("CREATE TABLE EXCEPTION : " + e.getMessage());
		}finally {
			try {
				if(!stmt.isClosed()) {
					LOGGER.info("CREATE TABLE FINALLY stmt(!null) => close");
					stmt.close();
				}
			} catch(SQLException se2) {
				LOGGER.info("CREATE TABLE FINALLY stmt(!null) [SQLException] =>  " + se2.getMessage());
			}
			try {
				if(!conn.isClosed()) {
					LOGGER.info("CREATE TABLE FINALLY conn(!null) => close");
					conn.close();
				}
			} catch(SQLException se) {
				LOGGER.info("CREATE TABLE FINALLY conn(!null) [SQLException] =>  " + se.getMessage());
			}
		}
	}

	public static void main(String argv[]) {
		ServerSocket server;
		try{
			server = new ServerSocket(465);
			Socket client;
			while(true){
				client = server.accept();
				if(client.isConnected()) {
					SMTPThread st = new SMTPThread(server, client);
					LOGGER.info("New Client Connected on Thread : " + st);
					st.start();
				}
			}

		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
