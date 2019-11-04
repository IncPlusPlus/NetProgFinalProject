package io.github.incplusplus.peerprocessing.client;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Callable;

import static io.github.incplusplus.peerprocessing.common.StupidSimpleLogger.debug;

/**
 * Modified version of solution at <a href="https://stackoverflow.com/a/4983156/1687436">Stack Overflow</a>
 */
public class ConsoleInputReadTask implements Callable<String> {
	private final Socket dependentSocket;
	
	public ConsoleInputReadTask(Socket dependency) {
		this.dependentSocket = dependency;
	}
	public String call() throws IOException {
		BufferedReader br = new BufferedReader(
				new InputStreamReader(System.in));
		System.out.println("ConsoleInputReadTask run() called.");
		String input;
		do {
			System.out.println("Please type something: ");
			try {
				// wait until we have data to complete a readLine()
 				while (!br.ready()) {
					Thread.sleep(200);
					if(dependentSocket.isClosed()) {
						throw new InterruptedException("The socket this task depends on was closed");
					}
				}
				input = br.readLine();
			} catch (InterruptedException e) {
				debug("Ending input reader task");
				return null;
			}
		} while ("".equals(input));
		System.out.println("Thank You for providing input!");
		return input;
	}
}