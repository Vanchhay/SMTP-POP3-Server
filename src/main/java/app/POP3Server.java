package app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class POP3Server {

	private final static Logger LOGGER = LoggerFactory.getLogger(POP3Server.class);

	public static void main (String[] args){

		ServerSocket server;
		try{
			server = new ServerSocket(110);
			Socket client;
			while(true){
				client = server.accept();
				if(client.isConnected()) {
					POP3Thread pt = new POP3Thread(server, client);
					LOGGER.info("New Client Connected on Thread : " + pt);
					pt.start();
				}
			}

		}catch(IOException e){
			e.printStackTrace();
		}
	}
}