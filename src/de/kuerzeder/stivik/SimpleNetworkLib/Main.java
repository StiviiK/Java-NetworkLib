package de.kuerzeder.stivik.SimpleNetworkLib;

import de.kuerzeder.stivik.SimpleNetworkLib.Client.Client;
import de.kuerzeder.stivik.SimpleNetworkLib.Server.Server;
import de.kuerzeder.stivik.SimpleNetworkLib.Util.Callback;
import de.kuerzeder.stivik.SimpleNetworkLib.Util.NetworkPackage;

public class Main {

    public static void main(String[] args) {
	// Only for testing, will be removed later on
        Server server = new Server(8612, true);
        server.registerCallback(Callback.ON_SYSTEM_MESSAGE, (msg) -> System.out.println("SERVER: " + msg)); // To handle maybe via own MSG-System
        server.registerCallback(Callback.ON_NEW_NETPACKAGE, (networkPackage) -> System.out.println("SERVER: " + ((NetworkPackage) networkPackage).getId()));
        server.registerCallback(Callback.SERVER_PRE_START, (o) -> {});
        server.registerCallback(Callback.SERVER_POST_START, (o) -> {});
        server.startServer();


        Client client = new Client("STEFAN-PC", 8612, 0, true);
        client.registerCallback(Callback.ON_SYSTEM_MESSAGE, (msg) -> System.out.println("CLIENT: " + msg)); // To handle maybe via own MSG-System
        client.registerCallback(Callback.ON_NEW_NETPACKAGE, (networkPackage) -> System.out.println("CLIENT: " + ((NetworkPackage) networkPackage).getId()));
        client.registerCallback(Callback.ON_ERROR, (o) -> {
            client.disconnect();
            System.err.println("[Error] Client-System halt now!");
            System.exit(-1);
        });
        client.connect();

        client.write(new NetworkPackage("TestPackage", "TestMSG"));

         /**
            Client client = new Client("178.26.129.220", 8612, 0, true);
            client.connect();
            Client client2 = new Client("178.26.129.220", 8612, 0, true);
            client2.connect();
            Client client3 = new Client("178.26.129.220", 8612, 0, true);
            client3.connect();
            Client client4 = new Client("178.26.129.220", 8612, 0, true);
            client4.connect();
            Client client5 = new Client("178.26.129.220", 8612, 0, true);
            client5.connect();

            int i = 0;
            while(true){
                i++;
                client.write(new NetworkPackage("TestPackage", "TestMSG"));

                if(i == 3)
                    client2.disconnect();

                if(i < 3)
                    client2.write(new NetworkPackage("TestPackage2", "TestMSG"));

                try {
                    Thread.sleep(1000);
                    continue;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
         **/
    }
}
