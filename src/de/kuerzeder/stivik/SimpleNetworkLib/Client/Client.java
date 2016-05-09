package de.kuerzeder.stivik.SimpleNetworkLib.Client;

import de.kuerzeder.stivik.SimpleNetworkLib.Util.Util;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * SimpleNetworkLib: A simple Client class for Network Communication
 * @author Stefan Kürzeder
 * created on 09.05.2016 in BY, Germany
 */
public class Client {

    private InetSocketAddress remoteHost;
    private int timeout;
    private Socket networkSocket;

    public Client(String host, int port, int timeout) {
        this.remoteHost = new InetSocketAddress(host, port);
        this.timeout    = timeout;
    }

    public void connect(){
        if(networkSocket == null || !networkSocket.isConnected()) {
            try {
                networkSocket = new Socket();
                networkSocket.connect(this.remoteHost, this.timeout);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnect(){
        if(networkSocket != null || networkSocket.isConnected()) {
            try {
                networkSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
