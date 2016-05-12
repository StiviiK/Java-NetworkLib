package de.kuerzeder.stivik.SimpleNetworkLib.Tests;

import de.kuerzeder.stivik.SimpleNetworkLib.Client.Client;
import de.kuerzeder.stivik.SimpleNetworkLib.Util.NetworkPacket;

/**
 * SimpleNetworkLib:
 * @author Stefan Kürzeder
 * created on 11.05.2016 in BY, Germany
 */
public class TestClient extends Client {

    public TestClient(String host, int port, int timeout, boolean debug) {
        super(host, port, timeout, debug);
    }

    @Override
    public void login() {

    }


    @Override
    public void logout() {

    }

    @Override
    public void receiveNetworkPackage(NetworkPacket networkPackage) {
        System.out.println("Client: NetworkPacket[" + networkPackage.getId() + ":" + networkPackage.get(1) + "]");
    }
}
