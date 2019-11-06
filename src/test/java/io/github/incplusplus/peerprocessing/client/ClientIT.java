package io.github.incplusplus.peerprocessing.client;

import io.github.incplusplus.peerprocessing.server.Server;
import io.github.incplusplus.peerprocessing.slave.Slave;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import static java.math.BigDecimal.ONE;
import static org.junit.jupiter.api.Assertions.*;

class ClientIT {
	
	
	@BeforeEach
	void setUp() {
		Server.start(1234, true);
		while (!Server.started()) {}
		System.out.println("(MVN): Server started");
	}
	
	@AfterEach
	void tearDown() throws IOException {
		Server.stop();
	}
	
	@Test
	public void mainClientIntegrationTest() throws IOException, ExecutionException, InterruptedException {
		FutureTask<BigDecimal> task = null;
		try (Client myClient = new Client("localhost", 1234);
		     Slave mySlave = new Slave("localhost", 1234)) {
			myClient.setVerbose(true);
			myClient.init();
			myClient.begin();
			mySlave.setVerbose(true);
			mySlave.init();
			mySlave.begin();
			task = myClient.evaluateExpression("1+1");
			ExecutorService executor = Executors.newFixedThreadPool(2);
			executor.submit(task);
			assertEquals(task.get(), ONE.add(ONE));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}