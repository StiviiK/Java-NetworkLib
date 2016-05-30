package de.kuerzeder.stivik.SimpleNetworkLib.Client;

import de.kuerzeder.stivik.SimpleNetworkLib.Util.NetListener;

/**
 * SimpleNetworkLib:
 * @author Stefan Kürzeder
 * created on 30.05.2016 in BY, Germany
 */
public interface ClientListener extends NetListener {
    void onConnected();
    void onDisconnect();
    void onConnectionLost();
}
