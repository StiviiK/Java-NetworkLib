package de.kuerzeder.stivik.SimpleNetworkLib;

import de.kuerzeder.stivik.SimpleNetworkLib.Client.Client;
import de.kuerzeder.stivik.SimpleNetworkLib.Util.CallbackIds;

public class Main {

    public static void main(String[] args) {
	// Only for testing, will be removed later on
        Client client = new Client("STEFAN-PC", 22222, 0);
        client.registerCallback(CallbackIds.ON_CONNECTED, (k) -> {System.out.println(k);});
        client.runCallback(CallbackIds.ON_CONNECTED, 11234);
    }
}
