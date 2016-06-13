package de.kuerzeder.stivik.SimpleNetworkLib.Client;

import de.kuerzeder.stivik.SimpleNetworkLib.Server.ServerListener;
import de.kuerzeder.stivik.SimpleNetworkLib.Util.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * SimpleNetworkLib: A simple Client class for Network Communication
 * @author Stefan Kürzeder
 * created on 09.05.2016 in BY, Germany
 */
public abstract class Client {

    private boolean debugMode = false;
    private InetSocketAddress remoteHost;
    private int timeout;
    private Socket networkSocket;
    private boolean isLoggedin = false;
    private Thread listeningThread = null;
    private List<ClientListener> listeners = new ArrayList<>();

    public Client(String host, int port, int timeout, boolean debug) {
        this.debugMode  = debug;

        this.remoteHost = new InetSocketAddress(host, port);
        this.timeout    = timeout;
    }

    // Abstract methods //
    /**
     * Sends the login "command" to the server, to receive messages
     */
    public abstract void login();

    /**
     * Logs out from the server, so we no longer receive message
     */
    public abstract void logout();

    /**
     * Gets executed when receiving a new NetworkPacket
     * @param networkPacket which got received from the Server
     */
    public abstract void receiveNetworkPacket(NetworkPacket networkPacket);
    // END //

    // Class methods //
    /**
     * Connects to the server and opens an new networkSocket
     */
    public void connect(){
        try {
            if(networkSocket == null || !networkSocket.isConnected() || networkSocket.isClosed()) {
                networkSocket = new Socket();
                networkSocket.connect(this.remoteHost, this.timeout);
            } else {
                throw new AlreadyConnectedException();
            }

            dispatchEvent(EventType.ON_MESSAGE, "[Info] Connected successfull to the Server(-Socket) on " + this.remoteHost.toString());
            dispatchEvent(EventType.ON_CONNECTED);

            // Start listening && Login to the Server
            listen();
            login();
        } catch (IOException | AlreadyConnectedException e) {
            if(this.debugMode) {
                e.printStackTrace();
                dispatchEvent(EventType.ON_ERROR, "[Error] Connection to the Server(-Socket) failed. See StackTrace.");
            } else {
                dispatchEvent(EventType.ON_ERROR, "[Error] Connection to the Server(-Socket) failed. (enable debug-mode, for stacktrace)");
            }
            dispatchEvent(EventType.ON_ERROR, e.getLocalizedMessage());
        }
    }

    /**
     * Closes the connection to the server and closes the networkSocket
     */
    public void disconnect(){
        logout();

        if(listeningThread != null) {
            listeningThread.interrupt();
        }
        if(networkSocket != null && networkSocket.isConnected()) {
            try {
                networkSocket.close();
                dispatchEvent(EventType.ON_DISCONNECTED);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Starts the listeningThread of the Client which waits
     * for new NetworkPackets from the Server and handles them
     */
    private void listen(){
        if(listeningThread != null && listeningThread.isAlive())
            return;

        listeningThread = new Thread(() -> {
           while(networkSocket != null && networkSocket.isConnected()){
               try {
                   ObjectInputStream inputStream = new ObjectInputStream(networkSocket.getInputStream());
                   Object input = inputStream.readObject();

                   if(input instanceof NetworkPacket){
                       receiveNetworkPacket((NetworkPacket) input);
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

    /**
     * Writes a new NetworkPacket on the (Client-)networkSocket,
     * so the Server can read them and handle it
     * @param networkPacket which should get written on the networkSocket-Stream
     */
    public void write(NetworkPacket networkPacket){
        try {
            if(networkSocket != null && networkSocket.isConnected() && !networkSocket.isClosed()) {
                ObjectOutputStream outputStream = new ObjectOutputStream(networkSocket.getOutputStream());
                outputStream.writeObject(networkPacket);
            } else {
                throw new NotYetConnectedException();
            }
        } catch (IOException | NotYetConnectedException e) {
            e.printStackTrace();
            dispatchEvent(EventType.ON_CONNECTION_LOST);
        }
    }
    //

    // Event Methods
    protected void addListener(ClientListener listener){
        listeners.add(listener);
    }

    private void dispatchEvent(EventType type, Object arg){
        for (ClientListener listener : listeners) {
            switch (type) {
                case ON_MESSAGE:
                    listener.onMessage(arg);
                    break;
                case ON_ERROR:
                    listener.onError(arg);
                    break;
                case ON_CONNECTED:
                    listener.onConnected();
                    break;
                case ON_DISCONNECTED:
                    listener.onDisconnect();
                    break;
                case ON_CONNECTION_LOST:
                    listener.onConnectionLost();
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
