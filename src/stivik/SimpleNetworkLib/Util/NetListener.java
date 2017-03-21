package stivik.SimpleNetworkLib.Util;

import java.util.EventListener;

/**
 * SimpleNetworkLib:
 * @author Stefan Kürzeder
 * created on 13.05.2016 in BY, Germany
 */
public interface NetListener extends EventListener {
    void onMessage(Object arg);
    void onError(Object arg);
}

