import java.io.*;
import java.net.*;
import java.util.*;

public class SMTPClient {

	private static DataOutputStream requestStream;
	private static BufferedReader requestInput;
	private static BufferedReader responseStream;

	private static String sentence;
	private static String response;

	public static void main(String[] args) throws Exception {
		// TODO code application logic here
		// Establish a TCP connection with the mail server.
		System.out.println("Enter the mail server you wish to connect to (example:  smtp,gmail.com):\n");
		String hostName = new String();
		Scanner emailScanner = new Scanner(System.in);
		hostName = emailScanner.next();
		System.out.println(hostName);

		while (true) {

	// Create socket
			Socket emailSocket = new Socket("10.10.18.143", 465);
	// Get user's command
			System.out.print("Client: ");
			requestInput = new BufferedReader(new InputStreamReader(System.in));
	// Init input/output stream for socket
			requestStream = new DataOutputStream(emailSocket.getOutputStream());
			responseStream = new BufferedReader(new InputStreamReader(emailSocket.getInputStream()));
	// extract command from buffer then make the request to server
			sentence = requestInput.readLine();
			requestStream.writeBytes(sentence + '\n');    // write input to server
	// get response from server
			response = responseStream.readLine();
			if (response.startsWith("220")) {
				throw new Exception("220 reply not received from server.\n");
			}else{
				System.out.println("Server: " +response);
			}
			System.out.println();
			emailSocket.close();
		}
	}
}