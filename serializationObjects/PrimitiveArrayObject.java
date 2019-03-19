package serializationObjects;

public class PrimitiveArrayObject {
	
	private int[] intArray;
	
	/**
	 * Primitive Array Object Constructor
	 * @param ints
	 */
	public PrimitiveArrayObject(int[] ints) {
		intArray = ints;
	}
	
	public String toString() {
		String s = "";
		for (int i=0; i<intArray.length; i++) {
			s += String.valueOf(intArray[i]);
			if (i != intArray.length - 1)
				s += ", ";
		}
		return "PrimitiveArrayObject[" + s + "]";
	}

	public int[] getIntArray() {
		return intArray;
	}

	public void setIntArray(int[] intArray) {
		this.intArray = intArray;
	}
}
