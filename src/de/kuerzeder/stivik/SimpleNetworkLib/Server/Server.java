package de.kuerzeder.stivik.SimpleNetworkLib.Server;

import de.kuerzeder.stivik.SimpleNetworkLib.Util.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.AlreadyConnectedException;
import java.util.*;

/**
 * SimpleNetworkLib: A simple Server class for Network Communication
 * @author Stefan Kürzeder
 * created on 09.05.2016 in BY, Germany
 */
public abstract class Server {

    private ServerSocket serverSocket;
    private int port;
    private boolean debugMode = false;
    private HashMap<Callback, Executable> callbacks;
    private Thread listeningThread = null;
    private List<ServerListener> listeners = new ArrayList<>();

    public Server(int port, boolean debug) {
        this.debugMode = debug;

        this.port = port;
        this.callbacks = new HashMap<>();
    }

    // Abstract methods
    /**
     * Gets executed before the server realy gets started,
     * before the ServerSocket gets openend and the Server starts listening
     */
    //public abstract void preStart();

    /**
     * Gets executed when the server has been started
     * and the server listening started
     */
    //public abstract void postStart();


    /**
     * Gets executed when receiving a new NetworkPacket from a client
     * @param clientSocket the socket of client (to send back a message)
     * @param networkPacket which got received from the client
     */
    public abstract void receiveNetworkPackage(Socket clientSocket, NetworkPacket networkPacket);

    /**
     * Override this method to check if the client is loggedin or not
     * @param clientSocket the socket of the client which should get checked
     * @return default value if method gets not overriden
     */
    public boolean isSocketValid(Socket clientSocket) { return true; }

    /**
     * Ovveride this method to handle "login"-Packages from a client
     * @param clientSocket the socket of the client which should get loggedin
     * @param networkPacket the NetworkPacket which the client send
     */
    public void socketLogin(Socket clientSocket, NetworkPacket networkPacket) { }

    /**
     * Ovveride this method to handle "logout"-Packages from a client
     * @param clientSocket the socket of the client which should get loggedin
     * @param networkPacket the NetworkPacket which the client send
     */
    public void socketLogout(Socket clientSocket, NetworkPacket networkPacket) { }
    //

    // Class methods
    /**
     * Handles the starting of the Server (init Server-Socket, start listening, ...)
     */
    public void startServer(){
        dispatchEvent(EventType.ON_PRE_START, null);
        runCallback(Callback.ON_SYSTEM_MESSAGE, "[Info] Starting the Server!");

        initServer();
        listen();

        dispatchEvent(EventType.ON_POST_START, null);
    }

    /**
     * Opens the ServerSocket and try's to resolve the remote-Adress
     */
    private void initServer(){
        if(serverSocket == null) {
            try {
                serverSocket = new ServerSocket(this.port);
                runCallback(Callback.ON_SYSTEM_MESSAGE, "[Info] Trying to resolve Address");
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(new URL("http://bot.whatismyipaddress.com/").openStream()));
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

    /**
     * Starts the listeningThread of the Server which accepts
     * new Client-Sockets and handle their Streams and processes
     * the received NetworkPackages
     */
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

                                if (input instanceof NetworkPacket) {
                                    NetworkPacket networkPacket = (NetworkPacket) input;
                                    if(networkPacket.getId().equalsIgnoreCase("client:login")) {
                                        socketLogin(clientSocket, networkPacket);
                                    } else if(networkPacket.getId().equalsIgnoreCase("client:logout")) {
                                        socketLogout(clientSocket, networkPacket);
                                        break; // We can break here, no new messages from the client
                                    } else if(isSocketValid(clientSocket)) {
                                        receiveNetworkPackage(clientSocket, networkPacket);
                                        runCallback(Callback.ON_NEW_NETPACKAGE, networkPacket);

                                        // Send back a Status
                                        write(clientSocket, new NetworkPacket("STATUS", true));
                                    } else {
                                        write(clientSocket, new NetworkPacket("STATUS", false));
                                    }
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

    /**
     * Sends the client a new NetworkPacket, on which the client can react on
     * @param clientSocket on which the NetworkPacket should get written
     * @param networkPackage which should get written on the Socket-Stream
     */
    private void write(Socket clientSocket, NetworkPacket networkPackage){
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
    //

    // Event Methods
    public void addListener(ServerListener listener){
        listeners.add(listener);
    }

    public void dispatchEvent(EventType type, Object arg){
        for (ServerListener listener : listeners) {
            switch (type) {
                case ON_PRE_START:
                    listener.preStart();
                    break;
                case ON_POST_START:
                    listener.postStart();
                    break;
                case ON_MESSAGE:
                    listener.onMessage(arg);
                    break;
                case ON_ERROR:
                    listener.onError(arg);
                    break;
                default:
                    listener.onError("[Error] Invalid Event '" + type.name() + "', cannot execute!");
                    break;
            }
        }
    }
    //
}
