package io.github.incplusplus.peerprocessing.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClientTest {
	
	@Test()
	void failIfNoServer() {
		Client myClient = new Client("localhost", 1234);
		boolean initSuccess = myClient.init();
		assert !initSuccess;
		IllegalStateException thrown =
				assertThrows(IllegalStateException.class,
						myClient::begin);
		assertEquals(thrown.getMessage(), "Socket not initialized properly. " +
				"Did you remember to check the boolean value of Client.begin()?!");
	}
}