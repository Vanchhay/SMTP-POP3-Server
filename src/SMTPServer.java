import java.io.*;
import java.net.*;

class SMTPServer {

	private static Socket connectionSocket;

	public static void main(String argv[]) throws Exception {
		String command;
		ServerSocket welcomeSocket = new ServerSocket(465);

		while (true) {

			// Get connection
			connectionSocket = welcomeSocket.accept();

			//	get input from client
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			command = inFromClient.readLine();
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

			processCommand(str[0]);
//			inFromClient.close();
		}
	}

	public static void processCommand(String command) throws Exception{

		DataOutputStream outToClient = null;
		try{
			outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			switch (command.trim().toUpperCase()) {
				case "HELO":
					outToClient.writeBytes("250 Hello ");
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
					connectionSocket.close();
					break;
				default:
					outToClient.writeBytes("Undefined Command");
					break;
			}
		}catch(IOException e){
			e.printStackTrace();
		}
//		outToClient.close();
	}
}
