package serialization;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;

public class Serializer {
	
	private static IdentityHashMap<Object, Integer> serializationMap;
	private static IdentityHashMap<Integer, Element> xmlTable;
	private static int id;
	
	public Serializer() {
		serializationMap = new IdentityHashMap<Object, Integer>();
		xmlTable = new IdentityHashMap<Integer, Element>();
		id = 0;
	}
	
	public Document serialize(Object obj) {
		Element root = new Element("serialized");
		Document doc = new Document(root);
		toXML(root, obj);
		id++;
		return doc;	
	}
	
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
	
	private void addReference(Element root, Element e) {
		int key = Integer.parseInt(e.getText());
		addReference(root, key);
		
		for (Map.Entry<Object, Integer> entry : serializationMap.entrySet()) {
			if (entry.getValue() == key) {
				traverseSerializationMap(root, entry.getKey());
			}
		}
	}
	
	private void addReference(Element root, int key) {
		Element xml = xmlTable.get(key);
		xml.detach();
		root.addContent(xml);
	}
}
