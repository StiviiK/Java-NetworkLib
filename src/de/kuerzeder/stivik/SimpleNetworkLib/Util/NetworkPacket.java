package de.kuerzeder.stivik.SimpleNetworkLib.Util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

/**
 * SimpleNetworkLib:
 *
 * @author Stefan Kürzeder
 *         created on 10.05.2016 in BY, Germany
 */
public class NetworkPacket implements Serializable {

    private static final long serialVersionUID = 6658257544678633086L;
    private ArrayList<Object> netpack;

    public NetworkPacket(String id, Object ...o){
        netpack = new ArrayList<>();
        netpack.add(id);
        for (Object current : o) {
            netpack.add(current);
        }
    }

    public String getId(){
        if(!(netpack.get(0) instanceof String)){
            throw new IllegalArgumentException("Id is not a String.");
        }
        return (String) netpack.get(0);
    }

    public Object get(int index){
        return netpack.get(index);
    }
}
