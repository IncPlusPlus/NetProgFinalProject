package io.github.incplusplus.peerprocessing.client;

import java.io.*;
import java.util.concurrent.Callable;

import static io.github.incplusplus.peerprocessing.logger.StupidSimpleLogger.debug;

/**
 * Modified version of solution at <a href="https://stackoverflow.com/a/4983156/1687436">Stack Overflow</a>
 */
public class ConsoleInputReadTask implements Callable<String> {
	private volatile Client dependentClient;
	
	public ConsoleInputReadTask(Client dependency) {
		this.dependentClient = dependency;
	}
	public String call() throws IOException {
		BufferedReader br = new BufferedReader(
				new InputStreamReader(System.in));
		String input;
		do {
			try {
				// wait until we have data to complete a readLine()
 				while (!br.ready()) {
					Thread.sleep(200);
					if(dependentClient.isClosed()) {
						throw new InterruptedException("The socket this task depends on was closed");
					}
				}
				input = br.readLine();
 				if(input.equals("/q")) {
				    throw new InterruptedException("The user chose to quit");
			    }
			} catch (InterruptedException e) {
				dependentClient.close();
				debug("Ending input reader task");
				return null;
			}
		} while ("".equals(input));
		return input;
	}
}