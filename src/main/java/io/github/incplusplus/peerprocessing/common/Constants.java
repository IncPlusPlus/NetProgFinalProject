package io.github.incplusplus.peerprocessing.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * A class of constants that the server and/or the client understand
 */
public class Constants {
	/**
	 * This is the character that separates the beginning header
	 * from the payload of the message. The header is what tells
	 * the server or client what the interaction is regarding.
	 */
	//  :) I love the bell character
	final static char HEADER_SEPARATOR = (char) 7;
	/**
	 * This is the string that will be typed into the client
	 * by a user to tell the client to disconnect itself
	 * from the server.
	 */
	public final static String QUIT_STRING = "/q";
	
	public final static ObjectMapper SHARED_MAPPER = new ObjectMapper()
//			.registerModule(new ParameterNamesModule())
//			.registerModule(new Jdk8Module())
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	
	/**
	 * This enum contains values that are processed by the server
	 * and/or client
	 */
	public enum ConstantEnum {
		/**
		 * Indicates to the server that the new connection it has
		 * just received is for a headless-style client where
		 * the client can send only in one console and passively receive
		 * in the other. This message would be sent by the writable console.
		 */
		CONNECT_HEADLESS_CLIENT_SENDER,
		/**
		 * Same as {@link #CONNECT_HEADLESS_CLIENT_SENDER} but
		 * this message would come from the reading-only console.
		 */
		CONNECT_HEADLESS_CLIENT_RECEIVER,
		/**
		 * Asks the server to register this new client.
		 * This command will only be accepted while this
		 * client has not yet registered itself.
		 */
		REGISTER_NEW_CLIENT,
		/**
		 * This is sent by the server to tell the client
		 * its name.
		 */
		SERVER_NAME,
		/**
		 * Sent from the server. Asks client for their name
		 */
		PROVIDE_CLIENT_NAME,
		/**
		 * Sent from the client. Specifies client name.
		 */
		CLIENT_NAME,
		/**
		 * Sent from the server indicating the
		 * client should identify itself as one
		 * of the known {@link ClientType}s
		 */
		IDENTIFY,
		/**
		 * Sent from the client as a response to
		 * the {@link #IDENTIFY} query.
		 */
		IDENTITY,
		/**
		 * Sent from the server indicating the client
		 * should provide a registration key.
		 */
		PROVIDE_REG_KEY,
		/**
		 * Sent from client to attach
		 * receiving console window.
		 */
		REG_KEY,
		/**
		 * This registration key does not
		 * exist on the server.
		 */
		REG_KEY_REJECTED,
		/**
		 * Whatever was just sent is fine.
		 * Continue with registration flow.
		 */
		CONTINUE,
		/**
		 * This tells the server to kill off the
		 * client connections.
		 */
		DISCONNECT,
		/**
		 * Indicates that this is to be treated as a
		 * regular chat message from a client.
		 */
		MESSAGE;
	}
}
