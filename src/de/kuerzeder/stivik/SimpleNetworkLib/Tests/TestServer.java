package de.kuerzeder.stivik.SimpleNetworkLib.Tests;

import de.kuerzeder.stivik.SimpleNetworkLib.Server.Server;
import de.kuerzeder.stivik.SimpleNetworkLib.Util.NetworkPacket;

import java.net.Socket;
import java.util.EventListener;

/**
 * SimpleNetworkLib:
 * @author Stefan Kürzeder
 * created on 11.05.2016 in BY, Germany
 */
public class TestServer extends Server {

    public TestServer(int port, boolean debug) {
        super(port, debug);
    }

    @Override
    public void preStart() {

    }

    @Override
    public void postStart() {
        System.out.println("SERVER: [Info] post-Start!");
    }

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
    public void receiveNetworkPackage(Socket clientSocket, NetworkPacket networkPackage) {
        System.out.println("Server: Socket[" + clientSocket.getInetAddress().toString() + "] -> NetworkPacket[" + networkPackage.getId() + ":" + networkPackage.get(1) + "]");
    }
}
