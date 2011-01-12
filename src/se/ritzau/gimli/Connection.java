package se.ritzau.gimli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public interface Connection {
    void setMessageHandler(MessageHandler handler);
    BufferedReader getInput();
    PrintWriter getOutput();
    void close() throws IOException;
    Thread startThread();
    boolean isConnected();
}
