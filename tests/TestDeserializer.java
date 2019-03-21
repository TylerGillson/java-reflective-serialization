package tests;

import static org.junit.jupiter.api.Assertions.*;

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
		SimpleObject o = (SimpleObject)deserializer.deserialize(doc);
		
		assertEquals(o.getClass(), simpleObj.getClass());
		assertEquals(o.getIntField(), simpleObj.getIntField());
		assertEquals(o.getBoolField(), simpleObj.getBoolField());
	}
	
	@Test
	void testDeserializeReferenceObject() {
		Document doc = serializer.serialize(refObj);
		ReferenceObject o = (ReferenceObject)deserializer.deserialize(doc);
		SimpleObject sObj = (SimpleObject)o.getObjRef();
		
		assertEquals(o.getClass(), refObj.getClass());
		assertEquals(sObj.getClass(), simpleObj.getClass());
		assertEquals(sObj.getIntField(), simpleObj.getIntField());
		assertEquals(sObj.getBoolField(), simpleObj.getBoolField());
	}
	
	@Test
	void testDeserializePrimitiveArrayObject() {
		Document doc = serializer.serialize(primitiveArrayObj);
		PrimitiveArrayObject o = (PrimitiveArrayObject)deserializer.deserialize(doc);
		int[] pInts = primitiveArrayObj.getIntArray();
		int[] ints = o.getIntArray();
		
		assertEquals(o.getClass(), primitiveArrayObj.getClass());
		assertEquals(ints[0], pInts[0]);
		assertEquals(ints[1], pInts[1]);
		assertEquals(ints[2], pInts[2]);
	}
	
	@Test
	void testDeserializeReferenceArrayObject() {
		Document doc = serializer.serialize(refArrayObj);
		ReferenceArrayObject o = (ReferenceArrayObject)deserializer.deserialize(doc);
		Object[] refs = o.getRefArray();
		SimpleObject sObj = (SimpleObject)refs[0];
		ReferenceObject rObj = (ReferenceObject)refs[1];
		SimpleObject refSObj = (SimpleObject)rObj.getObjRef();
		
		assertEquals(o.getClass(), refArrayObj.getClass());
		assertEquals(sObj.getClass(), simpleObj.getClass());
		assertEquals(sObj.getIntField(), simpleObj.getIntField());
		assertEquals(sObj.getBoolField(), simpleObj.getBoolField());
		assertEquals(rObj.getClass(), refObj.getClass());
		assertEquals(refSObj.getClass(), simpleObj.getClass());
		assertEquals(refSObj.getIntField(), simpleObj.getIntField());
		assertEquals(refSObj.getBoolField(), simpleObj.getBoolField());
	}
	
	@Test
	void testDeserializeCollectionObject() {
		Document doc = serializer.serialize(collectionObj);
		CollectionObject o = (CollectionObject)deserializer.deserialize(doc);
		ArrayList<Object> collectionArray = o.getRefArrayList();
		PrimitiveArrayObject pObj = (PrimitiveArrayObject)collectionArray.get(0);
		ReferenceArrayObject rObj = (ReferenceArrayObject)collectionArray.get(1);
		int[] ints = pObj.getIntArray();
		Object[] refs = rObj.getRefArray();
		SimpleObject refSObj = (SimpleObject)refs[0];
		ReferenceObject refRObj = (ReferenceObject)refs[1];
		SimpleObject refRefSObj = (SimpleObject)refRObj.getObjRef();
		
		assertEquals(o.getClass(), collectionObj.getClass());
		assertEquals(pObj.getClass(), primitiveArrayObj.getClass());
		assertEquals(rObj.getClass(), refArrayObj.getClass());
		assertEquals(refSObj.getClass(), simpleObj.getClass());
		assertEquals(refRObj.getClass(), refObj.getClass());
		assertEquals(refRefSObj.getClass(), simpleObj.getClass());
		
		assertEquals(ints[0], primitiveArrayObj.getIntArray()[0]);
		assertEquals(ints[1], primitiveArrayObj.getIntArray()[1]);
		assertEquals(ints[2], primitiveArrayObj.getIntArray()[2]);
		assertEquals(refSObj.getIntField(), simpleObj.getIntField());
		assertEquals(refSObj.getBoolField(), simpleObj.getBoolField());
		assertEquals(refRefSObj.getIntField(), simpleObj.getIntField());
		assertEquals(refRefSObj.getBoolField(), simpleObj.getBoolField());
	}
}
