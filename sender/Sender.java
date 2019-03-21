package sender;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class Sender {
	
	private static Scanner sc;
	private static ObjectCreator objCreator;
	private static Serializer serializer;
	private static XMLOutputter xmlOutputter;
	
	private static Socket socket;
	private static PrintWriter outStream;
	
	private static boolean working = true;
	private static int fileCount = 0;
	private static Object currentObj;
	
	private static final String remoteAddress = "136.159.5.25";  //"127.0.0.1"
	private static final int port = 5000;
	private static final String fileRoot = "/Users/tylergillson/Desktop/XML/";
	
	/**
	 * Create an arbitrary number of objects. Objects may be selected from 5 predefined options.
	 * Created objects may be serialized, then either transmitted over a network or written to a file.
	 * @param args
	 */
	public static void main(String[] args) {
		sc = new Scanner(System.in);
		objCreator = new ObjectCreator(sc);
		serializer = new Serializer();
		xmlOutputter = new XMLOutputter();
		
		objCreator.printMenu(false);
		while (working) {
			// Get menu selection from user:
			String prompt = "Enter a number from 1-9 (enter 8 to see the menu again): ";
			String errorMsg = "Please enter a number from 1-9 (enter 8 to see the menu again): ";
			int selection = objCreator.getIntSelection(prompt, errorMsg, 1, 10);
			
			// Create an object, serialize & transmit an object, re-print the menu, or exit:
			if (selection == 6)
				serializeThenTransmitObject();
			else if (selection == 7)
				serializeThenWriteObjectToFile();
			else if (selection == 8)
				objCreator.printMenu(true);
			else if (selection == 9) {
				System.out.println("Exiting ...");
				working = false;	
			}
			else
				objCreator.createObject(selection, true);
		}
		sc.close();	
	}
		
	/**
	 * Serialize an arbitrary object, then transmit it over a network
	 * socket for deserialization on a remote machine.
	 * @param obj - An object instance
	 */
	public static void serializeThenTransmitObject() {
		Document doc = serializeObject("transmit");
		initConnection();
		
		if (doc != null) {
			try {
				xmlOutputter.setFormat(Format.getRawFormat());
				xmlOutputter.output(doc, outStream);
				closeConnection();
				System.out.println("Transmitted!\n");
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	public static void serializeThenWriteObjectToFile() {
		Document doc = serializeObject("write to a file");
		
		if (doc != null) {
		
			String className = currentObj.getClass().getSimpleName();
			String filepath = fileRoot + String.valueOf(fileCount++) + "_" + className + ".xml";
			
			try {
				xmlOutputter.setFormat(Format.getPrettyFormat());
				xmlOutputter.output(doc, new FileWriter(filepath));
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}	
		}
	}

	public static Document serializeObject(String msg) {
		if (objCreator.getObjCounter() == 0) {
			System.out.println("\nNo objects have been created yet.\n");
			return null;
		}
		else {
			System.out.println();
			objCreator.printExistingObjects();
			
			String prompt = "\nEnter the id number of the object you wish to serialize & " + msg + ": ";
			String errorMsg;
			if (objCreator.getObjCounter() > 1)
				errorMsg = "Please enter a number from 0-" + String.valueOf(objCreator.getObjCounter() - 1);
			else
				errorMsg = "You must enter 0, as only one object exists.";	
			
			int selection = objCreator.getIntSelection(prompt, errorMsg, 0, objCreator.getObjCounter());
			System.out.println();
			
			currentObj = objCreator.getObj(selection);
			return serializer.serialize(currentObj);
		}
	}
	
	/**********************
	 * NETWORK CONNECTION *
	 **********************/
	
	public static void initConnection() {
		try {
			socket = new Socket(remoteAddress, port);
			System.out.println("Connected to remote machine ...");
			
			outStream = new PrintWriter(socket.getOutputStream(), true);
		} catch (UnknownHostException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void closeConnection() {
		try {
			outStream.close();
			socket.close(); 
		} catch(IOException e) {
			System.out.println(e.getMessage());
		} 
	}
}
