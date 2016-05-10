package de.kuerzeder.stivik.SimpleNetworkLib.Util;

/**
 * SimpleNetworkLib: Client callback enums
 * @author Stefan Kürzeder
 * created on 09.05.2016 in BY, Germany
 */
public enum Callback {
    ON_ERROR, ON_SYSTEM_MESSAGE, ON_NEW_NETPACKAGE, // Global
    SERVER_PRE_START, SERVER_POST_START, // Server only
    CLIENT_ON_CONNECTED, CLIENT_ON_DISCONNECTED, // Client only
}
