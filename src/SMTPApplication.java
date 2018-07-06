import org.h2.tools.Server;
import org.h2.table.*;

import java.sql.*;

public class SMTPApplication {


	public static void main(String[] args) {

		// start the TCP Server
		Server server = null;
		try{
			server = Server.createTcpServer(args).start();

			Class.forName("org.h2.Driver");
			Connection connection = DriverManager.getConnection("jdbc:h2:tcp://localhost/~/test", "admin", "admin123");
			Statement statement = connection.createStatement();
			String str = "SELECT * FROM TEST";
			ResultSet resultSet = statement.executeQuery(str);
			while(resultSet.next()){
				System.out.print("ID :" + resultSet.getInt("ID"));
				System.out.println("NAME :" + resultSet.getString("NAME"));
			}
		}catch(SQLException e){
			e.printStackTrace();
		}catch(ClassNotFoundException e){
			e.printStackTrace();
		}

		if(server.isRunning(false)){
			System.out.println("Is running");
		}else {
			System.out.println("server is stop");
		}
		// stop the TCP Server
		server.stop();
	}
}
