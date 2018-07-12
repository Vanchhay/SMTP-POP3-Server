import java.io.*;
import java.net.*;

class SMTPServer {

	private static EmailMessage email = null;

	protected final static String CRLF = "\r\n";
	public static void main(String argv[]) throws Exception {
		String command;
		ServerSocket welcomeSocket = new ServerSocket(465);

		while (true) {

			// Get connection
			Socket connectionSocket = welcomeSocket.accept();
			System.out.println("received connection");

			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

			command = inFromClient.readLine();
			System.out.println("Received: " + command);

			// Spliting command
			String splitedCommand[] = command.split(":", 2);
			switch (splitedCommand[0].trim().toUpperCase()) {
				case "HELO":
					outToClient.writeBytes("250 Hello" + CRLF);
					break;
				case "MAIL FROM":
					email = new EmailMessage();
					if(email.getStatus().isEmpty()) {
						email.setStatus(email.MAIL_FROM);
					}
					String mailFrom = extractEmail(splitedCommand[1]);
					if(isValidEmail(mailFrom)){
						email.setMailFrom(mailFrom);
						outToClient.writeBytes("250 <" + mailFrom + "> Accepted." + CRLF);
					}else{
						outToClient.writeBytes("421 Service not available, closing transmission channel");
					}
					break;
				case "RCPT TO":
					if(email == null){
						outToClient.writeBytes("Email null, MAIL FROM Command is missing." +CRLF);
						break;
					}
					if(!(email.getStatus().equalsIgnoreCase(email.MAIL_FROM))) {
						outToClient.writeBytes("500 Syntax error, command unrecognised. Did you forget MAIL FROM Command." + CRLF);
						break;
					}else{
						email.setStatus(email.RCPT_TO);
					}
					String mailTo = extractEmail(splitedCommand[1]);
					if(isValidEmail(mailTo)){
						email.setMailTo(mailTo);
						outToClient.writeBytes("250 <" + mailTo + "> Accepted" + CRLF);
					}else{
						outToClient.writeBytes("421 Service not available, closing transmission channel" +CRLF);
					}
					break;
				case "DATA":
					if(email == null){
						outToClient.writeBytes("Email null, MAIL FROM Command is missing." +CRLF);
						break;
					}
					if(!(email.getStatus().equalsIgnoreCase(email.RCPT_TO))) {
						outToClient.writeBytes("500 Syntax error, command unrecognised." + CRLF);
						break;
					}
					outToClient.writeBytes("354 Start mail input; end with <CRLF>.<CRLF>" + CRLF);

					// After get the message then send
					String data = inFromClient.readLine();
					if(!data.isEmpty()){ // if successfully sent
						email.setMessage(data);
						System.out.println(data);
						outToClient.writeBytes("250 Email has successfully sent." + CRLF);
					}else{
						outToClient.writeBytes("552 Transaction Fail" + CRLF);
					}
					break;
				case "QUIT":
					System.out.println("QUIT");
					connectionSocket.close();
					outToClient.writeBytes("221 Bye" + CRLF);
					break;
				default:
					outToClient.writeBytes("Undefined Command");
					break;
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
