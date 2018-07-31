package app;

import java.io.*;
import java.net.*;
//import

class SMTPServer {

	public static void main(String argv[]) {
		ServerSocket server;
		try{
			server = new ServerSocket(465);
			Socket client;
			while(true){
				client = server.accept();
				if(client.isConnected()) {
					System.out.println("CONNECTED =============================");
					SMTPThread st = new SMTPThread(server, client);
					st.start();
				}
			}

		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
