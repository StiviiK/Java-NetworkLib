# SimpleNetworkLib
A basic simple Libary for Java-Developers to provide Client, Server Applications.

## Install
To use this Libary, just download the JAR-File from "realeases" and include as external Libary to your Project.

## How to use the Server
```java
Server serverInstance = new Server(int port, boolean debug); // Create the Server instance

// Register some important callbacks
server.registerCallback(Callback.ON_SYSTEM_MESSAGE, (msg) -> {});
server.registerCallback(Callback.ON_NEW_NETPACKAGE, (pack) -> {
  final NetworkPackage networkPackage = (NetworkPackage) pack;
  // Do something with the networkPackage
});
server.startServer(); // At least start the Server, so it listens on the given TCP-Port
```

## How to use the Client
```java
Client client = new Client(String host, int port, int timeout, boolean debug);

// Register some important callbacks
client.registerCallback(Callback.ON_SYSTEM_MESSAGE, (msg) -> {});
client.registerCallback(Callback.ON_NEW_NETPACKAGE, (pack) -> {
  final NetworkPackage networkPackage = (NetworkPackage) pack;
  // Do something with the networkPackage
});
client.connect(); // At least (try) to connect to the Server
```

## Todo
- Add Login(), Logout() function -> so the Server can broadcast NetworkPackages to all clients
- Fixe some @Todo's in the Code :P
- Cleanup code
