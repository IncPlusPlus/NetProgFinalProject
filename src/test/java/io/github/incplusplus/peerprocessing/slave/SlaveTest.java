package io.github.incplusplus.peerprocessing.slave;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SlaveTest {
	
	@Test
	void failIfNoServer() {
		Slave mySlave = new Slave("localhost", 1234);
		boolean initSuccess = mySlave.init();
		assert !initSuccess;
		IllegalStateException thrown =
				assertThrows(IllegalStateException.class,
						mySlave::begin);
		assertEquals(thrown.getMessage(), "Socket not initialized properly. " +
				"Did you remember to check the boolean value of Slave.begin()?!");
	}
}