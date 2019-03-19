package serializationObjects;

import java.util.ArrayList;

public class CollectionObject {
	
	private ArrayList<Object> refArrayList;
	
	/**
	 * Collection Object Constructor
	 * @param refList
	 */
	public CollectionObject(ArrayList<Object> refList) {
		refArrayList = refList;
	}
	
	public String toString() {
		String s = "";
		for (int i=0; i<refArrayList.size(); i++) {
			s += refArrayList.get(i).toString();
			if (i != refArrayList.size() - 1)
				s += ", ";
		}
		return "CollectionObject[" + s + "]";
	}
	
	public ArrayList<Object> getRefArrayList() {
		return refArrayList;
	}

	public void setRefArrayList(ArrayList<Object> refArrayList) {
		this.refArrayList = refArrayList;
	}
}
