import java.io.*;
import java.net.*;

class SMTPServer {

	public static EmailMessage email = null;

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
				outToClient.writeBytes("250" +CRLF);
			}

			while(!connectionSocket.isClosed()) {
				command = inFromClient.readLine();
				System.out.println("Received: " + command);

				// Spliting command
				String splitedCommand[];
				if (command.startsWith("HELO")) {
					splitedCommand = command.split(" ",2);
				} else {
					splitedCommand = command.split(":",2);
				}
				switch (splitedCommand[0].trim().toUpperCase()) {
					case "HELO":
						outToClient.writeBytes("250 Hello " +CRLF);
						break;
					case "MAIL FROM":
						System.out.println("mail from");
						email = new EmailMessage();

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
						outToClient.writeBytes("354 Start mail input; end with <CRLF>.<CRLF>" + '\n');
						outToClient.writeBytes("Date: " + '\n');
						outToClient.writeBytes("From: " +email.getMailFrom() + '\n');
						outToClient.writeBytes("To: ");
						for (String rcpt : email.getMailTo()){
							outToClient.writeBytes(rcpt + " , ");
						}
//						if(!email.getMailCc().isEmpty()) {
//							outToClient.writeBytes("CC: ");
//							for (String cc : email.getMailCc())
//								outToClient.writeBytes(cc + " , ");
//						}
//						if(!email.getMailBcc().isEmpty()) {
//							outToClient.writeBytes("BCC: ");
//							for (String bcc : email.getMailBcc())
//								outToClient.writeBytes(bcc + " , ");
//						}
						outToClient.writeBytes(CRLF);

						// After get the message then send
						String data = inFromClient.readLine();
						if (!data.isEmpty()) { // if successfully sent
							email.setMessage(data);
							System.out.println(data);
							outToClient.writeBytes("250 Email has successfully sent." +CRLF);
						} else {
							outToClient.writeBytes("552 Transaction Fail." +CRLF);
						}
						break;
					case "QUIT":
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

}
