package serializationObjects;

public class ReferenceObject {
	
	private Object objRef;
	
	public ReferenceObject() {}
	
	public ReferenceObject(Object ref) {
		objRef = ref;
	}
	
	public String toString() {
		return "ReferenceObject[objRef: " + objRef.toString() + "]";
	}
	
	public Object getObjRef() {
		return objRef;
	}

	public void setObjRef(Object objRef) {
		this.objRef = objRef;
	}
}
