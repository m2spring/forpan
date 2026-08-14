package org.springdot.forpan.gui;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Detects a second running instance of Forpan and, if found, tells it to bring itself
 * to the foreground. Detection and signalling both piggyback on binding a fixed loopback
 * port: whichever instance binds it first is "the" instance; a later instance that fails
 * to bind connects to that port to deliver a focus request, then has nothing further to do.
 */
class SingleInstanceGuard{
    private static final Logger LOG = Logger.getLogger(SingleInstanceGuard.class.getName());
    private static final int PORT = 47852;
    private static final String FOCUS_MSG = "FOCUS";

    private final ServerSocket serverSocket;

    private SingleInstanceGuard(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }

    /**
     * @return a guard for this (first) instance, or null if another instance is already
     * running, in which case it has already been notified to come to the foreground.
     */
    static SingleInstanceGuard acquire(){
        ServerSocket ss;
        try{
            ss = new ServerSocket(PORT,0,InetAddress.getLoopbackAddress());
        }catch (IOException e){
            LOG.info("another instance appears to be running, notifying it: "+e.getMessage());
            notifyRunningInstance();
            return null;
        }
        return new SingleInstanceGuard(ss);
    }

    private static void notifyRunningInstance(){
        try (Socket s = new Socket(InetAddress.getLoopbackAddress(),PORT)){
            s.getOutputStream().write(FOCUS_MSG.getBytes(StandardCharsets.UTF_8));
        }catch (IOException e){
            LOG.warning("could not notify running instance: "+e.getMessage());
        }
    }

    /** Starts a daemon thread that invokes {@code onFocusRequested} whenever another launch attempt occurs. */
    void listenForFocusRequests(Runnable onFocusRequested){
        Thread t = new Thread(() -> {
            while (!serverSocket.isClosed()){
                try (Socket client = serverSocket.accept()){
                    client.getInputStream().readAllBytes();
                    onFocusRequested.run();
                }catch (IOException e){
                    if (!serverSocket.isClosed()) LOG.warning("error accepting focus request: "+e.getMessage());
                }
            }
        },"single-instance-listener");
        t.setDaemon(true);
        t.start();
    }

    void close(){
        try{
            serverSocket.close();
        }catch (IOException ignored){}
    }
}
