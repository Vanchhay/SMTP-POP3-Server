package app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;

class SMTPServer {

	private final static Logger LOGGER = LoggerFactory.getLogger(SMTPServer.class);

	public static void main(String argv[]) {
		ServerSocket server;
		try{
			server = new ServerSocket(465);
			Socket client;
			while(true){
				client = server.accept();
				if(client.isConnected()) {
					SMTPThread st = new SMTPThread(server, client);
					LOGGER.info("New Client Connected on Thread : " + st);
					st.start();
				}
			}

		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
