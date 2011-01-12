package se.ritzau.gimli;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.TreeSet;

import se.ritzau.gimli.command.Command;

public class DenonCore implements MessageHandler {
    private AVRUI ui;
    private Connection connection;
    private PrintWriter out;
    private Hashtable<String, String> map = new Hashtable<String, String>();
    private int tick = 0;

    private TreeSet<Command<?>> commands = new TreeSet<Command<?>>(new LengthComparator());
    
    private static class LengthComparator implements Comparator<Command<?>> {
	@Override
	public int compare(Command<?> o1, Command<?> o2) {
	    int l1 = o1.getPrefix().length();
	    int l2 = o2.getPrefix().length();

	    return l1 != l2? l2 - l1 : o1.getPrefix().compareTo(o2.getPrefix());
	}
    }
    
    public DenonCore(AVRUI ui, Connection connection) {
	this.ui = ui;
	this.connection = connection;
	this.out = connection.getOutput();
    }

    public boolean isConnected() {
	return connection.isConnected();
    }
    
    public void registerCommand(Command<?> command) {
	commands.add(command);
    }

    // XXX rewrite to improve efficiency
    public synchronized void handleMessage(String line) {
        tick++;
        Log.d(">>> " + line);

        for (Command<?> c : commands) {
            String param = c.getParameter(line);
            if (param != null) {
        	String prefix = c.getPrefix();
        	
        	String old = map.get(prefix);
        	if (old == null || !old.equals(param)) {
        	    map.put(prefix, param);
        	    c.updateUI(ui, param);
        	}
        	
        	break;
            }
        }
        
        notifyAll();
    }
    
    public void send(String msg) {
        out.print(msg + '\r');
        out.flush();
        Log.d("<<< " + msg);
    }
    
    private String waitFor(String key, String query) throws InterruptedException {
        synchronized (map) {
            int pre = tick;
            
            String v;
            while ((v = map.get(key)) == null) {
                //System.err.println("Waiting for: " + key);
                map.wait(200);
                if (tick != pre) {
                    pre = tick;
                }
                else {
                    send(query);
                }
            }
            
            return v;
        }
    }
    
    // TODO Fix timeout!
    public String get(String key, String query, long timeout) throws InterruptedException {
	String res = map.get(key);
	if (res == null) {
	    // XXX keep queue of outstanding requests
	    send(query);
	    res = waitFor(key, query);
	}

	return res;
    }
    
    public String getString(Command<?> command) throws InterruptedException {
	return get(command.getPrefix(), command.getQuery(), 0);
    }
    
    public <T> T get(Command<T> cmd) throws InterruptedException {
	return cmd.parseParameter(getString(cmd));
    }
}
