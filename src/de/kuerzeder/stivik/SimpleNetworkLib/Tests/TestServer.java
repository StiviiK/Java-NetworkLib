package de.kuerzeder.stivik.SimpleNetworkLib.Tests;

import de.kuerzeder.stivik.SimpleNetworkLib.Server.Server;
import de.kuerzeder.stivik.SimpleNetworkLib.Util.NetworkPackage;
import java.net.Socket;

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
        System.out.println("SERVER: [Info] pre-Start!");
    }

    @Override
    public void postStart() {
        System.out.println("SERVER: [Info] post-Start!");
    }

    @Override
    public void socketLogin(Socket clientSocket, NetworkPackage networkPackage) {

    }

    @Override
    public void socketLogout(Socket clientSocket, NetworkPackage networkPackage) {

    }

    @Override
    public boolean isSocketValid(Socket clientSocket) {
        return true;
    }

    @Override
    public void receiveNetworkPackage(Socket clientSocket, NetworkPackage networkPackage) {
        System.out.println("Server: Socket[" + clientSocket.getInetAddress().toString() + "] -> NetworkPackage[" + networkPackage.getId() + ":" + networkPackage.get(1) + "]");
    }
}
