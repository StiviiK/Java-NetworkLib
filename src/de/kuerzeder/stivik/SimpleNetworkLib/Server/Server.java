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
    private boolean debugMode;
    private Thread listeningThread;
    private List<ServerListener> listeners;

    public Server(int port, boolean debug) {
        this.debugMode = debug;
        this.listeners = new ArrayList<>();
        this.port = port;
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
    public abstract void receiveNetworkPacket(Socket clientSocket, NetworkPacket networkPacket);

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
        dispatchEvent(EventType.ON_PRE_START);
        dispatchEvent(EventType.ON_MESSAGE, "[Info] Starting the Server!");

        initServer();
        listen();

        dispatchEvent(EventType.ON_POST_START);
    }

    /**
     * Opens the ServerSocket and try's to resolve the remote-Adress
     */
    private void initServer(){
        if(serverSocket == null) {
            try {
                serverSocket = new ServerSocket(this.port);
                dispatchEvent(EventType.ON_MESSAGE, "[Info] Trying to resolve Address");
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(new URL("http://bot.whatismyipaddress.com/").openStream()));
                    dispatchEvent(EventType.ON_MESSAGE, "[Info] Bound to Address " + in.readLine() + ":" + serverSocket.getLocalPort());
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
                                    if(networkPacket.getId().equals(NetworkPacketId.CLIENT_LOGIN.getId())) {
                                        socketLogin(clientSocket, networkPacket);
                                    } else if(networkPacket.getId().equals(NetworkPacketId.CLIENT_LOGOUT.getId())) {
                                        socketLogout(clientSocket, networkPacket);
                                        break; // We can break here, no new messages from the client
                                    } else if(isSocketValid(clientSocket)) {
                                        receiveNetworkPacket(clientSocket, networkPacket);

                                        // Send back a Status
                                        write(clientSocket, new NetworkPacket(NetworkPacketId.REPLY_STATUS.getId(), true));
                                    } else {
                                        write(clientSocket, new NetworkPacket(NetworkPacketId.REPLY_STATUS.getId(), false));
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
    //

    // Event Methods
    protected void addListener(ServerListener listener){
        listeners.add(listener);
    }

    private void dispatchEvent(EventType type, Object arg){
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

    private void dispatchEvent(EventType type){
        dispatchEvent(type, null);
    }
    //
}
