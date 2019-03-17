package serialization;

import java.util.IdentityHashMap;

public class Serializer {
	private static IdentityHashMap<Object, Integer> idHashMap;
	private static int id;
	
	public Serializer() {
		idHashMap = new IdentityHashMap<Object, Integer>();
		id = 0;
	}
	
	public org.jdom2.Document serialize(Object obj) {
		if (idHashMap.containsKey(obj))
			return null;
		else {
			idHashMap.put(obj, id++);
		}
		return null;
	}
	

}
