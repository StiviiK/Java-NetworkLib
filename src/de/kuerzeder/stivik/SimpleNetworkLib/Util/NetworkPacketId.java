package de.kuerzeder.stivik.SimpleNetworkLib.Util;

/**
 * SimpleNetworkLib:
 * @author Stefan Kürzeder
 * created on 15.06.2016 in BY, Germany
 */
public enum NetworkPacketId {
    REPLY_STATUS(0x0), CLIENT_LOGIN(0x1), CLIENT_LOGOUT(0x2), PING_STATUS(0x3), PING_REPLY(0x4);

    private Integer id;

    NetworkPacketId(int Id) {
        id = Id;
    }

    public String getId() {
        return id.toString();
    }
}
