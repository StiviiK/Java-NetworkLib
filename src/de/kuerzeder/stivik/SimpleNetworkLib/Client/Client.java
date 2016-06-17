package de.kuerzeder.stivik.SimpleNetworkLib.Client;

import de.kuerzeder.stivik.SimpleNetworkLib.Util.*;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.NotYetConnectedException;
import java.util.*;

/**
 * SimpleNetworkLib: A simple Client class for Network Communication
 * @author Stefan Kürzeder
 * created on 09.05.2016 in BY, Germany
 */
public abstract class Client {

    private boolean debugMode;
    private InetSocketAddress remoteHost;
    private int timeout;
    private Socket networkSocket;
    private boolean isLoggedin;
    private Thread listeningThread;
    private List<ClientListener> listeners;
    private Thread pingThread;

    // Ping
    private Long lastPing;
    private Date lastPingTest;

    public Client(String host, int port, int timeout, boolean debug) {
        this.debugMode  = debug;
        this.listeners  = new ArrayList<>();
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
            if(debugMode) {
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

        if(pingThread != null) {
            pingThread.interrupt();
        }
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

                   if (input instanceof NetworkPacket) {
                       NetworkPacket networkPacket = (NetworkPacket) input;
                       if(networkPacket.getId().equals(NetworkPacketId.PING_REPLY.getId())) {
                           onPingReply();
                       } else {
                           receiveNetworkPacket(networkPacket);
                       }
                   } else {
                       throw new InvalidClassException("Input is not an instanceof NetworkPacket");
                   }
               } catch (InvalidClassException | ClassNotFoundException e) {
                   e.printStackTrace();
               } catch (IOException  e) { // Socket is closed
                   Thread.currentThread().interrupt();
                   break;
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

    /**
     * Starts a new Ping-Test, to capture the latency between
     * Client and Server (Client -> Server -> Client), also to
     * check if the connection is still alive.
     */
    public void startPingTest(){
        if(pingThread == null) {
            pingThread = new Thread(() -> {
                while (true) {
                    try {Thread.sleep(3 * 1000);} catch (InterruptedException ignored) {}

                    if(networkSocket != null && networkSocket.isConnected() && !networkSocket.isClosed()) {
                        lastPingTest = Calendar.getInstance().getTime();
                        write(new NetworkPacket(NetworkPacketId.PING_STATUS.getId(), true));
                    }
                }
            });
            pingThread.start();
        }
    }

    /**
     * Is the "hidden" Callback handler for an Ping-Reply Package
     * from the Server. It sets the lastPing value.
     */
    private void onPingReply(){
        lastPing = (Calendar.getInstance().getTime().getTime() - lastPingTest.getTime());
    }

    /**
     * Returns the lastPing latency between the Client and the Server.
     * Gets update all ~3000ms (+ Latency).
     * @return the last captured ping value
     */
    public long getPing(){
        return lastPing != null ? lastPing : -1;
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
