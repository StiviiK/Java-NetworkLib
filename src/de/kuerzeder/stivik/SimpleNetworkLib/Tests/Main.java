package de.kuerzeder.stivik.SimpleNetworkLib.Tests;

import de.kuerzeder.stivik.SimpleNetworkLib.Util.Callback;
import de.kuerzeder.stivik.SimpleNetworkLib.Util.NetworkPacket;

public class Main {

    public static void main(String[] args) {
	// Only for testing, will be removed later on
        TestServer server = new TestServer(8612, true);
        server.startServer();


        /**
        TestClient client = new TestClient("STEFAN-PC", 8612, 0, true);
        client.registerCallback(Callback.ON_SYSTEM_MESSAGE, (msg) -> System.out.println("CLIENT: " + msg)); // To handle maybe via own MSG-System
        client.registerCallback(Callback.ON_ERROR, (o) -> {
            client.disconnect();
            System.err.println("[Error] Client-System halt now!");
            System.exit(-1);
        });
        client.connect();

        client.write(new NetworkPacket("TestPackage", "TestMSG"));
        client.write(new NetworkPacket("TestPackage", new Random().nextFloat()));
        client.write(new NetworkPacket("TestPackage", new Random().nextInt()));
        client.write(new NetworkPacket("TestPackage", new Random().nextBoolean()));
        client.write(new NetworkPacket("TestPackage", new Random().nextLong()));
         */

        // Client disconnect + reconnect test (+ some clients which are connection for no porous)
        TestClient client = new TestClient("178.26.129.220", 8612, 0, true);
        client.connect();
        TestClient client2 = new TestClient("178.26.129.220", 8612, 0, true);
        client2.connect();
        //TestClient client3 = new TestClient("178.26.129.220", 8612, 0, true);
        //client3.connect();
        //TestClient client4 = new TestClient("178.26.129.220", 8612, 0, true);
        //client4.connect();
        //TestClient client5 = new TestClient("178.26.129.220", 8612, 0, true);
        //client5.connect();

        int i = 0;
        while(true){
            i++;
            client.write(new NetworkPacket("TestPackage", "TestMSG"));

            if(i == 3)
                client2.disconnect();

            if(i == 10)
                client2.connect();


            if(i < 3 || i > 10)
                client2.write(new NetworkPacket("TestPackage2", "TestMSG"));

            try {
                Thread.sleep(1000);
                continue;
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
