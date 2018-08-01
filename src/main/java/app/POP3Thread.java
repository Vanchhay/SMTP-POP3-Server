package app;

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

	/* Client's status */
	protected final String UNAUTH = "UNAUTH";
	protected final String AUTH = "AUTH";
	protected final String TRANSACTION = "TRANSACTION";
	protected final String UPDATE = "UPDATE";

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
				System.out.println("Connection Accepted");
				outToClient.writeBytes("+OK" + CRLF);
			}

			while (!client.isClosed()) {
				command = inFromClient.readLine();
				System.out.println("Command: " + command);

				switch (command.substring(0,4).toUpperCase()) {
					case "AUTH":
						System.out.println("AUTH commanded");
						outToClient.writeBytes("+OK" + CRLF + "." + CRLF);
						break;
					case "CAPA":
						System.out.println("CAPA commanded");
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
						break;
					case "USER":
						user = command.substring(5).trim();
						System.out.println("USER commanded: " + user);

						if(user.length() == 0 ){
							outToClient.writeBytes("-ERR Cannot find email");
						}
						outToClient.writeBytes("+OK" + CRLF);
						break;
					case "PASS":
//						String pass = command.substring(5).trim();
						System.out.println("PASS COMMENDED: ");
						outToClient.writeBytes("+OK procced your wish" + CRLF);
						this.clientStatus = this.TRANSACTION;
						break;
					case "STAT":
						if(!this.clientStatus.equalsIgnoreCase(this.TRANSACTION)){
							outToClient.writeBytes("-ERR Requrired to make AUTH" +CRLF);
						}
						System.out.println("STAT commanded :" + user);
						mails = getInboxWhereFrom(user);
						int count = 0;
						for (Envelop envelop : mails) {
							totalByte += envelop.getMessage().getBytes().length;
							count++;
						}
						System.out.println("Response: +OK " + count + " " + totalByte);
						outToClient.writeBytes("+OK " + count + " " + totalByte + CRLF);
						break;
					case "LIST":
						if(!this.clientStatus.equalsIgnoreCase(this.TRANSACTION)){
							outToClient.writeBytes("-ERR Requrired to make AUTH" +CRLF);
						}
						System.out.println("LIST COMMENDED: " + command);
						String msg = command.trim().substring(4).trim();

						if (msg.length() > 0) {
							int index = Integer.valueOf(msg);
							if ((index - 1) >= mails.size()) {
								outToClient.writeBytes("-ERR no such message, Please check the LIST again." + CRLF);
							} else {
								outToClient.writeBytes("+OK " + index + " " + mails.get(index - 1).getMessage().length() + CRLF);
							}
							break;
						}
						if (msg.length() == 0) {
							outToClient.writeBytes("+OK " + mails.size() + " " + totalByte + CRLF);
							for (int i = 0; i < mails.size(); i++) {
								outToClient.writeBytes((i + 1) + " " + mails.get(i).getMessage().getBytes().length + CRLF);
							}
							outToClient.writeBytes(CRLF + "." + CRLF);
						}
						break;
					case "UIDL":
						if(!this.clientStatus.equalsIgnoreCase(this.TRANSACTION)){
							outToClient.writeBytes("-ERR Requrired to make AUTH" +CRLF);
						}
						System.out.println("UIDL commanded: " + envelopHashMap.size());
						if (envelopHashMap.size() == 0) {
							for (Envelop envelop : mails) {
								String key = genKey(envelopHashMap);
								envelopHashMap.put(key,envelop);
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
						break;
					case "RETR":
						if(!this.clientStatus.equalsIgnoreCase(this.TRANSACTION)){
							outToClient.writeBytes("-ERR Requrired to make AUTH" +CRLF);
						}
						int index = Integer.valueOf(command.substring(5));
						if( (index -1) >= mails.size() ){
							outToClient.writeBytes("-ERR no such message, LIMIT: " + mails.size() + "  " + CRLF);
						}

						outToClient.writeBytes("+OK " + mails.get(index - 1).getMessage().length() + CRLF);
						outToClient.writeBytes(mails.get(index - 1).getHeader() + CRLF);
						outToClient.writeBytes(mails.get(index - 1).getMessage());
						outToClient.writeBytes(CRLF + "." + CRLF);
						break;
					case "DELE":
						if(!this.clientStatus.equalsIgnoreCase(this.TRANSACTION)){
							outToClient.writeBytes("-ERR Requrired to make AUTH" +CRLF);
						}
						// Delete from H2
						int i = Integer.valueOf(command.substring(5));
						System.out.println("SENDING MEEING ID =====  " + mails.get(i - 1).getMeetingID());

						for(String mailTo : mails.get(i - 1).getMailTo()) {
							if (deleteEmail(mails.get(i - 1).getMeetingID(), mailTo)) {
								System.out.println("DELETED");
								outToClient.writeBytes("+OK Message Deleted" + CRLF);
								break;
							}
						}
						outToClient.writeBytes("-ERR" + CRLF);
						break;
					case "QUIT":
						System.out.println("QUIT COMMENDED: ");
						outToClient.writeBytes("+OK" + CRLF);
						client.close();
						break;
					default:
						System.out.println("Undefined command: ( " + command + " )");
						outToClient.writeBytes("-ERR Undefined command" + CRLF);
						break;
				}
			}
		} catch(IndexOutOfBoundsException e){
			System.out.println(e.getMessage());
		} catch(IOException e){
			e.printStackTrace();
		} catch(Exception e){
			System.out.println(e.getClass() + " === " + e.getMessage());
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
			System.out.println("DELETING MEEING ID : "+meetingID);
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

	public String genKey(HashMap<String, Envelop> envelopHashmap){
		int leftLimit = 97; // letter 'a'
		int rightLimit = 122; // letter 'z'
		int targetStringLength = 10;

		Random random = new Random();
		StringBuilder buffer = new StringBuilder(targetStringLength);
		while(true) {
			for (int i = 0; i < targetStringLength; i++) {
				int randomLimitedInt = leftLimit + (int)
						(random.nextFloat() * (rightLimit - leftLimit + 1));
				buffer.append((char) randomLimitedInt);
			}

			if(!envelopHashmap.containsKey(buffer.toString())){
				return buffer.toString();
			}
		}
	}

}
