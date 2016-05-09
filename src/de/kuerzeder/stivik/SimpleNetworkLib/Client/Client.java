package de.kuerzeder.stivik.SimpleNetworkLib.Client;

import de.kuerzeder.stivik.SimpleNetworkLib.Util.*;
import java.io.IOException;
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
    private HashMap<CallbackIds, Executable> callbacks;
    private boolean isLoggedin = false;

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

            runCallback(CallbackIds.ON_SYSTEM_MESSAGE, "[Info] Connected successfull to the Server(-Socket) on " + this.remoteHost.toString());
            runCallback(CallbackIds.CLIENT_ON_CONNECTED);
        } catch (Exception e) {
            if(this.debugMode) {
                e.printStackTrace();
                System.err.println("[Error] Connection to the Server(-Socket) failed. See StackTrace.");
            } else {
                System.err.println("[Error] Connection to the Server(-Socket) failed. (enable debug-mode, for stacktrace)");
            }
            runCallback(CallbackIds.ON_ERROR, e.getLocalizedMessage());
        }
    }

    /**
     * Closes the connection to the server and closes the networkSocket
     */
    public void disconnect(){
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

    /**
     * Registers a new callback handler, to handle some "Events"
     * e.g. onConnected, onError, onMessage, ...
     * @param callbackId (CallbackIds Enum) on which Event this callback gets registered
     * @param callback an executable which gets executed on a certain event
     */
    public void registerCallback(CallbackIds callbackId, Executable callback) {
        callbacks.put(callbackId, callback);
    }

    /**
     * Executes the callback Handler for an "Event"
     * e.g. onConnected, onError, onMessage, ...
     * @param callbackId (CallbackIds Enum) on which Event this callback gets registered
     * @param arg an Object (Argument) which can be provided to the executable
     * @TODO: 09.05.2016 make this function later private
     */
    public void runCallback(CallbackIds callbackId, Object arg) {
        Executable callback = callbacks.get(callbackId);
        if(callback != null) {
            callback.run(arg);
        }
    }

    /**
     * Executes the callback Handler for an "Event"
     * e.g. onConnected, onError, onMessage, ...
     * @param callbackId (CallbackIds Enum) on which Event this callback gets registered
     * @TODO: 09.05.2016 make this function later private
     */
    public void runCallback(CallbackIds callbackId) {
        Executable callback = callbacks.get(callbackId);
        if(callback != null) {
            callback.run(null);
        }
    }
}
