package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import org.jdom2.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import receiver.Deserializer;
import sender.Serializer;
import serializationObjects.CollectionObject;
import serializationObjects.PrimitiveArrayObject;
import serializationObjects.ReferenceArrayObject;
import serializationObjects.ReferenceObject;
import serializationObjects.SimpleObject;

public class TestDeserializer {
	private Serializer serializer;
	private Deserializer deserializer;
	private SimpleObject simpleObj;
	private ReferenceObject refObj;
	private PrimitiveArrayObject primitiveArrayObj;
	private ReferenceArrayObject refArrayObj;
	private CollectionObject collectionObj;
	
	@BeforeEach
	void setUp() throws Exception {
		serializer = new Serializer();
		deserializer = new Deserializer();
		simpleObj = new SimpleObject(1, true);
		refObj = new ReferenceObject(simpleObj);
		primitiveArrayObj = new PrimitiveArrayObject(new int[] {1, 2, 3});
		refArrayObj = new ReferenceArrayObject(new Object[] {simpleObj, refObj});
		collectionObj = new CollectionObject(new ArrayList<Object>(Arrays.asList(primitiveArrayObj, refArrayObj)));
	}

	@AfterEach
	void tearDown() throws Exception {
		serializer = null;
		deserializer = null;
		simpleObj = null;
		refObj = null;
		primitiveArrayObj = null;
		refArrayObj = null;
		collectionObj = null;
	}
	
