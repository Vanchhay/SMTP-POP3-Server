package main.java;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class POP3Server {

	public static void main (String[] args){

		ServerSocket server;
		try{
			server = new ServerSocket(110);
			Socket client;
			while(true){
				client = server.accept();
				if(client.isConnected()) {
					System.out.println("CONNECTED =============================");
					POP3Thread pt = new POP3Thread(server, client);
					pt.start();
				}
			}

		}catch(IOException e){
			e.printStackTrace();
		}
	}
}