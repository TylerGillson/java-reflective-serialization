package serialization;

import java.util.IdentityHashMap;

public class Serializer {
	private static final IdentityHashMap<Object, Integer> idHashMap = new IdentityHashMap<Object, Integer>();
	private static int id = 0;
	
	public org.jdom2.Document serialize(Object obj) {
		if (idHashMap.containsKey(obj))
			return null;
		else {
			idHashMap.put(obj, id++);
		}
		return null;
	}
	

}