	@Test
	void testDeserializeSimpleObject() {
		Document doc = serializer.serialize(simpleObj);
		Object o = deserializer.deserialize(doc);
		
		Class<?> classObj = o.getClass();
		assertEquals(classObj.getSimpleName(), "SimpleObject");
		
		Field[] fields = classObj.getDeclaredFields();
		for (Field f : fields)
			f.setAccessible(true);
		
		try {
			assertEquals(fields[0].get(o), simpleObj.getIntField());
			assertEquals(fields[1].get(o), simpleObj.getBoolField());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	void testDeserializeReferenceObject() {
		Document doc = serializer.serialize(refObj);
		Object o = deserializer.deserialize(doc);
		
		Class<?> classObj = o.getClass();
		assertEquals(classObj.getSimpleName(), "ReferenceObject");
		
		Field[] fields = classObj.getDeclaredFields();
		for (Field f : fields)
			f.setAccessible(true);
		
		try {
			Object sObj = fields[0].get(o);
			Class<?> sClassObj = sObj.getClass();
			assertEquals(sClassObj.getSimpleName(), "SimpleObject");
			
			Field[] sFields = sClassObj.getDeclaredFields();
			for (Field f : sFields)
				f.setAccessible(true);
			
			assertEquals(sFields[0].get(sObj), simpleObj.getIntField());
			assertEquals(sFields[1].get(sObj), simpleObj.getBoolField());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	void testDeserializePrimitiveArrayObject() {
		Document doc = serializer.serialize(primitiveArrayObj);
		Object o = deserializer.deserialize(doc);
		
		Class<?> classObj = o.getClass();
		assertEquals(classObj.getSimpleName(), "PrimitiveArrayObject");
		
		Field[] fields = classObj.getDeclaredFields();
		for (Field f : fields)
			f.setAccessible(true);
		
		try {
			Object ints = fields[0].get(o);
			assertEquals(Array.get(ints, 0), primitiveArrayObj.getIntArray()[0]);
			assertEquals(Array.get(ints, 1), primitiveArrayObj.getIntArray()[1]);
			assertEquals(Array.get(ints, 2), primitiveArrayObj.getIntArray()[2]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	void testDeserializeReferenceArrayObject() {
		Document doc = serializer.serialize(refArrayObj);
		Object o = deserializer.deserialize(doc);
		
		Class<?> classObj = o.getClass();
		assertEquals(classObj.getSimpleName(), "ReferenceArrayObject");
		
		Field[] fields = classObj.getDeclaredFields();
		for (Field f : fields)
			f.setAccessible(true);	
		
		try {
			Object refs = fields[0].get(o);
			Object sObj = Array.get(refs, 0);
			Object rObj = Array.get(refs, 1);
			
			// SIMPLE OBJ
			Class<?> sClassObj = sObj.getClass();
			assertEquals(sClassObj.getSimpleName(), "SimpleObject");
			
			Field[] sFields = sClassObj.getDeclaredFields();
			for (Field f : sFields)
				f.setAccessible(true);
			
			assertEquals(sFields[0].get(sObj), simpleObj.getIntField());
			assertEquals(sFields[1].get(sObj), simpleObj.getBoolField());
			
			// REFERENCE OBJ
			Class<?> rClassObj = rObj.getClass();
			assertEquals(rClassObj.getSimpleName(), "ReferenceObject");
			
			Field[] rFields = rClassObj.getDeclaredFields();
			for (Field f : rFields)
				f.setAccessible(true);
			
			Object sObj2 = rFields[0].get(rObj);
			Class<?> sClassObj2 = sObj2.getClass();
			assertEquals(sClassObj2.getSimpleName(), "SimpleObject");
			
			Field[] sFields2 = sClassObj2.getDeclaredFields();
			for (Field f : sFields2)
				f.setAccessible(true);
			
			assertEquals(sFields2[0].get(sObj2), simpleObj.getIntField());
			assertEquals(sFields2[1].get(sObj2), simpleObj.getBoolField());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	void testDeserializeCollectionObject() {
		Document doc = serializer.serialize(collectionObj);
		Object o = deserializer.deserialize(doc);
		
		Class<?> classObj = o.getClass();
		assertEquals(classObj.getSimpleName(), "CollectionObject");
		
		Field[] fields = classObj.getDeclaredFields();
		for (Field f : fields)
			f.setAccessible(true);	
		
		try {
			Object oField = fields[0].get(o);
			@SuppressWarnings("unchecked")
			ArrayList<Object> arrList = ((ArrayList<Object>)oField);
			Object pObj = arrList.get(0);
			Object rArrayObj = arrList.get(1);
			
			// PRIMITIVE ARRAY OBJ
			Class<?> pclassObj = pObj.getClass();
			assertEquals(pclassObj.getSimpleName(), "PrimitiveArrayObject");
			
			Field[] pfields = pclassObj.getDeclaredFields();
			for (Field f : pfields)
				f.setAccessible(true);
			
			Object ints = pfields[0].get(pObj);
			assertEquals(Array.get(ints, 0), primitiveArrayObj.getIntArray()[0]);
			assertEquals(Array.get(ints, 1), primitiveArrayObj.getIntArray()[1]);
			assertEquals(Array.get(ints, 2), primitiveArrayObj.getIntArray()[2]);
			
			// REFERENCE ARRAY OBJ
			Class<?> rArrayClassObj = rArrayObj.getClass();
			assertEquals(rArrayClassObj.getSimpleName(), "ReferenceArrayObject");
			
			Field[] rArrayFields = rArrayClassObj.getDeclaredFields();
			for (Field f : rArrayFields)
				f.setAccessible(true);	
			
			Object refs = rArrayFields[0].get(rArrayObj);
			Object sObj = Array.get(refs, 0);
			Object rObj = Array.get(refs, 1);
			
			// SIMPLE OBJ
			Class<?> sClassObj = sObj.getClass();
			assertEquals(sClassObj.getSimpleName(), "SimpleObject");
			
			Field[] sFields = sClassObj.getDeclaredFields();
			for (Field f : sFields)
				f.setAccessible(true);
			
			assertEquals(sFields[0].get(sObj), simpleObj.getIntField());
			assertEquals(sFields[1].get(sObj), simpleObj.getBoolField());
			
			// REFERENCE OBJ
			Class<?> rClassObj = rObj.getClass();
			assertEquals(rClassObj.getSimpleName(), "ReferenceObject");
			
			Field[] rFields = rClassObj.getDeclaredFields();
			for (Field f : rFields)
				f.setAccessible(true);
			
			Object sObj2 = rFields[0].get(rObj);
			Class<?> sClassObj2 = sObj2.getClass();
			assertEquals(sClassObj2.getSimpleName(), "SimpleObject");
			
			Field[] sFields2 = sClassObj2.getDeclaredFields();
			for (Field f : sFields2)
				f.setAccessible(true);
			
			assertEquals(sFields2[0].get(sObj2), simpleObj.getIntField());
			assertEquals(sFields2[1].get(sObj2), simpleObj.getBoolField());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
