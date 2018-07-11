import java.io.*;
import java.net.*;

class SMTPServer {
	protected final static String CRLF = "\r\n";
	public static void main(String argv[]) throws Exception {
		String command;
		ServerSocket welcomeSocket = new ServerSocket(6782);
		EmailMessage email;

		while (true) {

			// Get connection
			Socket connectionSocket = welcomeSocket.accept();

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
					if(email.getStatus().isEmpty()){
						email.setStatus(email.MAIL_FROM);
					}
					String mailFrom = splitedCommand[1].replaceAll("[<,>]", "").trim();

					if(isValidEmail(mailFrom)){
						email.setMailFrom(mailFrom);
						outToClient.writeBytes("250 " + mailFrom + "Accepted" + CRLF);
					}else{
						outToClient.writeBytes("421 unsupported domain!!");
					}
					break;
				case "RCPT TO":
					outToClient.writeBytes("250 OK" + CRLF);
					break;
				case "DATA":
					outToClient.writeBytes("250 Ok message accepted for delivery: queued as 12345" + CRLF);
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
		System.out.println("returning false");
		return false;
	}

}
