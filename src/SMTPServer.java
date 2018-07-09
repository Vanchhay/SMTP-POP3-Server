import java.io.*;
import java.net.*;

class SMTPServer {
	public static void main(String argv[]) throws Exception {
		String command;
		ServerSocket welcomeSocket = new ServerSocket(465);

		while (true) {

			// Get connection
			Socket connectionSocket = welcomeSocket.accept();

			//	get input from client
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			command = inFromClient.readLine().trim().toUpperCase();
			System.out.println(command);

			if (command.equalsIgnoreCase("error")){
				System.out.println("Received: " + command);
				DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
				outToClient.writeBytes("220 Bad Request !! DONOT command error");
			}

			System.out.println("Received: " + command);

			// response to client
			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

			String str[] = command.split(" ");
			switch (str[0]) {
				case "HELO":
					outToClient.writeBytes("250 Hello "+ str[1]);
					break;
				case "MAIL FROM":
					outToClient.writeBytes("250 OK");
					break;
				case "RCPT TO":
					outToClient.writeBytes("250 OK");
					break;
				case "DATA":
					outToClient.writeBytes("250 Ok message accepted for delivery: queued as 12345");
					break;
				case "QUIT":
					outToClient.writeBytes("221 Bye");
					break;
				default:
					outToClient.writeBytes("Undefined Command");
					break;
			}
			connectionSocket.close();
		}
	}
}
