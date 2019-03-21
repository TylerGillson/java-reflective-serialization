package receiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import inspector.Inspector;

/**
 * The Receiver class encapsulates a Deserializer and a network connection
 * to a remote machine which sends it serialized objects in the form of
 * byte streams, which are converted into org.jdom2.Document instances
 * via a SAXBuilder prior to deserialization.
 * 
 * @author tylergillson
 */
public class Receiver {
	
	private static Deserializer deserializer;
	private static SAXBuilder saxBuilder;
	
	private static ServerSocket serverSocket;
	private static Socket clientSocket;
	private static BufferedReader inStream;
	private static final int port = 5000;
	
	private static boolean working = true;
	
	/**
	 * Receive objects from the Sender as byte streams.
	 * Construct org.jdom2.Document instances from the byte streams
	 * via SAXBuilder, then pass said instances off to the Deserializer
	 * for deserialization. Upon reconstruction of the received object
	 * by the deserializer, pass the reconstructed objects off to the object
	 * inspector for visualization.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		deserializer = new Deserializer();
		saxBuilder = new SAXBuilder();
		
		while (working) {
			try {
				initConnection();
				Document doc = saxBuilder.build(inStream);
				System.out.println("Received document! Deserializing ...");
				
				Object o = deserializer.deserialize(doc);
				System.out.println("Deserialization complete ... inspecting deserialized object:");
				
				Inspector.inspect(o, true, false);
				System.out.println();
			} catch (IOException e) {
				System.out.println(e.getMessage());
			} catch (JDOMException e) {
				System.out.println(e.getMessage());
			}
			
			closeConnection();
		}
	}

	/*********************************
	 * NETWORK CONNECTION MANAGEMENT *
	 *********************************/
	
	/**
	 * Initialize a network connection with a remote machine.
	 * The port to listen for connections on is specified by
	 * the port global variable.
	 */
	private static void initConnection() {
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("Waiting for a connection ...");
			
			clientSocket = serverSocket.accept();
			System.out.println("Connection accepted");
			
			inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}	
	}
	
	/**
	 * Close an existing network connection.
	 */
	private static void closeConnection() {
		try {
			inStream.close();
			clientSocket.close();
			serverSocket.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
