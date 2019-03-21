package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sender.Serializer;
import serializationObjects.*;

public class TestSerializer {
	
	private Serializer serializer;
	private SimpleObject simpleObj;
	private ReferenceObject refObj;
	private PrimitiveArrayObject primitiveArrayObj;
	private ReferenceArrayObject refArrayObj;
	private CollectionObject collectionObj;
	
	@BeforeEach
	void setUp() throws Exception {
		serializer = new Serializer();
		simpleObj = new SimpleObject(1, true);
		refObj = new ReferenceObject(simpleObj);
		primitiveArrayObj = new PrimitiveArrayObject(new int[] {1, 2, 3});
		refArrayObj = new ReferenceArrayObject(new Object[] {simpleObj, refObj});
		collectionObj = new CollectionObject(new ArrayList<Object>(Arrays.asList(primitiveArrayObj, refArrayObj)));
	}

	@AfterEach
	void tearDown() throws Exception {
		serializer = null;
		simpleObj = null;
		refObj = null;
		primitiveArrayObj = null;
		refArrayObj = null;
		collectionObj = null;
	}
	
	@Test
	void testSerializeSimpleObject() {
		Document doc = serializer.serialize(simpleObj);
		List<Element> fields = doc.getRootElement().getChildren().get(0).getChildren();
		assertEquals(simpleObj.getIntField(), Integer.parseInt(fields.get(0).getChildren().get(0).getText()));
		assertEquals(simpleObj.getBoolField(), Boolean.parseBoolean(fields.get(1).getChildren().get(0).getText()));
	}
	
	@Test
	void testSerializeReferenceObject() {
		Document doc = serializer.serialize(refObj);
		Element refObjElement = doc.getRootElement().getChildren().get(0);
		Element simpleObjElement = doc.getRootElement().getChildren().get(1);
		
		// Check that IDs match:
		String simpleObjElementId = simpleObjElement.getAttribute("id").getValue();
		String refId = refObjElement.getChildren().get(0).getChildren().get(0).getText();
		assertEquals(simpleObjElementId, refId);
		
		// Check that simpleObj was properly serialized:
		int simpleObjIntField = Integer.parseInt(simpleObjElement.getChildren().get(0).getChildren().get(0).getText());
		boolean simpleObjBoolField = Boolean.parseBoolean(simpleObjElement.getChildren().get(1).getChildren().get(0).getText());
		assertEquals(simpleObj.getIntField(), simpleObjIntField);
		assertEquals(simpleObj.getBoolField(), simpleObjBoolField);
	}
	
	@Test
	void testSerializePrimitiveArrayObject() {
		Document doc = serializer.serialize(primitiveArrayObj);
		Element primitiveArrayObjElement = doc.getRootElement().getChildren().get(0);
		Element arrayElement = doc.getRootElement().getChildren().get(1);
		
		// Check that IDs match:
		String arrayElementId = arrayElement.getAttribute("id").getValue();
		String refId = primitiveArrayObjElement.getChildren().get(0).getChildren().get(0).getText();
		assertEquals(arrayElementId, refId);
		
		// Check that primitiveArrayObj was properly serialized:
		List<Element> arrayElements = arrayElement.getChildren();
		int arr0 = Integer.parseInt(arrayElements.get(0).getText());
		int arr1 = Integer.parseInt(arrayElements.get(1).getText());
		int arr2 = Integer.parseInt(arrayElements.get(2).getText());
		assertEquals(arr0, primitiveArrayObj.getIntArray()[0]);
		assertEquals(arr1, primitiveArrayObj.getIntArray()[1]);
		assertEquals(arr2, primitiveArrayObj.getIntArray()[2]);
	}
	
