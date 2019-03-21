package receiver;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import serializationObjects.*;

public class Deserializer {
	
	private HashMap<Integer, Element> objElementTable;
	
	public Deserializer() {
		objElementTable = new HashMap<Integer, Element>();
	}
	
	public Object deserialize(Document document) {
		Element root = document.getRootElement();
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
	
	public int extractId(Element objElement) {
		return Integer.parseInt(objElement.getAttribute("id").getValue());
	}
}
