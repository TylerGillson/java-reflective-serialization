package serialization;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import serializationObjects.CollectionObject;
import serializationObjects.PrimitiveArrayObject;
import serializationObjects.ReferenceArrayObject;
import serializationObjects.ReferenceObject;
import serializationObjects.SimpleObject;

import java.util.Scanner;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class ObjectCreator {
	
	private static Serializer serializer;
	private static Transmitter transmitter;
	
	private static Scanner sc;
	private static XMLOutputter xmlOutputter = new XMLOutputter();
	private static final HashMap<Integer, Object> objHashMap = new HashMap<Integer, Object>();
	
	private static boolean creatingObjects = true;
	private static int id = 0;
	private static Object currentObj;
	private static int currentObjId;
	
	/**
	 * Create an arbitrary number of objects. Objects may be selected from 5 predefined options.
	 * Optionally serialize, then transmit created objects over a network.
	 * @param args
	 */
	public static void main(String[] args) {
		serializer = new Serializer();
		transmitter = new Transmitter();
		
		sc = new Scanner(System.in);
		printMenu(false);
		
		while (creatingObjects) {
			// Get menu selection from user:
			String prompt = "Enter a number from 1-9 (enter 8 to see the menu again): ";
			String errorMsg = "Please enter a number from 1-9 (enter 8 to see the menu again): ";
			int selection = getIntSelection(prompt, errorMsg, 1, 10);
			
			// Create an object, serialize & transmit an object, re-print the menu, or exit:
			if (selection == 6)
				serializeThenTransmitObject();
			else if (selection == 7)
				serializeThenWriteObjectToFile();
			else if (selection == 8)
				printMenu(true);
			else if (selection == 9) {
				System.out.println("Exiting ...");
				creatingObjects = false;	
			}
			else
				createObject(selection, true);
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
		if (doc != null)
			transmitter.transmit(doc);
	}
	
	public static void serializeThenWriteObjectToFile() {
		Document doc = serializeObject("write to a file");
		if (doc != null) {
			xmlOutputter.setFormat(Format.getPrettyFormat());
			
			String className = currentObj.getClass().getSimpleName();
			String filepath = "/Users/tylergillson/Desktop/XML/" + className + currentObjId + ".xml";
			
			try {
				xmlOutputter.output(doc, new FileWriter(filepath));
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}	
		}
	}

	public static Document serializeObject(String msg) {
		if (id == 0) {
			System.out.println("\nNo objects have been created yet.\n");
			return null;
		}
		else {
			System.out.println();
			printExistingObjects();
			
			String prompt = "\nEnter the id number of the object you wish to serialize & " + msg + ": ";
			String errorMsg;
			if (id > 1)
				errorMsg = "Please enter a number from 0-" + String.valueOf(id - 1);
			else
				errorMsg = "You must enter 0, as only one object exists.";	
			
			int selection = getIntSelection(prompt, errorMsg, 0, id);
			System.out.println();
			
			currentObj = objHashMap.get(selection);
			currentObjId = selection;
			return serializer.serialize(currentObj);
		}
	}
	
	/**********************************
	 * Object Creation Methods *
	 **********************************/
	
	/**
	 * Create a new SimpleObject instance.
	 * @param endLine - Whether or not to print a line break after object creation
	 * @return simpleObj - A newly created SimpleObject
	 */
	public static SimpleObject createSimpleObject(boolean endLine) {
		System.out.println("\nCreating new simple object...");

		int i = getIntInput(false, 0);
		
		System.out.print("Enter a boolean: ");
		while (!sc.hasNextBoolean()) {
			sc.next();
			System.out.print("Enter a valid boolean: ");	
		}
		boolean b = sc.nextBoolean();
		
		SimpleObject simpleObj = new SimpleObject(i, b);
		objHashMap.put(id++, simpleObj);
		
		System.out.println("Simple object created.");
		if (endLine) System.out.println();
		return simpleObj;
	}
	
	/**
	 * Create a new ReferenceObject instance.
	 * @param endLine - Whether or not to print a line break after object creation
	 * @return refObj - A newly created ReferenceObject
	 */
	public static ReferenceObject createReferenceObject(boolean endLine) {
		System.out.println("\nCreating new reference object... what type of object should be referenced?");
		String prompt = "Enter: 1 for new Simple Object, 2 for new Reference Object, 3 for existing object: ";
		String errorMsg = "Enter a number from 1-3";
		int selection = getIntSelection(prompt, errorMsg, 1, 4);
		
		ReferenceObject refObj = null;
		switch (selection) {
			case 1:
				SimpleObject simpleObj = createSimpleObject(false);
				refObj = new ReferenceObject(simpleObj);
				break;
			case 2:
				ReferenceObject recRefObj = createReferenceObject(false);
				refObj = new ReferenceObject(recRefObj);
				break;
			case 3:
				if (id == 0)
					System.out.println("No objects have been created yet.\n");
				else {
					printExistingObjects();
					
					prompt = "\nEnter the id number of the object you wish to reference: ";
					if (id > 1)
						errorMsg = "Please enter a number from 0-" + String.valueOf(id - 1);
					else
						errorMsg = "You must enter 0, as only one object exists.";
						
					selection = getIntSelection(prompt, errorMsg, 0, id);
					refObj = new ReferenceObject(objHashMap.get(selection));
				}
				break;
		}
		
		if (refObj != null) {
			objHashMap.put(id++, refObj);
			System.out.println("Reference object created.");
			if (endLine) System.out.println();	
		}
		return refObj;
	}
	
	/**
	 * Create a new PrimitiveArrayObject instance.
	 * @param endLine - Whether or not to print a line break after object creation
	 * @return primitiveArrayObj - A newly created PrimitiveArrayObject
	 */
	public static PrimitiveArrayObject createPrimitiveArrayObject(boolean endLine) {
		System.out.println("\nCreating new primitive array... how many elements should it have?");
		int length = getIntInput(true, 1);
		int[] ints = new int[length];
		
		// Initialize its elements:
		System.out.println("Integer array initialized. Please provide values for its elements.");
		for (int i=0; i<length; i++) {
			ints[i] = getIntInput(false, 0);
		}
		
		PrimitiveArrayObject primitiveArrayObj = new PrimitiveArrayObject(ints);
		objHashMap.put(id++, primitiveArrayObj);
		
		System.out.println("Primitive array object created.");
		if (endLine) System.out.println();
		return primitiveArrayObj;
	}
	
	/**
	 * Create a new ReferenceArrayObject instance.
	 * @param endLine - Whether or not to print a line break after object creation
	 * @return referenceArrayObj - A newly created ReferenceArrayObject
	 */
	public static ReferenceArrayObject createReferenceArrayObject(boolean endLine) {
		System.out.println("\nCreating new reference array object... how many elements should it have?");
		
		// Initialize the array:
		int length = getIntInput(true, 1);
		Object[] refs = new Object[length];
		
		// Initialize its elements:
		System.out.println("Reference array initialized. Please create its elements.");
		System.out.println("Options: 1=SimpleObject, 2=ReferenceObject, 3=PrimitiveArrayObject, 4=ReferenceArrayObject, 5=CollectionObject\n");
		for (int i=0; i<length; i++) {
			int selection = getIntSelection("Select an option for element " + String.valueOf(i) + ": ", "", 1, 6);
			refs[i] = createObject(selection, false);
		}
		
		ReferenceArrayObject referenceArrayObj = new ReferenceArrayObject(refs);
		objHashMap.put(id++, referenceArrayObj);
		
		System.out.println("Reference array object created.");
		if (endLine) System.out.println();
		return referenceArrayObj;
	}
	
	/**
	 * Create a new CollectionObject instance.
	 * @param endLine - Whether or not to print a line break after object creation
	 * @return collectionObj - A newly created CollectionObject
	 */
	public static CollectionObject createObjectCollection(boolean endLine) {
		System.out.println("\nCreating new collection object... how many elements should it have?");
		
		// Initialize the array:
		int length = getIntInput(true, 1);
		ArrayList<Object> refList = new ArrayList<Object>();
		
		// Initialize its elements:
		System.out.println("Collection object initialized. Please create its elements.");
		System.out.println("Options: 1=SimpleObject, 2=ReferenceObject, 3=PrimitiveArrayObject, 4=ReferenceArrayObject, 5=CollectionObject\n");
		for (int i=0; i<length; i++) {
			int selection = getIntSelection("Select an option for element " + String.valueOf(i) + ": ", "", 1, 6);
			refList.add(createObject(selection, false));
		}
		
		CollectionObject collectionObj = new CollectionObject(refList);
		objHashMap.put(id++, collectionObj);
		
		System.out.println("Collection object created.");
		if (endLine) System.out.println();
		return collectionObj;
	}
	
	/**********************************
	 * Object Creation Helper Methods *
	 **********************************/
	
	/**
	 * Print a menu of object creation options for the user.
	 * @param preLine - boolean indicating whether or not to print a line break before printing the menu
	 */
	public static void printMenu(boolean preLine) {
		if (preLine) System.out.println();
		System.out.println("Please select an option by typing a number and pressing enter:\n");
		System.out.println("\t1 = Create a simple object");
		System.out.println("\t2 = Create an object containing an object reference");
		System.out.println("\t3 = Create an object containing an array of primitives");
		System.out.println("\t4 = Create an object containing an array of objects");
		System.out.println("\t5 = Create an object containing a collection of objects");
		System.out.println("\t6 = Serialize an existing object, then transmit it to another machine");
		System.out.println("\t7 = Serialize an existing object, then write it to a file");
		System.out.println("\t8 = Re-print the menu");
		System.out.println("\t9 = Exit\n");
	}
	
	/**
	 * Acquire an integer from the user via standard input.
	 * @param useMin - Whether or not to enforce a minimum acceptable value
	 * @param min - The minimum acceptable value
	 * @return The integer entered by the user
	 */
	public static int getIntInput(boolean useMin, int min) {
		int i;
		while (true) {
			String prompt = (useMin) ? "Enter an integer >= " + String.valueOf(min) + ": " : "Enter an integer: ";
			String errorMsg = (useMin) ? "Enter a valid integer >= " + String.valueOf(min) + ": " : "Enter a valid integer: ";
			System.out.print(prompt);
			
			while (!sc.hasNextInt()) {
				sc.next();
				System.out.print(errorMsg);	
			}
			i = sc.nextInt();
			
			if (!useMin || useMin && i >= min)
				break;
		}
		return i;
	}
	
	/**
	 * Acquire an integer selection from the user via standard input.
	 * @param prompt - String message to introduce the selection
	 * @param errorMsg - String message to print when an invalid input is provided
	 * @param min - Integer indicating minimum acceptable input value (inclusive)
	 * @param max - Integer indicating maximum acceptable input value (exclusive)
	 * @return selection - An integer input from [min, max-1]
	 */
	public static int getIntSelection(String prompt, String errorMsg, int min, int max) {
		int selection;
		while (true) {
			System.out.print(prompt);
            
			while (!sc.hasNextInt()) {
                sc.next();
                System.out.print(errorMsg);
            }
            
			selection = sc.nextInt();
			if (min == max) {
				if (selection == min)
	            	break;
	            else
	            	System.out.print(errorMsg);
			}
			else {
				if (selection >= min && selection < max)
	            	break;
	            else
	            	System.out.print(errorMsg);	
			}
        }
		return selection;
	}
	
	/**
	 * Print all the objects that have been created so far.
	 */
	public static void printExistingObjects() {
		System.out.println("Existing objects:");
		for (Entry<Integer, Object> e : objHashMap.entrySet())
			System.out.println("\tID:" + String.valueOf(e.getKey()) + e.getValue().toString());
	}
	
	/**
	 * Create one of six possible object types.
	 * @param selection - Integer indicating which type of object to create
	 * @param endLines - Whether or not to print a line break after object creation
	 * @return o - A newly created Object
	 */
	public static Object createObject(int selection, boolean endLine) {
		Object o = null;	
		switch (selection) {
			case 1:
				o = createSimpleObject(endLine);
				break;
			case 2:
				o = createReferenceObject(endLine);
				break;
			case 3:
				o = createPrimitiveArrayObject(endLine);
				break;
			case 4:
				o = createReferenceArrayObject(endLine);
				break;
			case 5:
				o = createObjectCollection(endLine);
				break;
		}
		return o;
	}
}
