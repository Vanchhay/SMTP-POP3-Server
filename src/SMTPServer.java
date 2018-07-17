import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.h2.expression.Function.CURRENT_TIMESTAMP;

class SMTPServer {

	public static Envelop email = null;

	static final String JDBC_DRIVER = "org.h2.Driver";
	static final String DB_URL = "jdbc:h2:tcp://localhost/~/test";

	//  Database credentials
	static final String USER = "admin";
	static final String PASS = "admin123";

	protected final static String CRLF = "\r\n";
	public static void main(String argv[]) throws Exception {
		String command;
		ServerSocket welcomeSocket = new ServerSocket(465);

		while (true) {

			// Get connection
			Socket connectionSocket = welcomeSocket.accept();
			System.out.println("received connection :" + connectionSocket);

			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

			if(connectionSocket.isConnected()){
				outToClient.writeBytes("220" +CRLF);
			}

			while(!connectionSocket.isClosed()) {
				command = inFromClient.readLine();
				System.out.println("Received: " + command);

				// Spliting command
				String splitedCommand[];
				if (command.startsWith("HELO") || command.startsWith("EHLO")) {
					splitedCommand = command.split(" ",2);
				} else {
					splitedCommand = command.split(":",2);
				}
				switch (splitedCommand[0].trim().toUpperCase()) {
					case "HELO":
						outToClient.writeBytes("250 Hello " +CRLF);
						break;
					case "EHLO":
						outToClient.writeBytes("250 Hello " +CRLF);
						break;
					case "MAIL FROM":
						System.out.println("mail from");
						email = new Envelop();

						String mailFrom = extractEmail(splitedCommand[1]);
						if (isValidEmail(mailFrom)) {
							email.setMailFrom(mailFrom);
							email.setStatus(email.MAIL_FROM);
							outToClient.writeBytes("250 <" + mailFrom + "> Accepted." +CRLF);
						} else {
							outToClient.writeBytes("421 Service not available, closing transmission channel" +CRLF);
						}
						break;
					case "RCPT TO":
						System.out.println("rcpt to");
						if (email == null) {
							outToClient.writeBytes("Email null, MAIL FROM Command is missing." +CRLF);
							break;
						}else{
							if((!(email.getStatus().equalsIgnoreCase(email.MAIL_FROM))) && !(email.getStatus().equalsIgnoreCase(email.RCPT_TO))){
								outToClient.writeBytes("500 Syntax error, command unrecognised. Did you forget MAIL FROM Command." +CRLF);
								break;
							}
							if(((email.getStatus().equalsIgnoreCase(email.MAIL_FROM)))){
								email.setStatus(email.RCPT_TO);
							}
							String mailTo = extractEmail(splitedCommand[1]);
							if (isValidEmail(mailTo)) {
								email.setMailTo(mailTo);
								outToClient.writeBytes("250 <" + mailTo + "> Accepted" +CRLF);
							} else {
								outToClient.writeBytes("421 Service not available, closing transmission channel." +CRLF);
							}
						}
						break;
					case "DATA":
						System.out.println("Data");
						if (email == null) {
							outToClient.writeBytes("Email null, MAIL FROM Command is missing." +CRLF);
							break;
						}
						if (!(email.getStatus().equalsIgnoreCase(email.RCPT_TO))) {
							outToClient.writeBytes("500 Syntax error, command unrecognised." +CRLF);
							break;
						}
						outToClient.writeBytes("354 Start mail input; end with <CRLF>.<CRLF>" +CRLF);

						// After get the message then send
						String data;
						boolean success;
						boolean startMessage = false;
						while(true) {
							email.setStatus(email.DATA);
							data = inFromClient.readLine().trim();
							if(data.startsWith("Subject:")){
								String subject = data.substring(8).trim();
								email.setSubject(subject);
							}
							if(data.trim().startsWith(".")){
								if(addToH2(email)) {
									email.setStatus(email.SENT);
									outToClient.writeBytes("250 Email has successfully sent." + CRLF);
									success = true;
									break;
								}
							}
							if(!data.isEmpty()){ // if successfully sent
								if(startMessage){
									email.setMessage(email.getMessage().concat(data + '\n'));
								}
							}
							if(data.startsWith("Content-Transfer-Encoding")){
								startMessage = true;
							}
						}
						if(success) {
							break;
						}else{
							outToClient.writeBytes("552 Transaction Fail." + CRLF);
							break;
						}
					case "QUIT":
						System.out.println("==============================");
						System.out.println("STATUS: "+ email.getStatus());
						System.out.println("FROM: "+ email.getMailFrom());
						System.out.println("TO: "+ email.getMailTo());
						System.out.println("SUBJECT: "+ email.getSubject());
						System.out.println("MESSAGE: "+ email.getMessage());
						System.out.println("==============================");
						System.out.println("QUIT");
						outToClient.writeBytes("221 Bye ");
						connectionSocket.close();
						break;
					default:
						System.out.println("Default");
						outToClient.writeBytes("Please try again! "+CRLF);
						break;
				}
			}
		}
	}

	public static boolean isValidEmail(String email){

		String [] hostDomain = email.split("@",0);
		hostDomain = hostDomain[1].split("[.]",0);

		final String domain = hostDomain[0].trim();

		String[] supportedDomain = {"gmail", "yahoo"};
		for (String support : supportedDomain) {
			if(support.equalsIgnoreCase(domain)){
				return true;
			}
		}
		return false;
	}

	public static String extractEmail(String unExtractMail){
		return unExtractMail.replaceAll("[<,>]", "").trim();
	}

	public static boolean addToH2(Envelop email){
		Connection conn = null;
		Statement stmt = null;

		try{
			String mailTo = "";

			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			// Extracting MailTo list
			for (String to : email.getMailTo()) {
				mailTo = mailTo.concat(to + ",");
			}
			// Execute a query
			stmt = conn.createStatement();
			String sql = "insert into mail values(null, "+
					"'"+email.getSubject()+"'," +
					"'"+email.getMessage()+"', " +
					"'"+email.getMailFrom()+"', " +
					"'"+mailTo+"', " +
					"'cc@gmail.com,cc1@gmail.com', " +
					"'bcc@gmail.com,bcc1@gmail.com', " +
					"current_timestamp()" +
					")";

			stmt.executeUpdate(sql);

			stmt.close();
			conn.close();
		} catch(SQLException se) {
			se.printStackTrace();
			return false;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if(stmt!=null) stmt.close();
			} catch(SQLException se2) {
			}
			try {
				if(conn!=null) conn.close();
			} catch(SQLException se) {
				se.printStackTrace();
			}
		}
		return true;
	}
}
