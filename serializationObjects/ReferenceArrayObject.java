package serializationObjects;

public class ReferenceArrayObject {
	private final int idNum;
	private Object[] refArray;
	
	/**
	 * Reference Array Object Constructor
	 * @param refs
	 */
	public ReferenceArrayObject(int id, Object[] refs) {
		idNum = id;
		refArray = refs;
	}
	
	public String toString() {
		String s = "";
		for (int i=0; i<refArray.length; i++) {
			s += refArray[i].toString();
			if (i != refArray.length - 1)
				s += ", ";
		}
		return "ReferenceArrayObject" + String.valueOf(idNum) + "[" + s + "]";
	}
	
	public Object[] getRefArray() {
		return refArray;
	}

	public void setRefArray(Object[] refArray) {
		this.refArray = refArray;
	}
}
