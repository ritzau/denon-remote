package se.ritzau.gimli;

// XXX make it use a factory (or registry) to select implementation (e.g. android)
public class Log {
    private static final boolean DEBUG = false;
    
    public static void d(Object msg) {
	if (DEBUG) System.out.println(msg.toString());
    }
    
    public static void v(Object msg) {
	if (DEBUG) System.out.println(msg.toString());
    }
    
    public static void i(Object msg) {
	System.out.println(msg.toString());
    }
    
    public static void w(Object msg) {
	System.err.println(msg.toString());
    }
}
