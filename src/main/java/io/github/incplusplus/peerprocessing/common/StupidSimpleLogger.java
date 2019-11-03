package io.github.incplusplus.peerprocessing.common;

import com.diogonunes.jcdp.color.ColoredPrinter;
import com.diogonunes.jcdp.color.api.Ansi;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StupidSimpleLogger {
	private static boolean enabled;
	private static ColoredPrinter cp;
	
	public static void enable() {
		enabled = true;
		cp = new ColoredPrinter.Builder(1, false)
				.foreground(Ansi.FColor.WHITE).background(Ansi.BColor.BLUE)   //setting format
				.build();
	}
	
	public static void disable() {
		enabled = false;
	}
	
	public static void debug(String message) {
		if (enabled) {
			cp.clear();
			cp.print(cp.getDateFormatted(), Ansi.Attribute.NONE, Ansi.FColor.CYAN, Ansi.BColor.BLACK);
			cp.debugPrintln(" " + message);
			cp.clear();
			
		}
	}
	
	public static void info(String message) {
		if (enabled) {
			cp.clear();
			cp.println(message, Ansi.Attribute.NONE, Ansi.FColor.CYAN, Ansi.BColor.BLACK);
			cp.clear();
		}
	}
	
	public static void error(String message) {
		if (enabled) {
			cp.clear();
			cp.println(message, Ansi.Attribute.NONE, Ansi.FColor.CYAN, Ansi.BColor.BLACK);
			cp.clear();
		}
	}
	
	public static void printStackTrace(Exception e) {
		if (enabled) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			cp.clear();
			cp.println(errors.toString(), Ansi.Attribute.NONE, Ansi.FColor.CYAN, Ansi.BColor.BLACK);
			cp.clear();
		}
	}
}
