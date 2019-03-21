package sender;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;

/**
 * The Serializer class serializes objects created by an ObjectCreator.
 * 
 * @author tylergillson
 */
public class Serializer {
	
	private static IdentityHashMap<Object, Integer> serializationMap;
	private static IdentityHashMap<Integer, Element> xmlTable;
	private static int id;
	
	public Serializer() {
		serializationMap = new IdentityHashMap<Object, Integer>();
		xmlTable = new IdentityHashMap<Integer, Element>();
		id = 0;
	}
	
	/*************************
	 * SERIALIZATION METHODS *
	 *************************/
	
	/**
	 * Given an arbitrary object from the serializationObjects package,
	 * serialize it into an org.jdom2.Document object.
	 * 
	 * @param obj - The object to serialize
	 * @return An org.jdom2.Document instance
	 */
	public Document serialize(Object obj) {
		Element root = new Element("serialized");
		Document doc = new Document(root);
		toXML(root, obj);
		id++;
		return doc;	
	}
	
	/**
	 * Given a Document with a single root Element, add all necessary object elements
	 * for the object being serialized, as well as any and all objects which it references.
	 * 
	 * @param root - The root Element of the empty Document
	 * @param obj - The object undergoing serialization
	 */
	private void toXML(Element root, Object obj) {
		if (serializationMap.containsKey(obj)) {
			traverseSerializationMap(root, obj);
			return;
		}
		else {
			Class <?> objClass = obj.getClass();
			ObjectElement objElement = initObjectElement(obj, objClass); 
			
			if (objClass.isArray())
				serializeArray(root, objElement.elem, objElement.length, obj, objClass);
			else
				serializeNonArray(root, objElement.elem, obj, objClass);
			
			int key = Integer.parseInt(objElement.elem.getAttribute("id").getValue());
			xmlTable.put(key, objElement.elem);
			serializationMap.put(obj, key);
			root.addContent(0, objElement.elem);
		}
	}

	/**
	 * Create the XML document's primary element.
	 * 
	 * @param obj - The object undergoing serialization
	 * @param c - An instance of obj's declaring class
	 * @return An org.jdom2.Element instance for the object containing attributes
	 * 		   for its class, id, and length (if obj is an array)
	 */
	private ObjectElement initObjectElement(Object obj, Class<?> c) {
		Element objElement = new Element("object");
		objElement.setAttribute(new Attribute("class", c.getSimpleName()));
		objElement.setAttribute(new Attribute("id", String.valueOf(id)));
		
		if (c.isArray()) {
			objElement.setAttribute("class", c.getName());  // override default class representation
			int l = Array.getLength(obj);
			objElement.setAttribute(new Attribute("length", String.valueOf(l)));
			return new ObjectElement(objElement, l);
		}
		return new ObjectElement(objElement);
	}
	
	/**
	 * Recursively serialize an array instance which was encountered within
	 * another object undergoing serialization.
	 * 
	 * @param root - The root Element of the XML document currently being created
	 * @param element - The <reference> Element of the document indicating the id of the array which must be serialized 
	 * @param array - The array object which will be recursively serialized
	 */
	private void addArray(Element root, Element element, Object array) {
		Element entry = new Element("reference");
		
		if (serializationMap.containsKey(array)) {
			int key = serializationMap.get(array);
			entry.setText(String.valueOf(key));
			traverseSerializationMap(root, array);
		}
		else {
			entry.setText(String.valueOf(++id));
			element.addContent(entry);
			toXML(root, array);
		}
	}
	
	/**
	 * Recursively serialize an object instance which was encountered within
	 * another object undergoing serialization.
	 * 
	 * @param root - The root Element of the XML document currently being created
	 * @param childElement - The <reference> Element of the document indicating the id of the array which must be serialized
	 * @param o - The object which will be recursively serialized
	 */
	private void addObject(Element root, Element childElement, Object o) {
		if (serializationMap.containsKey(o)) {
			int key = serializationMap.get(o);
			childElement.setText(String.valueOf(key));
			traverseSerializationMap(root, o);
		}
		else {
			childElement.setText(String.valueOf(++id));
			toXML(root, o);
		}
	}
	
	/**
	 * Iterate over each element of an array instance, serializing each element.
	 * 
	 * @param root - The root Element of the XML document currently being created
	 * @param objElement - The object Element indicating the object currently undergoing serialization
	 * @param length - The length of the array
	 * @param obj - The array object
	 * @param objClass - An instance of obj's declaring class
	 */
	private void serializeArray(Element root, Element objElement, int length, Object obj, Class<?> objClass) {
		for (int i = 0; i < length; i++) {
			Object element = Array.get(obj, i);
			Class<?> elementClass = element.getClass();
			if (elementClass.isArray())
				addArray(root, objElement, element);
			else {
				Element elementEntry = serializePrimitiveOrObject(root, element, elementClass);
				objElement.addContent(elementEntry);	
			}
		}
	}
	
