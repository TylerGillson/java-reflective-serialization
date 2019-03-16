package serialization;

import java.net.Socket;

public class Transmitter {
	Socket s;
	
	public Transmitter() {
		s = new Socket();
	}
	
	public void transmit(org.jdom2.Document document) {
		
	}
}
