package app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SMTPThread extends Thread {

	protected final static String CRLF = "\r\n";
	private final static Logger LOGGER = LoggerFactory.getLogger(SMTPThread.class);

	public Envelope email = null;
	public ServerSocket server;
	public Socket client;
	List<String> mailToList = new ArrayList<>();

	public SMTPThread(ServerSocket server, Socket client) {
		this.server = server;
		this.client = client;
	}

	@Override
	public void run() {

		String command;

		try {
			BufferedReader inFromClient = null ;
			DataOutputStream outToClient = null;

			if (client.isConnected()) {
				inFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
				outToClient = new DataOutputStream(client.getOutputStream());
				outToClient.writeBytes("220" + CRLF);
				LOGGER.info("GREETING RESPONSED");
			}

			while (!client.isClosed()) {
				command = inFromClient.readLine();

				// Spliting command
				String splitedCommand[];
				if (command.startsWith("HELO") || command.startsWith("EHLO")) {
					splitedCommand = command.split(" ",2);
				} else {
					splitedCommand = command.split(":",2);
				}
				switch (splitedCommand[0].trim().toUpperCase()) {
					case "HELO":
						outToClient.writeBytes("250 Hello " + CRLF);
						LOGGER.info(this.getName() + " HELO 250 Hello");
						break;
					case "EHLO":
						outToClient.writeBytes("250 Hello " + CRLF);
						LOGGER.info(this.getName() + " EHLO 250 Hello");
						break;
					case "MAIL FROM":
						email = new Envelope();

						String mailFrom = extractEmail(splitedCommand[1]);
						this.setName(getHostOfEmail(mailFrom));
						if (isValidEmail(mailFrom)) {
							email.setMailFrom(mailFrom);
							email.setStatus(email.MAIL_FROM);
							outToClient.writeBytes("250 <" + mailFrom + "> Accepted." + CRLF);
							LOGGER.info(this.getName() + " MAIL_FROM 250 <" + mailFrom + "> Accepted.");
						} else {
							outToClient.writeBytes("421 Service not available, closing transmission channel" + CRLF);
							LOGGER.info(this.getName() + " MAIL_FROM 421 Service not available, closing transmission channel");
						}
						break;
					case "RCPT TO":
						if (email == null) {
							outToClient.writeBytes("Email null, MAIL FROM Command is missing." + CRLF);
							LOGGER.info(this.getName() + " RCPT_TO Email null, MAIL FROM Command is missing.");
							break;
						} else {
							if ((!(email.getStatus().equalsIgnoreCase(email.MAIL_FROM))) && !(email.getStatus().equalsIgnoreCase(email.RCPT_TO))) {
								outToClient.writeBytes("500 Syntax error, command unrecognised. Did you forget MAIL FROM Command." + CRLF);
								LOGGER.info(this.getName() + " RCPT_TO 500 Syntax error, command unrecognised. Did you forget MAIL FROM Command.");
								break;
							}
							if (((email.getStatus().equalsIgnoreCase(email.MAIL_FROM)))) {
								email.setStatus(email.RCPT_TO);
							}
							String mailTo = extractEmail(splitedCommand[1]);
							if (isValidEmail(mailTo)) {
								mailToList.add(mailTo);
								outToClient.writeBytes("250 <" + mailTo + "> Accepted" + CRLF);
								LOGGER.info(this.getName() + " RCPT_TO 250 <" + mailTo + "> Accepted");
							} else {
								outToClient.writeBytes("421 Service not available, closing transmission channel." + CRLF);
								LOGGER.info(this.getName() + " RCPT_TO 421 Service not available, closing transmission channel.");
							}
						}
						email.setMailTo(mailToList);
						break;
					case "DATA":
						if (email == null) {
							outToClient.writeBytes("Email null, MAIL FROM Command is missing." + CRLF);
							LOGGER.info(this.getName() + " DATA Email null, MAIL FROM Command is missing.");
							break;
						}
						if (!(email.getStatus().equalsIgnoreCase(email.RCPT_TO))) {
							outToClient.writeBytes("500 Syntax error, command unrecognised." + CRLF);
							LOGGER.info(this.getName() + " DATA 500 Syntax error, command unrecognised.");
							break;
						}
						outToClient.writeBytes("354 Start mail input; end with <CRLF>.<CRLF>" + CRLF);
						LOGGER.info(this.getName() + " DATA 354 Start mail input; end with <CRLF>.<CRLF>");

						// After get the message then send
						String data;
						boolean startMessage = false;

						while (true) {
							email.setStatus(email.DATA);
							data = inFromClient.readLine().trim();
							/**
							 * when startMessage true then only record new
							 */

							if (data.getBytes().length == 0) {
								if (!startMessage) {
									startMessage = true;
									continue;
								}
							}
							if (!data.isEmpty()) {
								if (startMessage) {
									if (data.trim().startsWith(".")) {
										if (addToH2(email)) {
											email.setStatus(email.SENT);
											outToClient.writeBytes("250 Email has successfully sent." + CRLF);
											LOGGER.info(this.getName() + " DATA_MESSAGE ADDED TO H2");
											break;
										} else {
											outToClient.writeBytes("552 Transaction Fail." + CRLF);
											LOGGER.info(this.getName() + " DATA_MESSAGE 552 Transaction Fail.");
											client.close();
											break;
										}
									}
									email.setMessage(email.getMessage().concat(data + '\n'));
									continue;
								} else {
									email.setHeader(email.getHeader().concat(data + '\n'));
									if (data.startsWith("Subject")) {
										email.setSubject(data.substring(9));
										continue;
									}
									continue;
								}
							}
						}
						break;
					case "QUIT":
						outToClient.writeBytes("221 Bye ");
						client.close();
						LOGGER.info(this.getName()+ " QUIT 221 Bye ");
						break;
					default:
						outToClient.writeBytes("Please try again! " + CRLF);
						LOGGER.info(this.getName() + " DEFAULT UNDEFINDED COMMAND +OK");
						break;
				}
			}
		} catch(Exception e){
			LOGGER.info(this.getName() +"  "+ e.getClass() + " === " + e.getMessage());
		} finally {
			try{
				if(client != null) {
					client.close();
					LOGGER.info(this.getName() + " FINALLY Closed");
				}
			}catch(IOException e){
				LOGGER.info(this.getName() +"  "+ e.getMessage());
			}
		}
	}

	public boolean isValidEmail(String email){

		String [] hostDomain = email.split("@",0);
		hostDomain = hostDomain[1].split("[.]",0);

		final String domain = hostDomain[0].trim();

		String[] supportedDomain = {"gmail", "yahoo"};
		for (String support : supportedDomain) {
			if(support.equalsIgnoreCase(domain)){
				return true;
			}
		}
		return true;
	}

	public String extractEmail(String unExtractMail){
		return unExtractMail.replaceAll("[<,>]", "").trim();
	}

	public boolean addToH2(Envelope email){
		Connection conn = null;
		Statement stmt = null;

		try{
			conn = new H2Database().getConnection();
			stmt = conn.createStatement();

			String table = "CREATE TABLE IF NOT EXISTS MAIL(" +
					"ID BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT," +
					"UID VARCHAR(225) NOT NULL," +
					"HEADER TEXT NOT NULL," +
					"SUBJECT VARCHAR(50)," +
					"MESSAGE TEXT," +
					"MAIL_FROM VARCHAR(50) NOT NULL," +
					"MAIL_TO VARCHAR(50) NOT NULL," +
					"CREATE_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
					")";
			stmt.executeUpdate(table);

			// Execute a query
			for (String mailTo : email.getMailTo()) {
				email.setUid(genKey());
				String insert = "INSERT INTO mail VALUES (NULL, " +
						"'" + email.getUid() + "'," +
						"'" + email.getHeader() + "'," +
						"'" + email.getSubject() + "'," +
						"'" + email.getMessage() + "', " +
						"'" + email.getMailFrom() + "', " +
						"'" + mailTo + "', " +
						"current_timestamp()" +
						")";
				LOGGER.info("==== INSERT STATEMENT: " +insert);
				stmt.executeUpdate(insert);
				LOGGER.info("==== STATEMENT INSERTED" );
			}

			stmt.close();
			conn.close();
		} catch(SQLException se) {
			LOGGER.info("SQLException : " + se.getMessage());
			return false;
		} catch(Exception e) {
			LOGGER.info("Exception : " + e.getMessage());
			return false;
		} finally {
			try {
				if(stmt!=null) {
					LOGGER.info("FINALLY stmt(!null) => close");
					stmt.close();
				}
			} catch(SQLException se2) {
				LOGGER.info("FINALLY stmt(!null) [SQLException] =>  " + se2.getMessage());
			}
			try {
				if(conn!=null) {
					LOGGER.info("FINALLY conn(!null) => close");
					conn.close();
				}
			} catch(SQLException se) {
				LOGGER.info("FINALLY conn(!null) [SQLException] =>  " + se.getMessage());
			}
		}
		return true;
	}

	public String getHostOfEmail(String email){
		int chAtIndex = email.indexOf("@");
		return email.substring(0, chAtIndex);
	}

	public String genKey(){
		int leftLimit = 33;
		int rightLimit = 126;
		int targetStringLength = 30;

		Random random = new Random();
		StringBuilder buffer = new StringBuilder(targetStringLength);

		for (int i = 0; i < targetStringLength; i++) {
			int randomLimitedInt = leftLimit + (int)
					(random.nextFloat() * (rightLimit - leftLimit + 1));
			buffer.append((char) randomLimitedInt);
		}
		return buffer.toString();
	}

}