	/**
	 * Serialize an arbitrary object which is not an array by iterating over each of its
	 * fields, acquiring their value(s) and either adding <value> elements for primitive fields
	 * or adding <reference> elements for fields which contain objects. If a field contains an object,
	 * recursively serialize that object.
	 * 
	 * @param root - The root Element of the XML document currently being created
	 * @param objElement - The object Element indicating the object currently undergoing serialization
	 * @param obj - The object instance
	 * @param objClass - An instance of obj's declaring class
	 */
	private void serializeNonArray(Element root, Element objElement, Object obj, Class<?> objClass) {
		Field[] fields = objClass.getDeclaredFields();
		
		for (Field f : fields) {
			// Filter out static fields:
			if (Modifier.isStatic(f.getModifiers()))
				continue;
			
			Element field = new Element("field");
			field.setAttribute(new Attribute("name", f.getName()));
			field.setAttribute(new Attribute("declaringclass", objClass.getSimpleName()));
			f.setAccessible(true);
			
			try {
				Object value = f.get(obj);
				Class<?> valueClass = value.getClass();
				
				if (valueClass.isArray())
					addArray(root, field, value);
				else if (objClass.getSimpleName().equals("CollectionObject")) {
					@SuppressWarnings("unchecked")
					ArrayList<Object> l = (ArrayList<Object>)value;
					for (int i = 0; i < l.size(); i++) {
						Object o = l.get(i);
						Element fieldChild = serializePrimitiveOrObject(root, o, o.getClass());
						field.addContent(fieldChild);
					}
				}
				else {
					Element fieldChild = serializePrimitiveOrObject(root, value, valueClass);
					field.addContent(fieldChild);
				}
				
				objElement.addContent(field);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Given an object create and return a new XML Element.
	 * If the object is primitive, create a <value> Element containing the object's value.
	 * If the object is not primitive, recursively serialize it and create a <reference> 
	 * Element containing the id of the object element created by the recursive serialization.
	 *  
	 * @param root - The root Element of the XML document currently being created
	 * @param o - The object under inspection
	 * @param c - An instance of o's declaring class
	 * @return A new Element which either contains o's value, or a reference to o's recursively generated object element.
	 */
	private Element serializePrimitiveOrObject(Element root, Object o, Class<?> c) {
		Element entry = new Element("null");
		
		if (isPrimitive(c)) {		
			entry.setName("value");
			entry.setText(String.valueOf(o));
		} 
		else {
			entry.setName("reference");
			addObject(root, entry, o);
		}
		return entry;
	}
	
	/**
	 * Internal data class used to create XML object Elements
	 * for either generic Objects, or arrays.
	 * 
	 * @author tylergillson
	 */
	private static class ObjectElement {
		Element elem;
		int length;
		
		private ObjectElement(Element e) {
			elem = e;
		}
		
		private ObjectElement(Element e, int l) {
			elem = e;
			length = l;
		}
	}
	
	/**
	 * Check if a class instance is a primitive or a primitive wrapper.
	 * Source: https://stackoverflow.com/questions/209366/how-can-i-generically-tell-if-a-java-class-is-a-primitive-type
	 * 
	 * @param c - A Class instance
	 * @return Whether c is primitive type itself or if it's a wrapper for a primitive type
	 */
	private static boolean isPrimitive(Class<?> c) {
		if (c.isPrimitive())
			return true;
		else if (c == Byte.class
				|| c == Short.class
				|| c == Integer.class
				|| c == Long.class
				|| c == Float.class
				|| c == Double.class
				|| c == Boolean.class
				|| c == Character.class) {
			return true;
		}
		return false;
	}
	
	/*******************************
	 * Serialization Map Traversal *
	 *******************************/
	
	/**
	 * Given an object, traverse each of its fields and their values, adding
	 * all necessary XML elements to the root Element which was provided.
	 * This prevents unnecessary re-serialization.
	 * 
	 * @param root - The root Element to append pre-serialized Elements to
	 * @param obj - The object whose fields will be traversed
	 */
	private void traverseSerializationMap(Element root, Object obj) {
		int key = serializationMap.get(obj);
		addReference(root, key);
		Element xml = xmlTable.get(key);
		
		Iterator<Element> iter = xml.getChildren().iterator();
		while (iter.hasNext()) {
			Element child = iter.next();
			String childName = child.getName();
			
			if (childName.equals("field")) {
				Iterator<Element> childIter = child.getChildren().iterator();
				
				while (childIter.hasNext()) {
					Element e = childIter.next();
					if (e.getName().equals("reference"))
						addReference(root, e);
				}
			}
			else if (childName.equals("reference"))
				addReference(root, child);	
		}
	}
	
	/**
	 * Given an XML <reference> Element, extract its id, use that id to add
	 * the object element of the previously serialized object it references
	 * to the XML Document currently being created. Next, iterate over the 
	 * serialization map to recursively add any other object elements which
	 * are referenced by the object being referenced.
	 *  
	 * @param root - The root Element of the XML document currently being created
	 * @param e - A <reference> Element
	 */
	private void addReference(Element root, Element e) {
		int key = Integer.parseInt(e.getText());
		addReference(root, key);
		
		for (Map.Entry<Object, Integer> entry : serializationMap.entrySet()) {
			if (entry.getValue() == key) {
				traverseSerializationMap(root, entry.getKey());
			}
		}
	}
	
	/**
	 * Given a key into the XML table, acquire the corresponding Element
	 * and append it to the provided root Element of the Document being created.
	 * 
	 * @param root - The root Element of the XML document currently being created
	 * @param key - An integer key into the XML table
	 */
	private void addReference(Element root, int key) {
		Element xml = xmlTable.get(key);
		xml.detach();
		root.addContent(xml);
	}
}
