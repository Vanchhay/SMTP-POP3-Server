import org.h2.engine.Database;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.IllegalBlockingModeException;
import java.security.cert.CRL;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class POP3Server {
	protected static final String CRLF = "\r\n";

	static final String JDBC_DRIVER = "org.h2.Driver";
	static final String DB_URL = "jdbc:h2:tcp://localhost/~/test";

	//  Database credentials
	static final String USER = "admin";
	static final String PASS = "admin123";

	/* POP3 Server Properties */
	private int port;
	private int timeout;
	private boolean serverRunning;

	/* Client's status */
	protected final String AUTH = "AUTH";
	protected final String TRANSACTION = "TRANSACTION";
	protected final String UPDATE = "UPDATE";

	public static void main(String[] args) throws Exception{
		String command;
		int port = 110;
		ServerSocket server = new ServerSocket(port);

		String user = null;
		List<Envelop> mails = new ArrayList<>();
		HashMap<String, Envelop> envelopHashMap = new HashMap<>();
		int totalByte = 0;


		while(true){
			Socket client = server.accept();
			System.out.println("Client connected : " +client);

			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(client.getOutputStream());

			if(client.isConnected()){
				System.out.println("response to client");
				outToClient.writeBytes("+OK" +CRLF);
			}

			while(!client.isClosed()){
				command = inFromClient.readLine();
				System.out.println("Command: "+command);

//				switch (command.toUpperCase()){
//					case "AUTH":
//						System.out.println("AUTH commanded");
//						outToClient.writeBytes("+OK" +CRLF + "." + CRLF);
//						break;
//					case "CAPA":
//						System.out.println("CAPA commanded");
//						outToClient.writeBytes("+OK" + CRLF +"."+ CRLF);
//						break;
//					case "USER":
//						user = command.substring(4).trim();
//						System.out.println("USER commanded: " + user);
//						outToClient.writeBytes("+OK now send PASS "+CRLF);
//						break;
//					case "PASS":
//						System.out.println("PASS COMMENDED: ");
//						outToClient.writeBytes("+OK procced your wish" +CRLF);
//						break;
//					case "UIDL":
//						System.out.println("UIDL commanded");
//						if(envelopHashMap.size() == 0){
//							for (Envelop envelop : mails) {
//								String key = genKey(envelopHashMap);
//								envelopHashMap.put( key, envelop);
//							}
//						}
//						if(envelopHashMap.size() > 0) {
//							outToClient.writeBytes("+OK" + CRLF);
//							int index = 1;
//							for (String key : envelopHashMap.keySet()) {
//								System.out.println( (index++) +" "+ key +CRLF);
//								outToClient.writeBytes((index++) +" "+ key +CRLF);
//							}
//							outToClient.writeBytes(CRLF + "." + CRLF);
//						}
//						break;
//					case "STAT":
//						System.out.println("STAT commanded :" +user);
//						mails = getInboxWhereFrom(user);
//						int count=0;
//						for (Envelop envelop : mails) {
//							totalByte += envelop.getMessage().getBytes().length;
//							count++;
//						}
//						System.out.println("Response: +OK "+count + " " +totalByte);
//						outToClient.writeBytes("+OK "+count + " " +totalByte + CRLF);
//						break;
//				}

				if(command.trim().startsWith("AUTH")){
					System.out.println("AUTH commanded");
					outToClient.writeBytes("+OK" +CRLF + "." + CRLF);
				}
				if(command.trim().startsWith("CAPA")){
					System.out.println("CAPA commanded");
					outToClient.writeBytes("+OK" + CRLF +"."+ CRLF);
				}
				if(command.trim().startsWith("USER")){
					user = command.substring(4).trim();
					System.out.println("USER commanded: " + user);
					outToClient.writeBytes("+OK now send PASS "+CRLF);
				}
				if(command.trim().startsWith("PASS")){
					System.out.println("PASS COMMENDED: ");
					outToClient.writeBytes("+OK procced your wish" +CRLF);
				}

//				if(command.trim().startsWith("UIDL")){
//					System.out.println("UIDL commanded");
//					if(envelopHashMap.size() == 0){
//						for (Envelop envelop : mails) {
//							String key = genKey(envelopHashMap);
//							envelopHashMap.put( key, envelop);
//						}
//					}
//					if(envelopHashMap.size() > 0) {
//						outToClient.writeBytes("+OK" + CRLF);
//						int index = 1;
//						for (String key : envelopHashMap.keySet()) {
//							System.out.println( (index++) +" "+ key +CRLF);
//							outToClient.writeBytes((index++) +" "+ key +CRLF);
//						}
//						outToClient.writeBytes(CRLF + "." + CRLF);
//					}
//				}
				if(command.trim().startsWith("STAT")){
					System.out.println("STAT commanded :" +user);
					mails = getInboxWhereFrom(user);
					int count=0;
					for (Envelop envelop : mails) {
						totalByte += envelop.getMessage().getBytes().length;
						count++;
					}
					outToClient.writeBytes("+OK "+count + " " +totalByte + CRLF);
				}
				if(command.trim().startsWith("LIST")){
					System.out.println("LIST COMMENDED: " +command);

					String msg = command.trim().substring(4).trim();
					if( msg.length() > 0 ){
						int index = Integer.valueOf(msg);
						if( (index-1)> mails.size() ){
							outToClient.writeBytes("-ERR no such message, Please check the LIST again.");
						} else {
							outToClient.writeBytes("+OK " + index + " " + mails.get(index - 1).getMessage().length() + CRLF);
						}
					}
					if(msg.length() == 0){
						outToClient.writeBytes("+OK "+mails.size() + " " +totalByte +CRLF);
						for (int i = 0; i < mails.size() ; i++) {
							System.out.println((i+1) +" " + mails.get(i).getMessage().getBytes().length);
							outToClient.writeBytes( (i+1) +" " + mails.get(i).getMessage().getBytes().length +CRLF);
						}
						outToClient.writeBytes(CRLF +"."+ CRLF);
					}
				}
				if(command.startsWith("RETR")){
					int index = Integer.valueOf(command.substring(5));

					System.out.println("+OK " +mails.get(index-1).getMessage().length() +"\n");
					System.out.println("--- ");
					System.out.println(mails.get(index-1).getMessage());
					System.out.println(" ---");
					System.out.println(CRLF + "." +CRLF);

					outToClient.writeBytes("+OK " +mails.get(index-1).getMessage().length() + CRLF);
					outToClient.writeBytes("---" +CRLF);
					outToClient.writeBytes(mails.get(index-1).getMessage() +CRLF);
					outToClient.writeBytes("---" );
					outToClient.writeBytes(CRLF +"."+ CRLF);
				}
				if(command.trim().startsWith("QUIT")){
					System.out.println("QUIT COMMENDED: ");
					outToClient.writeBytes("+OK" +CRLF);
					client.close();
				}
				if(command.startsWith("DELE")){
					// Delete from H2
					System.out.println("DELETED");
					outToClient.writeBytes("+OK Message Deleted" +CRLF);
				}
			}
		}
	}

	public static List<Envelop> getInboxWhereFrom(String mailFrom){
		Connection conn = null;
		Statement stmt = null;
		List<Envelop> mails = new ArrayList<>();

		try{
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			// Execute a query
			stmt = conn.createStatement();
			String sql = "SELECT * FROM MAIL WhERE MAIL_FROM='"+mailFrom+"' ORDER BY CREATE_AT DESC;";

			ResultSet rs = stmt.executeQuery(sql);

			while(rs.next()){

				List<String> mailTo = new ArrayList<>();
				for (String s : rs.getString("MAIL_TO").split(",")) {
					mailTo.add(s);
				}
				Envelop envelop = new Envelop(
						rs.getString("SUBJECT"),
						rs.getString("MESSAGE"),
						rs.getString("MAIL_FROM"),
						mailTo  );
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

	public static String genKey(HashMap<String, Envelop> envelopHashmap){
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
