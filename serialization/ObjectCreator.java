package serialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

import serializationObjects.CollectionObject;
import serializationObjects.PrimitiveArrayObject;
import serializationObjects.ReferenceArrayObject;
import serializationObjects.ReferenceObject;
import serializationObjects.SimpleObject;

public class ObjectCreator {
	
	private Scanner sc;
	private HashMap<Integer, Object> objHashMap;
	private int objCounter;
	
	public ObjectCreator(Scanner s) {
		sc = s;
		objHashMap = new HashMap<Integer, Object>();
		objCounter = 0;
	}
	
	public int getObjCounter() {
		return objCounter;
	}
	
	public Object getObj(int id) {
		return objHashMap.get(id);
	}
	
	/**********************************
	 * Object Creation Methods *
	 **********************************/
	
	/**
	 * Create a new SimpleObject instance.
	 * @param endLine - Whether or not to print a line break after object creation
	 * @return simpleObj - A newly created SimpleObject
	 */
	public SimpleObject createSimpleObject(boolean endLine) {
		System.out.println("\nCreating new simple object...");

		int i = getIntInput(false, 0);
		
		System.out.print("Enter a boolean: ");
		while (!sc.hasNextBoolean()) {
			sc.next();
			System.out.print("Enter a valid boolean: ");	
		}
		boolean b = sc.nextBoolean();
		
		SimpleObject simpleObj = new SimpleObject(i, b);
		objHashMap.put(objCounter++, simpleObj);
		
		System.out.println("Simple object created.");
		if (endLine) System.out.println();
		return simpleObj;
	}
	
	/**
	 * Create a new ReferenceObject instance.
	 * @param endLine - Whether or not to print a line break after object creation
	 * @return refObj - A newly created ReferenceObject
	 */
	public ReferenceObject createReferenceObject(boolean endLine) {
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
				if (recRefObj != null)
					refObj = new ReferenceObject(recRefObj);
				break;
			case 3:
				if (objCounter == 0)
					System.out.println("No objects have been created yet.\n");
				else {
					printExistingObjects();
					
					prompt = "\nEnter the id number of the object you wish to reference: ";
					if (objCounter > 1)
						errorMsg = "Please enter a number from 0-" + String.valueOf(objCounter - 1);
					else
						errorMsg = "You must enter 0, as only one object exists.";
						
					selection = getIntSelection(prompt, errorMsg, 0, objCounter);
					refObj = new ReferenceObject(objHashMap.get(selection));
				}
				break;
		}
		
		if (refObj != null) {
			objHashMap.put(objCounter++, refObj);
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
	public PrimitiveArrayObject createPrimitiveArrayObject(boolean endLine) {
		System.out.println("\nCreating new primitive array... how many elements should it have?");
		int length = getIntInput(true, 1);
		int[] ints = new int[length];
		
		// Initialize its elements:
		System.out.println("Integer array initialized. Please provide values for its elements.");
		for (int i=0; i<length; i++) {
			ints[i] = getIntInput(false, 0);
		}
		
		PrimitiveArrayObject primitiveArrayObj = new PrimitiveArrayObject(ints);
		objHashMap.put(objCounter++, primitiveArrayObj);
		
		System.out.println("Primitive array object created.");
		if (endLine) System.out.println();
		return primitiveArrayObj;
	}
	
	/**
	 * Create a new ReferenceArrayObject instance.
	 * @param endLine - Whether or not to print a line break after object creation
	 * @return referenceArrayObj - A newly created ReferenceArrayObject
	 */
	public ReferenceArrayObject createReferenceArrayObject(boolean endLine) {
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
		objHashMap.put(objCounter++, referenceArrayObj);
		
		System.out.println("Reference array object created.");
		if (endLine) System.out.println();
		return referenceArrayObj;
	}
	
	/**
	 * Create a new CollectionObject instance.
	 * @param endLine - Whether or not to print a line break after object creation
	 * @return collectionObj - A newly created CollectionObject
	 */
	public CollectionObject createObjectCollection(boolean endLine) {
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
		objHashMap.put(objCounter++, collectionObj);
		
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
	public void printMenu(boolean preLine) {
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
	public int getIntInput(boolean useMin, int min) {
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
	public int getIntSelection(String prompt, String errorMsg, int min, int max) {
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
	public void printExistingObjects() {
		System.out.println("Existing objects:");
		for (Entry<Integer, Object> e : objHashMap.entrySet())
			System.out.println("\tID #" + String.valueOf(e.getKey()) + ": " + e.getValue().toString());
	}
	
	/**
	 * Create one of six possible object types.
	 * @param selection - Integer indicating which type of object to create
	 * @param endLines - Whether or not to print a line break after object creation
	 * @return o - A newly created Object
	 */
	public Object createObject(int selection, boolean endLine) {
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
