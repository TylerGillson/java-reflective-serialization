package serializationObjects;

public class ReferenceObject {
	private final int idNum;
	private Object objRef;
	
	/**
	 * Reference Object Constructor
	 * @param ref
	 */
	public ReferenceObject(int id, Object ref) {
		idNum = id;
		objRef = ref;
	}
	
	public String toString() {
		return "ReferenceObject" + String.valueOf(idNum) + "[objRef: " + objRef.toString() + "]";
	}
	
	public Object getObjRef() {
		return objRef;
	}

	public void setObjRef(Object objRef) {
		this.objRef = objRef;
	}
}
