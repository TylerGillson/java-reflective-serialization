package serializationObjects;

public class PrimitiveArrayObject {
	private final int idNum;
	private int[] intArray;
	
	/**
	 * Primitive Array Object Constructor
	 * @param ints
	 */
	public PrimitiveArrayObject(int id, int[] ints) {
		idNum = id;
		intArray = ints;
	}
	
	public String toString() {
		String s = "";
		for (int i=0; i<intArray.length; i++) {
			s += String.valueOf(intArray[i]);
			if (i != intArray.length - 1)
				s += ", ";
		}
		return "PrimitiveArrayObject" + String.valueOf(idNum) + "[" + s + "]";
	}

	public int[] getIntArray() {
		return intArray;
	}

	public void setIntArray(int[] intArray) {
		this.intArray = intArray;
	}
}
