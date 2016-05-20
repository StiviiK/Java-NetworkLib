package de.kuerzeder.stivik.SimpleNetworkLib.Tests;

import de.kuerzeder.stivik.SimpleNetworkLib.Server.Server;
import de.kuerzeder.stivik.SimpleNetworkLib.Server.ServerListener;
import de.kuerzeder.stivik.SimpleNetworkLib.Util.EventType;
import de.kuerzeder.stivik.SimpleNetworkLib.Util.NetworkPacket;

import java.net.Socket;

/**
 * SimpleNetworkLib:
 * @author Stefan Kürzeder
 * created on 11.05.2016 in BY, Germany
 */
public class TestServer extends Server implements ServerListener {

    public TestServer(int port, boolean debug) {
        super(port, debug);
        super.addListener(this);
    }

    // Listener Methods
    @Override
    public void preStart() {
        System.out.println("SERVER: [Info] pre-Start!");
    }

    @Override
    public void postStart() {
        System.out.println("SERVER: [Info] post-Start!");
    }

    @Override
    public void onMessage(Object msg) {
        System.out.println("SERVER: " + msg);
    }

    @Override
    public void onError(Object arg) {
        System.err.println("SERVER: " + arg);
    }
    //

    @Override
    public void socketLogin(Socket clientSocket, NetworkPacket networkPackage) {

    }

    @Override
    public void socketLogout(Socket clientSocket, NetworkPacket networkPackage) {

    }

    @Override
    public boolean isSocketValid(Socket clientSocket) {
        return true;
    }

    @Override
    public void receiveNetworkPacket(Socket clientSocket, NetworkPacket networkPacket) {
        System.out.println("Server: Socket[" + clientSocket.getInetAddress().toString() + "] -> NetworkPacket[" + networkPacket.getId() + ":" + networkPacket.get(1) + "]");
    }
}
