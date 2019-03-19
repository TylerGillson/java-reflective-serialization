package serializationObjects;

public class ReferenceObject extends SerializableObject {
	
	private Object objRef;
	
	/**
	 * Reference Object Constructor
	 * @param ref
	 */
	public ReferenceObject(int id, Object ref) {
		super(id);
		objRef = ref;
	}
	
	public String toString() {
		return "ReferenceObject" + String.valueOf(super.getId()) + "[objRef: " + objRef.toString() + "]";
	}
	
	public Object getObjRef() {
		return objRef;
	}

	public void setObjRef(Object objRef) {
		this.objRef = objRef;
	}
}
