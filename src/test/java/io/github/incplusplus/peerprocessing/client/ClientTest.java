package io.github.incplusplus.peerprocessing.client;

import io.github.incplusplus.peerprocessing.server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static io.github.incplusplus.peerprocessing.SingleSlaveIT.VERBOSE_TEST_OUTPUT;

class ClientTest {
	private static int serverPort;
	
	@BeforeAll
	static void setUp() throws IOException {
		serverPort = Server.start(0, VERBOSE_TEST_OUTPUT);
		while (!Server.started()) {}
	}
	
	@AfterAll
	static void tearDown() throws IOException {
		Server.stop();
	}
	
	@Test
	void main() {
	}
}