	@Test
	void testSerializeReferenceArrayObject() {
		Document doc = serializer.serialize(refArrayObj);
		Element refArrayObjElement = doc.getRootElement().getChildren().get(0);
		Element arrayElement = doc.getRootElement().getChildren().get(1);
		Element simpleObjElement = doc.getRootElement().getChildren().get(3);
		Element refObjElement = doc.getRootElement().getChildren().get(2);
		List<Element> arrayElements = arrayElement.getChildren();
		
		// Check that IDs match:
		String arrayElementId = arrayElement.getAttribute("id").getValue();
		String refId = refArrayObjElement.getChildren().get(0).getChildren().get(0).getText();
		String simpleObjElementId = simpleObjElement.getAttribute("id").getValue();
		String refObjElementId = refObjElement.getAttribute("id").getValue();
		String refObjElementRefId = refObjElement.getChildren().get(0).getChildren().get(0).getText();
		String simpleObjRefId = arrayElements.get(0).getText();
		String refObjRefId = arrayElements.get(1).getText();
		assertEquals(arrayElementId, refId);				   // ref array object references its array field
		assertEquals(simpleObjElementId, simpleObjRefId);	   // array[0] references simple object
		assertEquals(refObjElementId, refObjRefId);			   // array[1] references reference object
		assertEquals(refObjElementRefId, simpleObjElementId);  // reference object references simple object
		
		// Check that simpleObj was properly serialized:
		int simpleObjIntField = Integer.parseInt(simpleObjElement.getChildren().get(0).getChildren().get(0).getText());
		boolean simpleObjBoolField = Boolean.parseBoolean(simpleObjElement.getChildren().get(1).getChildren().get(0).getText());
		assertEquals(simpleObj.getIntField(), simpleObjIntField);
		assertEquals(simpleObj.getBoolField(), simpleObjBoolField);
	}
	
	@Test
	void testSerializeCollectionObject() {
		Document doc = serializer.serialize(collectionObj);
		Element collectionObjElement = doc.getRootElement().getChildren().get(0);
		Element primitiveArrayObjElement = doc.getRootElement().getChildren().get(4);
		Element primitiveArrayElement = doc.getRootElement().getChildren().get(5);
		Element refArrayObjElement = doc.getRootElement().getChildren().get(1);
		Element refArrayElement = doc.getRootElement().getChildren().get(2);
		Element simpleObjElement = doc.getRootElement().getChildren().get(6);
		Element refObjElement = doc.getRootElement().getChildren().get(3);
		
		// Check that IDs match:
		List<Element> collectionArray = collectionObjElement.getChildren().get(0).getChildren();
		List<Element> referenceArray = refArrayElement.getChildren();
		String collectionObjRef1 = collectionArray.get(0).getText();
		String collectionObjRef2 = collectionArray.get(1).getText();
		String primitiveArrayObjElementId = primitiveArrayObjElement.getAttribute("id").getValue();
		String primitiveArrayRef = primitiveArrayObjElement.getChildren().get(0).getChildren().get(0).getText();
		String primitiveArrayElementId = primitiveArrayElement.getAttribute("id").getValue();
		String refArrayObjElementId = refArrayObjElement.getAttribute("id").getValue();
		String refArrayElementId = refArrayElement.getAttribute("id").getValue();
		String refArrayObjRef = refArrayObjElement.getChildren().get(0).getChildren().get(0).getText();
		String refArrayRef1 = referenceArray.get(0).getText();
		String refArrayRef2 = referenceArray.get(1).getText();
		String simpleObjElementId = simpleObjElement.getAttribute("id").getValue();
		String refObjElementId = refObjElement.getAttribute("id").getValue();
		String refObjRef = refObjElement.getChildren().get(0).getChildren().get(0).getText();
		assertEquals(collectionObjRef1, primitiveArrayObjElementId);
		assertEquals(collectionObjRef2, refArrayObjElementId);
		assertEquals(primitiveArrayRef, primitiveArrayElementId);
		assertEquals(refArrayObjRef, refArrayElementId);
		assertEquals(refArrayRef1, simpleObjElementId);
		assertEquals(refArrayRef2, refObjElementId);
		assertEquals(refObjRef, simpleObjElementId);
		
		// Check that simpleObj was properly serialized:
		int simpleObjIntField = Integer.parseInt(simpleObjElement.getChildren().get(0).getChildren().get(0).getText());
		boolean simpleObjBoolField = Boolean.parseBoolean(simpleObjElement.getChildren().get(1).getChildren().get(0).getText());
		assertEquals(simpleObj.getIntField(), simpleObjIntField);
		assertEquals(simpleObj.getBoolField(), simpleObjBoolField);
		
		// Check that primitiveArrayObj was properly serialized:
		List<Element> arrayElements = primitiveArrayElement.getChildren();
		int arr0 = Integer.parseInt(arrayElements.get(0).getText());
		int arr1 = Integer.parseInt(arrayElements.get(1).getText());
		int arr2 = Integer.parseInt(arrayElements.get(2).getText());
		assertEquals(arr0, primitiveArrayObj.getIntArray()[0]);
		assertEquals(arr1, primitiveArrayObj.getIntArray()[1]);
		assertEquals(arr2, primitiveArrayObj.getIntArray()[2]);
	}
}

