package receiver;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import serializationObjects.*;

/**
 * The Deserializer class reconstructs arbitrary objects from
 * org.jdom2.Document instances which contain their serialized
 * field and field value information.
 * 
 * @author tylergillson
 */
public class Deserializer {
	
	private HashMap<Integer, Element> objElementTable;
	
	public Deserializer() {
		objElementTable = new HashMap<Integer, Element>();
	}
	
	/**
	 * Given an org.jdom2.Document instance, extract and re-create
	 * each object which it contains, correctly associating reconstructed
	 * objects in the process.
	 *  
	 * @param doc - The Document undergoing deserialization
	 * @return A reconstructed Object instance
	 */
	public Object deserialize(Document doc) {
		Element root = doc.getRootElement();
		List<Element> objects = root.getChildren();
		Element primaryObj = objects.remove(0);
		
		// Initialize object table:
		for (Element o : objects) {
			int key = extractId(o);
			objElementTable.put(key, o);
		}
			
		Object obj = rebuildObject(primaryObj);
		return obj;
	}
	
	/**
	 * Given an object's XML element, determine if it an array or not and deserialize it accordingly.
	 * 
	 * @param objElement - An object's primary XML element
	 * @return A reconstructed Object instance
	 */
	private Object rebuildObject(Element objElement) {
		Object obj = null;
		
		boolean isArray = objElement.getAttribute("class").getValue().charAt(0) == '[';
		Class<?> classObj = extractClassObject(objElement, "class", isArray);
		int id = extractId(objElement);
		int length = (classObj.isArray()) ? Integer.parseInt(objElement.getAttribute("length").getValue()) : -1;
		
		if (classObj != null) {
			if (classObj.isArray())
				obj = parseArrayElement(objElement, classObj, length, id);
			else
				obj = parseObjectElement(objElement, classObj, id);
		}
		return obj;
	}
	
	/**
	 * Given an array's primary XML element, an instance of its declaring class, its length,
	 * and its id, fully reconstruct the array object it encodes. Iterate over each
	 * <reference> and <value> Element contained by the array's primary XML element, recursively
	 * deserializing objects referenced by <reference> elements, and instantiating wrapper
	 * instances for primitive values indicated by <value> elements.
	 * 
	 * @param objElement - An array's primary XML element
	 * @param classObj - An instance of an array's declaring class
	 * @param length - The length of the array
	 * @param id - The array's id
	 * @return A newly created array containing deserialized objects and/or primitive values
	 */
	private Object parseArrayElement(Element objElement, Class<?> classObj, int length, int id) {
		Class<?> componentType = classObj.getComponentType();
		Object o = Array.newInstance(componentType, length);
		
		int i = 0;
		for (Element entry : objElement.getChildren()) {
			Object arrElem = null;
			
			if (entry.getName().equals("reference")) {
				int refId = Integer.parseInt(entry.getText());
				arrElem = rebuildObject(objElementTable.get(refId));
			}
			else {
				String text = entry.getText();
				switch(componentType.getSimpleName()) {
					case "int":
						arrElem = Integer.parseInt(text);
						break;
					case "boolean":
						arrElem = Boolean.parseBoolean(text);
						break;
				}
			}
			Array.set(o, i++, arrElem);
		}
		return o;
	}
	
	/**
	 * Given an object's primary XML element, an instance of its declaring class,
	 * and its id number, fully reconstruct that object by traversing each <reference>
	 * and <value> Element contained with the primary XML element. Recursively deserialize
	 * each object referenced by <reference> elements, and create wrapper instances for
	 * primitive values contained within <value> elements.
	 * 
	 * @param objElement - An object's primary XML element
	 * @param classObj - An instance of an object's declaring class
	 * @param id - The object's id
	 * @return A newly created object instance
	 */
	private Object parseObjectElement(Element objElement, Class<?> classObj, int id) {
		Object o = extractAndInvokeDefaultConstructor(classObj);
		
		if (o != null) {
			for (Element field : objElement.getChildren()) {
				String fieldName = field.getAttributeValue("name");
				Field f = extractAndEnableField(classObj, fieldName);
				ArrayList<Object> objArrayList = null;
				
				Iterator<Element> iter = field.getChildren().iterator();
				while (iter.hasNext()) {
					Element entry = iter.next();
					String text = entry.getText();
					
					if (entry.getName().equals("reference")) {
						int childId = Integer.parseInt(text);
						Object refObj = rebuildObject(objElementTable.get(childId));
						try {
							if (f.getType().getSimpleName().equals("ArrayList")) {
								if (objArrayList == null)
									objArrayList = new ArrayList<Object>();
								objArrayList.add(refObj);
								
								if (!iter.hasNext())
									f.set(o, objArrayList);
							}
							else
								f.set(o, refObj);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					// Primitive fields
					else {
						try {
							switch(f.getType().getSimpleName()) {
								case "int":
									f.setInt(o, Integer.parseInt(text));
									break;
								case "boolean":
									f.setBoolean(o, Boolean.parseBoolean(text));
									break;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}	
		}
		return o;
	}
	
	/**********************************
	 * DESERIALIZATOIN HELPER METHODS *
	 **********************************/
	
	/**
	 * Given an object's primary XML Element, an attribute name, and a flag indicating
	 * whether or not the object is an array, create and return a new instance of the
	 * object's declaring class.
	 * 
	 * @param objElement - An object's primary XML element
	 * @param attrName - A String indicating which attribute to extract (always either "class" or "declaringclass")
	 * @param isArray - A boolean indicating whether or not the object is an array
	 * @return A newly created instance of the object's declaring class
	 */
	public Class<?> extractClassObject(Element objElement, String attrName, boolean isArray) {
		String className = objElement.getAttribute(attrName).getValue();
		try {
			String loadName = (isArray) ? className : "serializationObjects." + className;
			return Class.forName(loadName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Given an instance of an object's declaring class, acquire its no-arg
	 * constructor and use it to create and return a new instance of the specified class.
	 * 
	 * @param classObj - An instance of an object's declaring class
	 * @return A newly created object instance, instantiated via its no-arg constructor
	 */
	public Object extractAndInvokeDefaultConstructor(Class<?> classObj) {
		try {
			Constructor<?> c = classObj.getDeclaredConstructors()[0];
			c.setAccessible(true);
			return c.newInstance();	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Given an instance of an object's declaring class and a field name,
	 * acquire a Field instance representing that object's specified field.
	 * 
	 * @param classObj - An instance of an object's declaring class
	 * @param fieldName - The name of the field to reconstruct
	 * @return A newly created java.lang.reflect.Field instance 
	 */
	public Field extractAndEnableField(Class<?> classObj, String fieldName) {
		try {
			Field f = classObj.getDeclaredField(fieldName);
			f.setAccessible(true);
			return f;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Given an object's primary XML element, extract and return its id number.
	 * 
	 * @param objElement - An object's primary XML element
	 * @return Its integer id
	 */
	public int extractId(Element objElement) {
		return Integer.parseInt(objElement.getAttribute("id").getValue());
	}
}
