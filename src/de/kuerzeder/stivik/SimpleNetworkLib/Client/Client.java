package de.kuerzeder.stivik.SimpleNetworkLib.Client;

import de.kuerzeder.stivik.SimpleNetworkLib.Util.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.AlreadyConnectedException;
import java.util.HashMap;

/**
 * SimpleNetworkLib: A simple Client class for Network Communication
 * @author Stefan Kürzeder
 * created on 09.05.2016 in BY, Germany
 */
public class Client {

    private boolean debugMode = false;
    private InetSocketAddress remoteHost;
    private int timeout;
    private Socket networkSocket;
    private HashMap<Callback, Executable> callbacks;
    private boolean isLoggedin = false;
    private Thread listeningThread = null;

    public Client(String host, int port, int timeout, boolean debug) {
        this.debugMode  = debug;

        this.remoteHost = new InetSocketAddress(host, port);
        this.timeout    = timeout;
        this.callbacks  = new HashMap<>();
    }

    /**
     * Connects to the server and opens an new networkSocket
     */
    public void connect(){
        try {
            if(networkSocket == null || !networkSocket.isConnected()) {
                networkSocket = new Socket();
                networkSocket.connect(this.remoteHost, this.timeout);
            } else {
                throw new AlreadyConnectedException();
            }

            runCallback(Callback.ON_SYSTEM_MESSAGE, "[Info] Connected successfull to the Server(-Socket) on " + this.remoteHost.toString());
            runCallback(Callback.CLIENT_ON_CONNECTED);

            // Start listening
            listen();
        } catch (IOException | AlreadyConnectedException e) {
            if(this.debugMode) {
                e.printStackTrace();
                System.err.println("[Error] Connection to the Server(-Socket) failed. See StackTrace.");
            } else {
                System.err.println("[Error] Connection to the Server(-Socket) failed. (enable debug-mode, for stacktrace)");
            }
            runCallback(Callback.ON_ERROR, e.getLocalizedMessage());
        }
    }

    /**
     * Closes the connection to the server and closes the networkSocket
     */
    public void disconnect(){
        if(listeningThread != null) {
            listeningThread.interrupt();
        }
        if(networkSocket != null && networkSocket.isConnected()) {
            if(isLoggedin) { // If we're loggedin -> logout before disconnect
                logout();
            }
            try {
                networkSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends the login "command" to the server, to receive messages
     */
    public void login(){
        if(!isLoggedin) {

        }
    }

    /**
     * Logs out from the server, so we no longer receive message
     */
    public void logout(){
        if(isLoggedin){

        }
    }

    private void listen(){
        if(listeningThread != null && listeningThread.isAlive())
            return;

        listeningThread = new Thread(() -> {
           while(true){
               try {
                   ObjectInputStream inputStream = new ObjectInputStream(networkSocket.getInputStream());
                   Object input = inputStream.readObject();

                   if(input instanceof NetworkPackage){
                       runCallback(Callback.ON_NEW_NETPACKAGE, (NetworkPackage) input);
                   }
               } catch (IOException  e) { // Socket is closed
                   Thread.currentThread().interrupt();
                   break;
               } catch (ClassNotFoundException e) {
                   e.printStackTrace();
               }
           }
        });

        listeningThread.start();
    }

    public void write(NetworkPackage networkPackage){
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(networkSocket.getOutputStream());
            outputStream.writeObject(networkPackage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Registers a new callback handler, to handle some "Events"
     * e.g. onConnected, onError, onMessage, ...
     * @param callbackId (Callback Enum) on which Event this callback gets registered
     * @param callback an executable which gets executed on a certain event
     */
    public void registerCallback(Callback callbackId, Executable callback) {
        callbacks.put(callbackId, callback);
    }

    /**
     * Executes the callback Handler for an "Event"
     * e.g. onConnected, onError, onMessage, ...
     * @param callbackId (Callback Enum) on which Event this callback gets registered
     * @param arg an Object (Argument) which can be provided to the executable
     * @TODO: 09.05.2016 make this function later private
     */
    public void runCallback(Callback callbackId, Object arg) {
        Executable callback = callbacks.get(callbackId);
        if(callback != null) {
            callback.run(arg);
        }
    }

    /**
     * Executes the callback Handler for an "Event"
     * e.g. onConnected, onError, onMessage, ...
     * @param callbackId (Callback Enum) on which Event this callback gets registered
     * @TODO: 09.05.2016 make this function later private
     */
    public void runCallback(Callback callbackId) {
        Executable callback = callbacks.get(callbackId);
        if(callback != null) {
            callback.run(null);
        }
    }

    public boolean isConnected(){
        return !networkSocket.isClosed();
    }
}
