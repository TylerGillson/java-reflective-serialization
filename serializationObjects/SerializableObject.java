package serializationObjects;

public class SerializableObject {

	private final int idNum;
	
	public SerializableObject(int id) {
		idNum = id;
	}
	
	public int getId() {
		return idNum;
	}

}