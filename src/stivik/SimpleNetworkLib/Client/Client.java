package stivik.SimpleNetworkLib.Client;

import stivik.SimpleNetworkLib.Util.EventType;
import stivik.SimpleNetworkLib.Util.NetworkPacket;
import stivik.SimpleNetworkLib.Util.NetworkPacketId;

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

    private boolean m_DebugMode = true;
    private InetSocketAddress s_HostAdress;
    private int m_Timeout;
    private Socket s_Socket;
    private boolean m_IsLoggedIn;
    private Thread m_Listener;
    private List<ClientListener> m_EventListeners = new ArrayList<>();
    private Thread s_PingListener;

    // Ping
    private Long m_Ping;
    private Date m_LastPingTest;

    public Client(String host, int port, int timeout, boolean debug) {
        m_DebugMode = debug;
        s_HostAdress = new InetSocketAddress(host, port);
        m_Timeout = timeout;
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
     * Connects to the server and opens an new s_Socket
     */
    public void connect(){
        try {
            if(s_Socket == null || !s_Socket.isConnected() || s_Socket.isClosed()) {
                s_Socket = new Socket();
                s_Socket.connect(this.s_HostAdress, this.m_Timeout);
            } else {
                throw new AlreadyConnectedException();
            }

            dispatchEvent(EventType.ON_MESSAGE, "[Info] Connected successfull to the Server(-Socket) on " + this.s_HostAdress.toString());
            dispatchEvent(EventType.ON_CONNECTED);

            // Start listening && Login to the Server
            listen();
            login();
        } catch (IOException | AlreadyConnectedException e) {
            if(m_DebugMode) {
                e.printStackTrace();
                dispatchEvent(EventType.ON_ERROR, "[Error] Connection to the Server(-Socket) failed. See StackTrace.");
            } else {
                dispatchEvent(EventType.ON_ERROR, "[Error] Connection to the Server(-Socket) failed. (enable debug-mode, for stacktrace)");
            }
            dispatchEvent(EventType.ON_ERROR, e.getLocalizedMessage());
        }
    }

    /**
     * Closes the connection to the server and closes the s_Socket
     */
    public void disconnect(){
        logout();

        if(s_PingListener != null) {
            s_PingListener.interrupt();
        }
        if(m_Listener != null) {
            m_Listener.interrupt();
        }
        if(s_Socket != null && s_Socket.isConnected()) {
            try {
                s_Socket.close();
                dispatchEvent(EventType.ON_DISCONNECTED);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Starts the m_Listener of the Client which waits
     * for new NetworkPackets from the Server and handles them
     */
    private void listen(){
        if(m_Listener != null && m_Listener.isAlive())
            return;

        m_Listener = new Thread(() -> {
           while(s_Socket != null && s_Socket.isConnected()){
               try {
                   ObjectInputStream inputStream = new ObjectInputStream(s_Socket.getInputStream());
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

        m_Listener.start();
    }

    /**
     * Writes a new NetworkPacket on the (Client-)s_Socket,
     * so the Server can read them and handle it
     * @param networkPacket which should get written on the s_Socket-Stream
     */
    public void write(NetworkPacket networkPacket){
        try {
            if(s_Socket != null && s_Socket.isConnected() && !s_Socket.isClosed()) {
                ObjectOutputStream outputStream = new ObjectOutputStream(s_Socket.getOutputStream());
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
        if(s_PingListener == null) {
            s_PingListener = new Thread(() -> {
                while (true) {
                    try {Thread.sleep(3 * 1000);} catch (InterruptedException ignored) {}

                    if(s_Socket != null && s_Socket.isConnected() && !s_Socket.isClosed()) {
                        m_LastPingTest = Calendar.getInstance().getTime();
                        write(new NetworkPacket(NetworkPacketId.PING_STATUS.getId(), true));
                    }
                }
            });
            s_PingListener.start();
        }
    }

    /**
     * Is the "hidden" Callback handler for an Ping-Reply Package
     * from the Server. It sets the m_Ping value.
     */
    private void onPingReply(){
        m_Ping = (Calendar.getInstance().getTime().getTime() - m_LastPingTest.getTime());
    }

    /**
     * Returns the m_Ping latency between the Client and the Server.
     * Gets update all ~3000ms (+ Latency).
     * @return the last captured ping value
     */
    public long getPing(){
        return m_Ping != null ? m_Ping : -1;
    }
    //

    // Event Methods
    protected void addListener(ClientListener listener){
        m_EventListeners.add(listener);
    }

    private void dispatchEvent(EventType type, Object arg){
        for (ClientListener listener : m_EventListeners) {
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
