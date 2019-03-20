package serializationObjects;

public class SimpleObject {
	
	private int intField;	   				
	private boolean boolField;  				
	
	public SimpleObject() {}
	
	public SimpleObject(int i, boolean b) {
		intField = i;
		boolField = b;
	}
	
	public String toString() {
		return "SimpleObject[intField: " + String.valueOf(intField) + ", boolField: " + String.valueOf(boolField) + "]"; 
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
