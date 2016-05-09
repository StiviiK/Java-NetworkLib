package de.kuerzeder.stivik.SimpleNetworkLib.Server;

import de.kuerzeder.stivik.SimpleNetworkLib.Util.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.AlreadyConnectedException;
import java.util.HashMap;

/**
 * SimpleNetworkLib: A simple Server class for Network Communication
 * @author Stefan Kürzeder
 * created on 09.05.2016 in BY, Germany
 */
public class Server {

    private ServerSocket serverSocket;
    private int port;
    private boolean debugMode = false;
    private HashMap<CallbackIds, Executable> callbacks;

    public Server(int port, boolean debug) {
        this.debugMode = debug;

        this.port = port;
        this.callbacks = new HashMap<>();
    }

    public void startServer(){
        runCallback(CallbackIds.ON_SYSTEM_MESSAGE, "[Info] Starting the Server!");
        preStart();

        initServer();
    }

    private void preStart(){
        runCallback(CallbackIds.SERVER_PRE_START);
    }

    private void initServer(){
        if(serverSocket == null) {
            try {
                serverSocket = new ServerSocket(this.port);
                runCallback(CallbackIds.ON_SYSTEM_MESSAGE, "[Info] Trying to resolve Address");
                try {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(new URL("http://bot.whatismyipaddress.com/").openStream())
                    );
                    runCallback(CallbackIds.ON_SYSTEM_MESSAGE, "[Info] Bound to Adress " + in.readLine() + ":" + serverSocket.getLocalPort());
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new AlreadyConnectedException();
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
