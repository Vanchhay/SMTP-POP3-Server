import java.io.*;
import java.net.*;

class SMTPServer {

	private static EmailMessage email;

	protected final static String CRLF = "\r\n";
	public static void main(String argv[]) throws Exception {
		String command;
		ServerSocket welcomeSocket = new ServerSocket(6782);

		while (true) {

			// Get connection
			Socket connectionSocket = welcomeSocket.accept();

			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

			command = inFromClient.readLine();
			System.out.println("Received: " + command);

			// Spliting command
			String splitedCommand[] = command.split(":", 2);
			System.out.println(email);
			switch (splitedCommand[0].trim().toUpperCase()) {
				case "HELO":
					outToClient.writeBytes("250 Hello" + CRLF);
					break;
				case "MAIL FROM":
					email = new EmailMessage();
					System.out.println(email);
					if(email.getStatus().isEmpty()) {
						email.setStatus(email.MAIL_FROM);
					}
					String mailFrom = extractEmail(splitedCommand[1]);
					if(isValidEmail(mailFrom)){
						email.setMailFrom(mailFrom);
						outToClient.writeBytes("250 " + mailFrom + " Accepted." + CRLF);
					}else{
						outToClient.writeBytes("421 Service not available, closing transmission channel");
					}
					break;
				case "RCPT TO":
					System.out.println(email);
					if(!(email.getStatus().equalsIgnoreCase(email.MAIL_FROM))) {
						outToClient.writeBytes("500 Syntax error, command unrecognised.");
					}
					String mailTo = extractEmail(splitedCommand[1]);
					if(isValidEmail(mailTo)){
						email.setMailTo(mailTo);
						outToClient.writeBytes("250 " + mailTo + " Accepted" + CRLF);
					}else{
						outToClient.writeBytes("421 Service not available, closing transmission channel");
					}
					break;
				case "DATA":
					if(!(email.getStatus().equalsIgnoreCase(email.RCPT_TO))) {
						outToClient.writeBytes("500 Syntax error, command unrecognised.");
					}
					outToClient.writeBytes("354 Start mail input; end with <CRLF>.<CRLF>" + CRLF);
					// After get the message then send
					if(true){ // if successfully sent
						outToClient.writeBytes("250 Email has successfully sent." + CRLF);
					}else{
						outToClient.writeBytes("552 Transaction Fail" + CRLF);
					}
					break;
				case "QUIT":
					outToClient.writeBytes("221 Bye" + CRLF);
					System.out.println("QUIT");
					inFromClient.close();
					connectionSocket.close();
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
