package de.kuerzeder.stivik.SimpleNetworkLib.Server;

import de.kuerzeder.stivik.SimpleNetworkLib.Util.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
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
    private HashMap<Callback, Executable> callbacks;
    private Thread listeningThread = null;

    public Server(int port, boolean debug) {
        this.debugMode = debug;

        this.port = port;
        this.callbacks = new HashMap<>();
    }

    public void startServer(){
        runCallback(Callback.ON_SYSTEM_MESSAGE, "[Info] Starting the Server!");
        preStart();

        initServer();
        listen();
    }

    private void preStart(){
        runCallback(Callback.SERVER_PRE_START);
    }

    private void initServer(){
        if(serverSocket == null) {
            try {
                serverSocket = new ServerSocket(this.port);
                runCallback(Callback.ON_SYSTEM_MESSAGE, "[Info] Trying to resolve Address");
                try {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(new URL("http://bot.whatismyipaddress.com/").openStream())
                    );
                    runCallback(Callback.ON_SYSTEM_MESSAGE, "[Info] Bound to Address " + in.readLine() + ":" + serverSocket.getLocalPort());
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

    private void listen(){
        listeningThread = new Thread(() -> {
            while(true){
                try {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(() -> { // Make a new Thread, so we can listen always on this socket (no new socket for a new message)
                        while(clientSocket.isConnected()) {
                            try {
                                ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
                                Object input = inputStream.readObject();

                                if (input instanceof NetworkPackage) {
                                    runCallback(Callback.ON_NEW_NETPACKAGE, (NetworkPackage) input);
                                    write(clientSocket, new NetworkPackage("STATUS", true)); // @TODO: 10.05.2016 Improve this -> so the client can read this within the write method
                                }
                            } catch (IOException e) { // Client has disconnected
                                break;
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                                break;
                            }
                        }

                        // Disable the Thread
                        Thread.currentThread().interrupt();
                    }).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        listeningThread.start();
    }

    private void write(Socket clientSocket, NetworkPackage networkPackage){
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
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
}
