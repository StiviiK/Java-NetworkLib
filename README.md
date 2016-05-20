# SimpleNetworkLib
A basic simple Libary for Java-Developers to provide Client, Server Applications.

## Install
To use this Libary, just download the JAR-File from "realeases" and include as external Libary to your Project.

## How to use the Server
You have to create a new Class which extends Server and has to implement ServerListener (otherwise the supe.addListener won't work).
You have to implement these Methods!
```java
public class TestServer extends Server implements ServerListener {

    public TestServer(int port, boolean debug) {
        super(port, debug);
        super.addListener(this);
    }

    // Listener Methods
    @Override
    public void preStart() {} // Gets called before the Server starts (Listener-Event)

    @Override
    public void postStart() {} // Gets called when the Server has started (Listener-Event)

    @Override
    public void onMessage(Object msg) {} // Gets called for a new Message (e.g. Debug) (Listener-Event)

    @Override
    public void onError(Object arg) {} // Gets called when an Error occures
    //

    @Override
    public void socketLogin(Socket clientSocket, NetworkPacket networkPackage) {} // Gets executed when the client sends a "Login"-Packet, you have to handle this by your-self!

    @Override
    public void socketLogout(Socket clientSocket, NetworkPacket networkPackage) {} // Gets executed when the client sends a "Logout"-Packet, you have to handle this by your self! (The listening on this socket gets stopped!)

    @Override
    public boolean isSocketValid(Socket clientSocket) {
        return true;
    } // Gets executed to demitire if is client valid, use with socketLogin (implement check by your-self!)

    @Override
    public void receiveNetworkPacket(Socket clientSocket, NetworkPacket networkPacket) {} // Gets called when the Server receives a new Packet from the Client!
}
```

## How to use the Client
You have to create a new Class which extends Client, see here.
```java
public class TestClient extends Client {

    public TestClient(String host, int port, int timeout, boolean debug) {
        super(host, port, timeout, debug);
    }

    @Override
    public void login() {} // Work in Progress


    @Override
    public void logout() {} // Work in Progress

    @Override
    public void receiveNetworkPackage(NetworkPacket networkPackage) {} // Gets called when the Client receives a new Packet from the Server!
}
```
