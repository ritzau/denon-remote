package se.ritzau.gimli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketConnection implements Connection, Runnable {
    private Socket s;
    private BufferedReader in;
    private PrintWriter out;
    private MessageHandler handler;
    private Thread thread;
    
    public SocketConnection(String adr) throws UnknownHostException, IOException {
        s = new Socket(adr, 23);
        out = new PrintWriter(s.getOutputStream());
        in = new BufferedReader(new InputStreamReader(s.getInputStream()));
    }

    @Override
    public void setMessageHandler(MessageHandler handler) {
	this.handler = handler;
    }
    
    @Override
    public BufferedReader getInput() {
        return in;
    }

    @Override
    public PrintWriter getOutput() {
        return out;
    }
    
    @Override
    public boolean isConnected() {
        return s.isConnected();
    }
    
    public Thread startThread() {
	if (thread != null) {
	    throw new AssertionError();
	}
	
	thread = new Thread(this);
	thread.start();
	
	return thread;
    }
    
    public void run() {
        try {
            while (true) {
        	String line = in.readLine();
        	if (line == null) break;
                handler.handleMessage(line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
	if (in != null) in.close();
	if (out != null) out.close();
	if (s != null) s.close();
    }
}
