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

    private InetSocketAddress remoteHost;
    private int timeout;
    private Socket networkSocket;
    private HashMap<CallbackIds, Executable> callbacks;

    public Client(String host, int port, int timeout) {
        this.remoteHost = new InetSocketAddress(host, port);
        this.timeout    = timeout;
        this.callbacks  = new HashMap<>();
    }

    public void connect(){
        try {
            assert networkSocket == null;
            if(!networkSocket.isConnected()) {
                networkSocket = new Socket();
                networkSocket.connect(this.remoteHost, this.timeout);
            } else {
                throw new AlreadyConnectedException();
            }

            System.out.println("[Info] Connected successfull to the Server(-Socket) on " + this.remoteHost.toString());
            runCallback(CallbackIds.ON_CONNECTED);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("[Error] Connection to the Server(-Socket) failed. See StackTrace.");
            runCallback(CallbackIds.ON_ERROR);
        }
    }

    public void disconnect(){
        assert networkSocket != null;
        if(networkSocket.isConnected()) {
            try {
                networkSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void registerCallback(CallbackIds callbackId, Executable callback) {
        callbacks.put(callbackId, callback);
    }

    public void runCallback(CallbackIds callbackId, Object arg) {
        Executable callback = callbacks.get(callbackId);
        if(callback != null) {
            callback.run(arg);
        }
    }

    public void runCallback(CallbackIds callbackId) {
        Executable callback = callbacks.get(callbackId);
        if(callback != null) {
            callback.run(null);
        }
    }
}
