package serialization;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;

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
		return doc;	
	}
	
	private void toXML(Element root, Object obj) {
		if (serializationMap.containsKey(obj)) {
			int key = serializationMap.get(obj);
			Element serialized = xmlTable.get(key);
			serialized.detach();
			root.addContent(serialized);
			/*
			 * NEED TO CHECK CHILDREN FOR <reference> ELEMENTS & PERFORM ADDITIONAL LOOKUPS!!!
			 */
			return;
		}
		else {
			Class <?> objClass = obj.getClass();
			ObjectElement objElement = initObjectElement(obj, objClass); 
			
			if (objClass.isArray())
				serializeArray(root, objElement.elem, objElement.length, obj, objClass);
			else
				serializeNonArray(root, objElement.elem, obj, objClass);
			
			serializationMap.put(obj, id++);
			xmlTable.put(id, objElement.elem);
			root.addContent(objElement.elem);
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
	
	private void serializeArray(Element root, Element objElement, int length, Object obj, Class<?> objClass) {
		for (int i = 0; i < length; i++) {
			Object element = Array.get(obj, i);
			Class<?> elementClass = element.getClass();
			Element elementEntry = serializeFieldOrElement(root, element, elementClass);
			objElement.addContent(elementEntry);
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
				Object val = f.get(obj);
				Class<?> valClass = val.getClass();
				
				if (valClass.isArray()) {
					Element entry = new Element("reference");
					entry.setText(String.valueOf(++id));
					field.addContent(entry);
					toXML(root, val);
				}
				else if (valClass.isInstance(Collection.class)) {
					@SuppressWarnings("unchecked")
					ArrayList<Integer> l = (ArrayList<Integer>)val;
					for (int i = 0; i < l.size(); i++) {
						Element entry = new Element("reference");
						entry.setText(String.valueOf(++id));
						field.addContent(entry);
						toXML(root, val);
					}
				}
				else {
					Element fieldChild = serializeFieldOrElement(root, val, valClass);
					field.addContent(fieldChild);
				}
				
				objElement.addContent(field);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private Element serializeFieldOrElement(Element root, Object o, Class<?> c) {
		Element entry = new Element("null");
		
		if (isPrimitive(c)) {		
			entry.setName("value");
			entry.setText(String.valueOf(o));
		} 
		else {
			entry.setName("reference");
			entry.setText(String.valueOf(++id));
			toXML(root, o);
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
}
