package receiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

//import inspector.Inspector;

public class Receiver {
	
	private static Deserializer deserializer;
	private static SAXBuilder saxBuilder;
	
	private static ServerSocket serverSocket;
	private static Socket clientSocket;
	private static BufferedReader inStream;
	private static PrintWriter outStream;
	private static final int port = 80;
	
	private static boolean working = true;
	
	public static void main(String[] args) {
		deserializer = new Deserializer();
		saxBuilder = new SAXBuilder();
		
		while (working) {
			try {
				initConnection();
				Document doc = saxBuilder.build(inStream);
				System.out.println("Received document!\n");
				
				Object o = deserializer.deserialize(doc);
				//Inspector.inspect(o, false);
				
			} catch (IOException e) {
				System.out.println(e.getMessage());
			} catch (JDOMException e) {
				System.out.println(e.getMessage());
			}
			
			closeConnection();
		}
	}

	private static void initConnection() {
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("Waiting for a connection ...");
			
			clientSocket = serverSocket.accept();
			System.out.println("Connection accepted");
			
			inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			outStream = new PrintWriter(clientSocket.getOutputStream(), true);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}	
	}
	
	private static void closeConnection() {
		try {
			inStream.close();
			outStream.close();
			clientSocket.close();
			serverSocket.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
