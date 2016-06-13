# SimpleNetworkLib
A basic simple Libary for Java-Developers to provide Client, Server Applications.

## Install
To use this Libary, just download the JAR-File from "realeases" and include as external Libary to your Project.

## How to use the Server
You have to create a new Class which extends Server and has to implement ServerListener (otherwise the supe.addListener won't work).
You have to implement these Methods!
```java
public class TestServer extends Server implements ServerListener {

    private ArrayList<Socket> validSockets = new ArrayList<>();

    public TestServer(int port, boolean debug) {
        super(port, debug);
        super.addListener(this);
    }

    // Listener Methods
    @Override
    public void preStart() {
        System.out.println("SERVER: [Info] pre-Start!");
    }

    @Override
    public void postStart() {
        System.out.println("SERVER: [Info] post-Start!");
    }

    @Override
    public void onMessage(Object msg) {
        System.out.println("SERVER: " + msg);
    }

    @Override
    public void onError(Object arg) {
        System.err.println("SERVER: " + arg);
    }
    //

    @Override
    public void socketLogin(Socket clientSocket, NetworkPacket networkPackage) {
        System.err.println("New Login!");
        validSockets.add(clientSocket);
    }

    @Override
    public void socketLogout(Socket clientSocket, NetworkPacket networkPackage) {
        System.err.println("New Logout!");
        validSockets.remove(clientSocket);
    }

    @Override
    public boolean isSocketValid(Socket clientSocket) {
        return validSockets.indexOf(clientSocket) != -1;
    }

    @Override
    public void receiveNetworkPacket(Socket clientSocket, NetworkPacket networkPacket) {
        System.out.println("Server: Socket[" + clientSocket.getInetAddress().toString() + "] -> NetworkPacket[" + networkPacket.getId() + ":" + networkPacket.get(1) + "]");
    }
}
```

## How to use the Client
You have to create a new Class which extends Client and has to implement ServerListener (otherwise the supe.addListener won't work), see here.
```java
public class TestClient extends Client implements ClientListener {

    public TestClient(String host, int port, int timeout, boolean debug) {
        super(host, port, timeout, debug);
        super.addListener(this);
    }

    @Override
    public void login() {
        //super.write() what you like more, its the same
        write(new NetworkPacket(Util.CLIENT_LOGIN_PACKAGE, ""));
    }

    @Override
    public void logout() {
        //super.write() what you like more, its the same
        write(new NetworkPacket(Util.CLIENT_LOGOUT_PACKAGE, ""));
    }

    @Override
    public void receiveNetworkPacket(NetworkPacket networkPacket) {
        System.out.println("Client: NetworkPacket[" + networkPacket.getId() + ":" + networkPacket.get(1) + "]");
    }

    @Override
    public void onMessage(Object msg) {
        System.out.println("CLIENT: " + msg);
    }

    @Override
    public void onError(Object msg) {
        System.err.println("CLIENT: " + msg);
    }

    @Override
    public void onConnected() { }

    @Override
    public void onDisconnect() { }

    @Override
    public void onConnectionLost() { }
}
```
