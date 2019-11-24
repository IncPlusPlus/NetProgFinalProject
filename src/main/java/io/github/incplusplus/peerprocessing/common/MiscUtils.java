package io.github.incplusplus.peerprocessing.common;

import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.util.Random;
import java.util.Scanner;
import java.util.SplittableRandom;

import static io.github.incplusplus.peerprocessing.common.Constants.HEADER_SEPARATOR;
import static org.javatuples.Pair.with;

public class MiscUtils {
	public static Pair<String, Integer> promptForHostPortTuple() {
		Scanner in = new Scanner(System.in);
		String host;
		int port;
		System.out.print("Host: ");
		host = in.nextLine();
		System.out.print("Port: ");
		port = in.nextInt();
		//gotta love the Scanner bug
		in.nextLine();
		return with(host, port);
	}
	
	public static Socket promptForSocket() throws IOException {
		Pair<String, Integer> hostAndPortPair = promptForHostPortTuple();
		return new Socket(hostAndPortPair.getValue0(), hostAndPortPair.getValue1());
	}
	
	public static Triplet<String, Integer, Socket> promptForHostPortSocket() throws IOException {
		Pair<String, Integer> hostAndPortPair = promptForHostPortTuple();
		String host = hostAndPortPair.getValue0();
		int port = hostAndPortPair.getValue1();
		return Triplet.with(host, port, new Socket(host, port));
	}
	
	/**
	 * Prefixes the provided string such that it begins with
	 * the some provided header value
	 * and is also separated from the header by {@link Constants#HEADER_SEPARATOR}
	 *
	 * @param intendedMessage the message to be prefixed
	 * @param header          the particular header to prefix the string with
	 * @return a prefixed copy of the provided string
	 */
	public static String msg(String intendedMessage, Header header) {
		return String.valueOf(header) +
				HEADER_SEPARATOR +
				intendedMessage;
	}
	
	/**
	 * Decodes a message. This strips the header from the message
	 * and returns only the payload.
	 *
	 * @param receivedMessage the message to decode
	 * @return the decoded message if it exists; else null
	 */
	public static String decode(String receivedMessage) {
		String[] split = receivedMessage.split(Character.toString(HEADER_SEPARATOR));
		if (split.length > 1)
			return split[1];
		return null;
	}
	
	/**
	 * Gets the header of a message which contains a header and a payload.
	 *
	 * @param fullPayload the message to get the header from
	 * @return the header of the supplied message
	 */
	public static Header getHeader(String fullPayload) {
		return Header.valueOf(fullPayload.split(Character.toString(HEADER_SEPARATOR))[0]);
	}
	
	public static int randInt(int lowerBoundInclusive, int upperBoundExclusive) {
		return new SplittableRandom().nextInt(lowerBoundInclusive, upperBoundExclusive );
	}
	
	/**
	 * Credit to https://stackoverflow.com/a/38342964 for this elegant solution
	 * @return the most likely local IP address of this machine
	 * @throws SocketException if something were to go wrong
	 * @throws UnknownHostException if something were to go horribly wrong
	 */
	public static String getIp() throws SocketException, UnknownHostException {
		try (final DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			return socket.getLocalAddress().getHostAddress();
		}
	}
	
	/**
	 * Generates an integer uniformly at random between 0 (inclusive) and
	 * the specified ceiling (exclusive) and creates a BigDecimal
	 * with that value divided by 100.
	 * <br>
	 * Credit to <a href="https://stackoverflow.com/a/21863676/1687436">this SO answer</a>.
	 * @param ceiling the ceiling of the initial value
	 * @return PLEASE SEE METHOD DESCRIPTION
	 */
	public static BigDecimal randBigDec(int ceiling) {
		return new BigDecimal(BigInteger.valueOf(new Random().nextInt(ceiling)), 2);
	}
}
