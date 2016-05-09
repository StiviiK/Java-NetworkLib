package de.kuerzeder.stivik.SimpleNetworkLib;

import de.kuerzeder.stivik.SimpleNetworkLib.Client.Client;
import de.kuerzeder.stivik.SimpleNetworkLib.Server.Server;
import de.kuerzeder.stivik.SimpleNetworkLib.Util.CallbackIds;

public class Main {

    public static void main(String[] args) {
	// Only for testing, will be removed later on
        Server server = new Server(1234, true);
        server.registerCallback(CallbackIds.ON_SYSTEM_MESSAGE, (msg) -> System.out.println("SERVER: " + msg)); // To handle maybe via own MSG-System
        server.registerCallback(CallbackIds.SERVER_PRE_START, (o) -> {});
        server.registerCallback(CallbackIds.SERVER_POST_START, (o) -> {});
        server.startServer();


        Client client = new Client("STEFAN-PC", 1234, 0, true);
        client.registerCallback(CallbackIds.ON_SYSTEM_MESSAGE, (msg) -> System.out.println("CLIENT: " + msg)); // To handle maybe via own MSG-System
        client.registerCallback(CallbackIds.ON_ERROR, (o) -> {
            client.disconnect();
            System.err.println("[Error] Client-System halt now!");
            System.exit(-1);
        });

        client.connect();
    }
}
