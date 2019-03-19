package serializationObjects;

public class SimpleObject extends SerializableObject {
	
	private int intField;	   				
	private boolean boolField;  				
	
	/**
	 * Simple Object Constructor
	 * @param i
	 * @param b
	 */
	public SimpleObject(int id, int i, boolean b) {
		super(id);
		intField = i;
		boolField = b;
	}
	
	public String toString() {
		return "SimpleObject" + String.valueOf(super.getId()) + "[intField: " + String.valueOf(intField) + ", boolField: " + String.valueOf(boolField) + "]"; 
	}

	public int getIntField() {
		return intField;
	}

	public void setIntField(int intField) {
		this.intField = intField;
	}

	public boolean getBoolField() {
		return boolField;
	}

	public void setBoolField(boolean boolField) {
		this.boolField = boolField;
	}
}
