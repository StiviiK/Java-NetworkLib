package de.kuerzeder.stivik.SimpleNetworkLib.Tests;

import de.kuerzeder.stivik.SimpleNetworkLib.Client.Client;
import de.kuerzeder.stivik.SimpleNetworkLib.Client.ClientListener;
import de.kuerzeder.stivik.SimpleNetworkLib.Util.NetworkPacket;
import de.kuerzeder.stivik.SimpleNetworkLib.Util.Util;

/**
 * SimpleNetworkLib:
 * @author Stefan Kürzeder
 * created on 11.05.2016 in BY, Germany
 */
public class TestClient extends Client implements ClientListener {

    public TestClient(String host, int port, int timeout, boolean debug) {
        super(host, port, timeout, debug);
        super.addListener(this);
    }

    @Override
    public void login() {
        //write(new NetworkPacket(Util.CLIENT_LOGIN_PACKAGE, ""));
    }

    @Override
    public void logout() {
        //write(new NetworkPacket(Util.CLIENT_LOGOUT_PACKAGE, ""));
    }

    @Override
    public void receiveNetworkPacket(NetworkPacket networkPacket) {
        System.out.println("Client: NetworkPacket[" + networkPacket.getId() + ":" + networkPacket.get(1) + "]");
    }

    @Override
    public void onMessage(Object msg) {
        System.out.println("CLIENT: " + msg);
    }

    @Override
    public void onError(Object msg) {
        System.err.println("CLIENT: " + msg);
    }

    @Override
    public void onConnected() { }

    @Override
    public void onDisconnect() { }

    @Override
    public void onConnectionLost() { }
}
