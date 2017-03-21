package stivik.SimpleNetworkLib.Server;

import stivik.SimpleNetworkLib.Util.EventType;
import stivik.SimpleNetworkLib.Util.NetworkPacket;
import stivik.SimpleNetworkLib.Util.NetworkPacketId;

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

    private ServerSocket s_Socket;
    private int m_Port;
    private List<ServerListener> s_EventListeners = new ArrayList<>();

    public Server(int port) {
        m_Port = port;
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
     * @param clientSocket the s_Socket of client (to send back a message)
     * @param networkPacket which got received from the client
     */
    public abstract void receiveNetworkPacket(Socket clientSocket, NetworkPacket networkPacket);

    /**
     * Override this method to check if the client is loggedin or not
     * @param clientSocket the s_Socket of the client which should get checked
     * @return default value if method gets not overriden
     */
    public boolean isSocketValid(Socket clientSocket) { return true; }

    /**
     * Override this method to handle "login"-Packages from a client
     * @param clientSocket the s_Socket of the client which should get loggedin
     * @param networkPacket the NetworkPacket which the client send
     */
    public void socketLogin(Socket clientSocket, NetworkPacket networkPacket) { }

    /**
     * Override this method to handle "logout"-Packages from a client
     * @param clientSocket the s_Socket of the client which should get loggedin
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
        if(s_Socket == null) {
            try {
                s_Socket = new ServerSocket(this.m_Port);
                dispatchEvent(EventType.ON_MESSAGE, "[Info] Trying to resolve Address");
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(new URL("http://bot.whatismyipaddress.com/").openStream()));
                    dispatchEvent(EventType.ON_MESSAGE, "[Info] Bound to Address " + in.readLine() + ":" + s_Socket.getLocalPort());
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
     * Starts the m_Listener of the Server which accepts
     * new Client-Sockets and handle their Streams and processes
     * the received NetworkPackages
     */
    private void listen(){
        Thread m_Listener = new Thread(() -> {
            while(true){
                try {
                    Socket p_ClientSocket = s_Socket.accept();
                    new Thread(() -> { // Make a new Thread, so we can listen always on this s_Socket (no new s_Socket for a new message)
                        while(p_ClientSocket.isConnected()) {
                            try {
                                ObjectInputStream inputStream = new ObjectInputStream(p_ClientSocket.getInputStream());
                                Object input = inputStream.readObject();

                                if (input instanceof NetworkPacket) {
                                    NetworkPacket networkPacket = (NetworkPacket) input;
                                    if(networkPacket.getId().equals(NetworkPacketId.CLIENT_LOGIN.getId())) {
                                        socketLogin(p_ClientSocket, networkPacket);
                                    } else if(networkPacket.getId().equals(NetworkPacketId.CLIENT_LOGOUT.getId())) {
                                        socketLogout(p_ClientSocket, networkPacket);
                                        break; // We can break here, no new messages from the client
                                    } else if(networkPacket.getId().equals(NetworkPacketId.PING_STATUS.getId())) {
                                        write(p_ClientSocket, new NetworkPacket(NetworkPacketId.PING_REPLY.getId(), true));
                                    } else if(isSocketValid(p_ClientSocket)) {
                                        receiveNetworkPacket(p_ClientSocket, networkPacket);

                                        // Send back a Status
                                        write(p_ClientSocket, new NetworkPacket(NetworkPacketId.REPLY_STATUS.getId(), true));
                                    } else {
                                        write(p_ClientSocket, new NetworkPacket(NetworkPacketId.REPLY_STATUS.getId(), false));
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

        m_Listener.start();
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
        s_EventListeners.add(listener);
    }

    private void dispatchEvent(EventType type, Object arg){
        for (ServerListener listener : s_EventListeners) {
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
