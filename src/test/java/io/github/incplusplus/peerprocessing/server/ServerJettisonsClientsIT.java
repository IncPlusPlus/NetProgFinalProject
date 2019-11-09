package io.github.incplusplus.peerprocessing.server;

import io.github.incplusplus.peerprocessing.client.Client;
import io.github.incplusplus.peerprocessing.common.ProperClient;
import io.github.incplusplus.peerprocessing.slave.Slave;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static io.github.incplusplus.peerprocessing.SingleSlaveIT.VERBOSE_TEST_OUTPUT;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerJettisonsClientsIT {
	private int serverPort;
	
	@BeforeEach
	void setUp() throws IOException {
		serverPort = Server.start(0, VERBOSE_TEST_OUTPUT);
		while (!Server.started()) {}
	}
	
	@AfterEach
	void tearDown() throws IOException {
		Server.stop();
	}
	
	@Test
	void oneClientOneSlave() throws IOException {
		performActionsAndAssertions(
				new Client("localhost", serverPort),
				new Slave("localhost", serverPort));
	}
	
	@Test
	void twoClientsOneSlave() throws IOException {
		performActionsAndAssertions(
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Slave("localhost", serverPort));
	}
	
	@Test
	void oneClientTwoSlaves() throws IOException {
		performActionsAndAssertions(
				new Client("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort));
	}
	
	@Test
	void manyClientsOneSlave() throws IOException {
		performActionsAndAssertions(
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Slave("localhost", serverPort));
	}
	
	@Test
	void oneClientManySlaves() throws IOException {
		performActionsAndAssertions(
				new Client("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort));
	}
	
	@Test
	void manyClientsManySlaves() throws IOException {
		performActionsAndAssertions(
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort));
	}
	
	void performActionsAndAssertions(ProperClient... properClients) throws IOException {
		List<ProperClient> properClientList = asList(properClients);
		properClientList.forEach(properClient -> {
			properClient.setVerbose(VERBOSE_TEST_OUTPUT);
			assertTrue(properClient.init());
			properClient.begin();
		});
		//Wait for all clients to have introduced themselves
		while (properClientList.stream().map(ProperClient::isPolite).anyMatch(isPolite -> !isPolite)) {}
//		Server.stop();
		properClientList.forEach(properClient -> {
			try {
				properClient.close();
			}
			catch (IOException e) {
				e.printStackTrace();
				assert false;
			}
			assertFalse(Server.isConnected(properClient.getConnectionId()));
		});
	}
}
