package io.github.incplusplus.peerprocessing.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClientTest {
	
	@Test()
	void failIfNoServer() {
		Client myClient = new Client("localhost", 9999);
		assertThrows(java.net.ConnectException.class,
				myClient::begin);
	}
}