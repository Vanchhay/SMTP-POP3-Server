package app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class POP3Thread extends Thread {

	protected static final String CRLF = "\r\n";
	private final static Logger LOGGER = LoggerFactory.getLogger(POP3Thread.class);

	/* Client's status */
	protected final String UNAUTH = "UNAUTH";
	protected final String TRANSACTION = "TRANSACTION";

	public String clientStatus;

	public ServerSocket server;
	public Socket client;

	public POP3Thread(ServerSocket server, Socket client) {
		this.server = server;
		this.client = client;
	}

	@Override
	public void run() {
		String command;

		String user = null;
		List<Envelop> mails = new ArrayList<>();
		HashMap<String, Envelop> envelopHashMap = new HashMap<>();
		int totalByte = 0;

		try {

			BufferedReader inFromClient = null;
			DataOutputStream outToClient = null;

			if (client.isConnected()) {
				this.clientStatus = this.UNAUTH;
				inFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
				outToClient = new DataOutputStream(client.getOutputStream());
				outToClient.writeBytes("+OK" + CRLF);
				LOGGER.info(this.getName() + " GREETING");
			}

			while (!client.isClosed()) {
				command = inFromClient.readLine();

				switch (command.substring(0,4).toUpperCase()) {
					case "AUTH":
						outToClient.writeBytes("+OK" + CRLF + "." + CRLF);
						LOGGER.info(this.getName() + " AUTH +OK");
						break;
					case "CAPA":
						LOGGER.info(this.getName() + " COMMENDED: " + command);
						outToClient.writeBytes("+OK" + CRLF);
						outToClient.writeBytes("AUTH" + CRLF);
						outToClient.writeBytes("USER" + CRLF);
						outToClient.writeBytes("PASS" + CRLF);
						outToClient.writeBytes("STAT" + CRLF);
						outToClient.writeBytes("LIST []" + CRLF);
						outToClient.writeBytes("UIDL" + CRLF);
						outToClient.writeBytes("RETR" + CRLF);
						outToClient.writeBytes("DELE" + CRLF);
						outToClient.writeBytes("QUIT" + CRLF);
						outToClient.writeBytes("+OK" + CRLF + "." + CRLF);
						LOGGER.info(this.getName() + " CAPA +OK");
						break;
					case "USER":
						user = command.substring(5).trim();
						this.setName(user);
						LOGGER.info("[" + this.getName() + "]");

						if(user.length() == 0 ){
							outToClient.writeBytes("-ERR Cannot find email" +CRLF);
							LOGGER.info(this.getName() + " USER -ERR Cannot find matched email");
						}
						outToClient.writeBytes("+OK" + CRLF);
						LOGGER.info(this.getName() + " USER OK");
						break;
					case "PASS":
//						String pass = command.substring(5).trim();
						outToClient.writeBytes("+OK procced your wish" + CRLF);
						LOGGER.info(this.getName() + " PASS: <>");
						this.clientStatus = this.TRANSACTION;
						break;
					case "STAT":
						if(!this.clientStatus.equalsIgnoreCase(this.TRANSACTION)){
							outToClient.writeBytes("-ERR Requrired to make AUTH" +CRLF);
							LOGGER.info(this.getName() + " STAT -ERR Requrired to make AUTH" );
						}
						mails = getInboxWhereFrom(user);
						int count = 0;
						for (Envelop envelop : mails) {
							totalByte += envelop.getMessage().getBytes().length;
							count++;
						}
						outToClient.writeBytes("+OK " + count + " " + totalByte + CRLF);
						LOGGER.info(this.getName() + " STAT +OK");
						break;
					case "LIST":
						if(!this.clientStatus.equalsIgnoreCase(this.TRANSACTION)){
							outToClient.writeBytes("-ERR Requrired to make AUTH" +CRLF);
							LOGGER.info(this.getName() + " LIST -ERR Requrired to make AUTH" );
						}
						String msg = command.trim().substring(4).trim();

						if (msg.length() > 0) {
							int index = Integer.valueOf(msg);
							if ((index - 1) >= mails.size()) {
								outToClient.writeBytes("-ERR no such message, Please check the LIST again." + CRLF);
								LOGGER.info(this.getName() + " LIST -ERR no such message, Please check the LIST again.");
							} else {
								outToClient.writeBytes("+OK " + index + " " + mails.get(index - 1).getMessage().length() + CRLF);
								LOGGER.info(this.getName() + " LIST +OK");
							}
							break;
						}
						if (msg.length() == 0) {
							outToClient.writeBytes("+OK " + mails.size() + " " + totalByte + CRLF);
							for (int i = 0; i < mails.size(); i++) {
								outToClient.writeBytes((i + 1) + " " + mails.get(i).getMessage().getBytes().length + CRLF);
							}
							outToClient.writeBytes(CRLF + "." + CRLF);
							LOGGER.info(this.getName() + " LIST +OK");
						}
						break;
					case "UIDL":
						LOGGER.info(this.getName() + " COMMENDED: " +command);
						if(!this.clientStatus.equalsIgnoreCase(this.TRANSACTION)){
							outToClient.writeBytes("-ERR Requrired to make AUTH" +CRLF);
							LOGGER.info(this.getName() + " -ERR Requrired to make AUTH");
						}
						if (envelopHashMap.size() == 0) {
							for (Envelop envelop : mails) {
								envelopHashMap.put( envelop.getMeetingID(), envelop);
							}
						}
						if (envelopHashMap.size() > 0) {
							outToClient.writeBytes("+OK" + CRLF);
							int index = 1;
							for (String key : envelopHashMap.keySet()) {
								System.out.println((index) + " " + key);
								outToClient.writeBytes((index++) + " " + key + CRLF);
							}
							outToClient.writeBytes(CRLF + "." + CRLF);
						}
						LOGGER.info(this.getName() + " UIDL +OK");
						break;
					case "RETR":
						if(!this.clientStatus.equalsIgnoreCase(this.TRANSACTION)){
							outToClient.writeBytes("-ERR Requrired to make AUTH" +CRLF);
							LOGGER.info(this.getName() + " RETR -ERR Requrired to make AUTH");
						}
						int index = Integer.valueOf(command.substring(5));
						if( (index -1) >= mails.size() ){
							outToClient.writeBytes("-ERR no such message, LIMIT: " + mails.size() + "  " + CRLF);
							LOGGER.info(this.getName() + " RETR -ERR no such message, LIMIT: " + mails.size());
						}

						outToClient.writeBytes("+OK " + mails.get(index - 1).getMessage().length() + CRLF);
						outToClient.writeBytes(mails.get(index - 1).getHeader() + CRLF);
						outToClient.writeBytes(mails.get(index - 1).getMessage());
						outToClient.writeBytes(CRLF + "." + CRLF);
						LOGGER.info(this.getName() + " RETR +OK");
						break;
					case "DELE":
						if(!this.clientStatus.equalsIgnoreCase(this.TRANSACTION)){
							outToClient.writeBytes("-ERR Requrired to make AUTH" +CRLF);
							LOGGER.info(this.getName() + " DELE -ERR Requrired to make AUTH ");
						}
						// Delete from H2
						int i = Integer.valueOf(command.substring(5));
						if(i > 0) {
							for (String mailTo : mails.get(i - 1).getMailTo()) {
								if (deleteEmail(mails.get(i - 1).getMeetingID(),mailTo)) {
									outToClient.writeBytes("+OK Message Deleted" + CRLF);
									LOGGER.info(this.getName() + " DELE " + (i - 1) + " +OK");
									break;
								}
							}
						}
						outToClient.writeBytes("-ERR" + CRLF);
						LOGGER.info(this.getName() + " DELE -ERR");
						break;
					case "QUIT":
						outToClient.writeBytes("+OK" + CRLF);
						client.close();
						LOGGER.info(this.getName() + " QUIT +OK");
						break;
					default:
						outToClient.writeBytes("-ERR Undefined command" + CRLF);
						LOGGER.info("COMMENDED UNDEFINED: " +command);
						break;
				}
			}
		} catch(IndexOutOfBoundsException e){
			LOGGER.info(this.getName() + "[" + e.getMessage() +"]");
		} catch(IOException e){
			LOGGER.info(this.getName() + "[" + e.getMessage() +"]");
		} catch(Exception e){
			LOGGER.info(this.getName() + "[" + e.getClass() + "] === " + "[" + e.getMessage() +"]");
		} finally {
			try{
				client.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}

	public List<Envelop> getInboxWhereFrom(String mailTo){

		Connection conn = null ;
		Statement stmt = null;
		List<Envelop> mails = new ArrayList<>();

		try{

			conn = new H2Database().getConnection();

			// Execute a query
			stmt = conn.createStatement();
			String sql = "SELECT * FROM MAIL WhERE MAIL_TO='"+mailTo+"' ORDER BY CREATE_AT DESC;";

			ResultSet rs = stmt.executeQuery(sql);

			while(rs.next()){

				List<String> mailToList = new ArrayList<>();
				for (String s : rs.getString("MAIL_TO").split(",")) {
					mailToList.add(s);
				}
				Envelop envelop = new Envelop(
						rs.getString("MEETINGID"),
						rs.getString("HEADER"),
						rs.getString("SUBJECT"),
						rs.getString("MESSAGE"),
						rs.getString("MAIL_FROM"),
						mailToList  );

				mails.add(envelop);
			}
			stmt.close();
			conn.close();
		} catch(SQLException se) {
			se.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try{
				if (stmt != null) stmt.close();
			}catch(SQLException se2){
			}
			try{
				if (conn != null) conn.close();
			}catch(SQLException se){
				se.printStackTrace();
			}
		}
		return mails;
	}

	public boolean deleteEmail(String meetingID, String mailTo){
		Connection conn = null;
		Statement stmt = null;

		try{
			conn = new H2Database().getConnection();

			// Execute a query
			stmt = conn.createStatement();
			LOGGER.info("DELETING MEEING ID : " + meetingID + " User: " + mailTo);
			String sql = "DELETE FROM MAIL WHERE MEETINGID='"+ meetingID +"' AND MAIL_TO='"+ mailTo +"';";

			stmt.executeUpdate(sql);

			stmt.close();
			conn.close();

			return true;
		} catch(SQLException se) {
			se.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try{
				if (stmt != null) stmt.close();
			}catch(SQLException se2){
			}
			try{
				if (conn != null) conn.close();
			}catch(SQLException se){
				se.printStackTrace();
			}
		}
		return false;
	}


}
