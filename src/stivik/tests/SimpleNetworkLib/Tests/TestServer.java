package stivik.tests.SimpleNetworkLib.Tests;

import stivik.SimpleNetworkLib.Server.Server;
import stivik.SimpleNetworkLib.Server.ServerListener;
import stivik.SimpleNetworkLib.Util.NetworkPacket;

import java.net.Socket;
import java.util.ArrayList;

/**
 * SimpleNetworkLib:
 * @author Stefan Kürzeder
 * created on 11.05.2016 in BY, Germany
 */
public class TestServer extends Server implements ServerListener {

    private ArrayList<Socket> validSockets = new ArrayList<>();

    public TestServer(int port) {
        super(port);
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
        System.err.println("New Login!");
        validSockets.add(clientSocket);
    }

    @Override
    public void socketLogout(Socket clientSocket, NetworkPacket networkPackage) {
        System.err.println("New Logout!");
        validSockets.remove(clientSocket);
    }

    @Override
    public boolean isSocketValid(Socket clientSocket) {
        return validSockets.indexOf(clientSocket) != -1;
    }

    @Override
    public void receiveNetworkPacket(Socket clientSocket, NetworkPacket networkPacket) {
        System.out.println("Server: Socket[" + clientSocket.getInetAddress().toString() + "] -> NetworkPacket[" + networkPacket.getId() + ":" + networkPacket.get(1) + "]");
    }
}
