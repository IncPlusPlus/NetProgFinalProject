package io.github.incplusplus.peerprocessing.common;

public class StupidSimpleLogger {
	private static boolean enabled;
	
	public static void enable() {
		enabled = true;
	}
	
	public static void disable() {
		enabled = false;
	}
	
	public static void log(String message) {
		if (enabled)
			System.out.println(message);
	}
}
