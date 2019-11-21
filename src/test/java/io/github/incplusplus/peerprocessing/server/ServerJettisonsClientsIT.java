package io.github.incplusplus.peerprocessing.server;

import io.github.incplusplus.peerprocessing.client.Client;
import io.github.incplusplus.peerprocessing.common.ProperClient;
import io.github.incplusplus.peerprocessing.slave.Slave;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static io.github.incplusplus.peerprocessing.SingleSlaveIT.INITIAL_SERVER_PORT;
import static io.github.incplusplus.peerprocessing.SingleSlaveIT.VERBOSE_TEST_OUTPUT;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerJettisonsClientsIT {
	private int serverPort;
	private final Server server = new Server();
	
	@BeforeEach
	void setUp() throws IOException {
		serverPort = server.start(INITIAL_SERVER_PORT, VERBOSE_TEST_OUTPUT);
		//noinspection StatementWithEmptyBody
		while (!server.started()) {}
	}
	
	@AfterEach
	void tearDown() throws IOException {
		server.stop();
	}
	
	@Test
	void oneClientOneSlave() {
		performActionsAndAssertions(
				new Client("localhost", serverPort),
				new Slave("localhost", serverPort));
	}
	
	@Test
	void twoClientsOneSlave() {
		performActionsAndAssertions(
				new Client("localhost", serverPort),
				new Client("localhost", serverPort),
				new Slave("localhost", serverPort));
	}
	
	@Test
	void oneClientTwoSlaves() {
		performActionsAndAssertions(
				new Client("localhost", serverPort),
				new Slave("localhost", serverPort),
				new Slave("localhost", serverPort));
	}
	
	@Test
	void manyClientsOneSlave() {
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
	void oneClientManySlaves() {
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
	void manyClientsManySlaves() {
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
	
	void performActionsAndAssertions(ProperClient... properClients) {
		List<ProperClient> properClientList = asList(properClients);
		properClientList.forEach(properClient -> {
			properClient.setVerbose(VERBOSE_TEST_OUTPUT);
			assertTrue(properClient.init());
			properClient.begin();
		});
		//Wait for all clients to have introduced themselves
		//noinspection StatementWithEmptyBody
		while (properClientList.stream().map(ProperClient::isPolite).anyMatch(isPolite -> !isPolite)) {}
		properClientList.forEach(properClient -> {
			try {
				properClient.close();
			}
			catch (IOException e) {
				e.printStackTrace();
				assert false;
			}
		});
		properClientList.forEach(properClient -> {
			//there was previously a much more elegant way but some
			//clients were still reading the disconnect line from the server
			//and caused this integration test to fail
			if(server.isConnected(properClient.getConnectionId())) {
				try {
					System.out.println("Waiting 50ms for server to drop " + properClient + ".");
					Thread.sleep(50);
					if (!server.isConnected(properClient.getConnectionId()))
						System.out.println("Success!!");
				}
				catch (InterruptedException e) {
					throw new IllegalStateException("Got interrupted while generously sleeping on the connected entities map.", e);
				}
			}
		});
	}
}
