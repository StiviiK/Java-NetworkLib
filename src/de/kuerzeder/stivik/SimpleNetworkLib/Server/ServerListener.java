package de.kuerzeder.stivik.SimpleNetworkLib.Server;

import de.kuerzeder.stivik.SimpleNetworkLib.Util.NetListener;

/**
 * SimpleNetworkLib:
 * @author Stefan Kürzeder
 * created on 13.05.2016 in BY, Germany
 */
public interface ServerListener extends NetListener {
    void preStart();
    void postStart();
}
