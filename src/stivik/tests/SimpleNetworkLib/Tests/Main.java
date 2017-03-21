package stivik.tests.SimpleNetworkLib.Tests;

import stivik.SimpleNetworkLib.Util.NetworkPacket;

public class Main {

    public static void main(String[] args) {
        // Only for testing, will be removed later on
        TestServer server = new TestServer(998);
        server.startServer();

        // Client disconnect + reconnect test (+ some clients which are connection for no porous)
        TestClient client = new TestClient("localhost", 998, 0, true);
        client.connect();
        TestClient client2 = new TestClient("localhost", 998, 0, true);
        client2.connect();

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
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
