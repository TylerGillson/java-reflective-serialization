package serializationObjects;

import java.util.ArrayList;

public class CollectionObject extends SerializableObject {
	
	private ArrayList<Object> refArrayList;
	
	/**
	 * Collection Object Constructor
	 * @param refList
	 */
	public CollectionObject(int id, ArrayList<Object> refList) {
		super(id);
		refArrayList = refList;
	}
	
	public String toString() {
		String s = "";
		for (int i=0; i<refArrayList.size(); i++) {
			s += refArrayList.get(i).toString();
			if (i != refArrayList.size() - 1)
				s += ", ";
		}
		return "CollectionObject" + String.valueOf(super.getId()) + "[" + s + "]";
	}
	
	public ArrayList<Object> getRefArrayList() {
		return refArrayList;
	}

	public void setRefArrayList(ArrayList<Object> refArrayList) {
		this.refArrayList = refArrayList;
	}
}
