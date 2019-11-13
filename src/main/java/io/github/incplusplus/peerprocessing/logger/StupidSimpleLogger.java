package io.github.incplusplus.peerprocessing.logger;

import com.diogonunes.jcdp.color.ColoredPrinter;
import com.diogonunes.jcdp.color.api.Ansi;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StupidSimpleLogger {
	private static boolean enabled;
	private final static ColoredPrinter cp = new ColoredPrinter.Builder(1, false)
			.foreground(Ansi.FColor.WHITE).background(Ansi.BColor.BLUE)   //setting format
			.build();
	
	public static void enable() {
		enabled = true;
	}
	
	@SuppressWarnings("unused")
	public static boolean isEnabled() {
		return enabled;
	}
	
	@SuppressWarnings("unused")
	public static void disable() {
		enabled = false;
	}
	
	public synchronized static void debug(String message) {
		if (enabled) {
			synchronized (cp){
				cp.clear();
				cp.print("[" + cp.getDateFormatted() + "]", Ansi.Attribute.NONE, Ansi.FColor.CYAN, Ansi.BColor.BLACK);
				cp.debugPrintln(" " + message);
				cp.clear();
			}
		}
	}
	
	public static void info(String message) {
		if (enabled) {
			synchronized (cp) {
				cp.clear();
				cp.println(message, Ansi.Attribute.NONE, Ansi.FColor.GREEN, Ansi.BColor.BLACK);
				cp.clear();
			}
		}
	}
	
	/**
	 * Same as {@link #info(String)} but without printing linefeed afterwards.
	 * @param message the message to print.
	 */
	public static void infoNoLine(String message) {
		if (enabled) {
			synchronized (cp) {
				cp.clear();
				//See https://github.com/dialex/JCDP/issues/21
				cp.print("", Ansi.Attribute.NONE, Ansi.FColor.GREEN, Ansi.BColor.BLACK);
				System.out.print(message);
				cp.clear();
			}
		}
	}
	
	public static void error(String message) {
		if (enabled) {
			synchronized (cp) {
				cp.clear();
				cp.print("[" + cp.getDateFormatted() + "]", Ansi.Attribute.NONE, Ansi.FColor.CYAN, Ansi.BColor.BLACK);
				cp.errorPrintln(" " + message, Ansi.Attribute.NONE, Ansi.FColor.CYAN, Ansi.BColor.BLACK);
				cp.clear();
			}
		}
	}
	
	public static void printStackTrace(Exception e) {
		if (enabled) {
			synchronized (cp) {
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				cp.clear();
				cp.errorPrint(errors.toString(), Ansi.Attribute.NONE, Ansi.FColor.CYAN, Ansi.BColor.BLACK);
				cp.clear();
			}
		}
	}
}
