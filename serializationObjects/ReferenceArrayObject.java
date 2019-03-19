package serializationObjects;

public class ReferenceArrayObject {
	
	private Object[] refArray;
	
	/**
	 * Reference Array Object Constructor
	 * @param refs
	 */
	public ReferenceArrayObject(Object[] refs) {
		refArray = refs;
	}
	
	public String toString() {
		String s = "";
		for (int i=0; i<refArray.length; i++) {
			s += refArray[i].toString();
			if (i != refArray.length - 1)
				s += ", ";
		}
		return "ReferenceArrayObject[" + s + "]";
	}
	
	public Object[] getRefArray() {
		return refArray;
	}

	public void setRefArray(Object[] refArray) {
		this.refArray = refArray;
	}
}
