Client–Server Messaging System (Java Sockets)

This project is a simple client–server messaging application built using Java Sockets, multithreading, and basic I/O streams. It demonstrates how a server can handle multiple clients simultaneously, how clients can communicate with the server, and how messages are broadcast across all connected clients.

🚀 Features
Server

Accepts multiple client connections using ServerSocket

Creates a new ClientHandler thread for each connection

Broadcasts messages sent by a client to all other connected clients

Manages connected clients safely through synchronized operations

Client

Connects to the server using a standard TCP socket

Reads and writes messages through input/output streams

Uses a ServerListener thread to constantly listen for messages from the server

Includes simple classes for managing and observing server messages

🧱 Project Structure
├── Client.java
├── Client.class
├── Client$ClientManager.class
├── Client$ClientObserver.class
├── Client$ServerListener.class
├── Server.java
├── Server.class
├── Server$ClientHandler.class
├── Server$1.class

Key Classes
Server.java

Starts the server

Accepts client sockets

Spawns ClientHandler threads

ClientHandler

Runs for each connected client

Reads incoming messages

Broadcasts them to all other clients

Client.java

Connects to the server

Sends user messages to the server

Displays messages received from the server

ServerListener

Thread that listens for incoming messages from the server

ClientManager / ClientObserver

Assist with managing client updates and UI/console output

🖥️ How to Run
1. Start the Server
javac Server.java
java Server


The server will start and listen for client connections (typically on port 1234 unless changed in your code).

2. Start a Client
javac Client.java
java Client


Run multiple clients in separate terminals to test messaging.

💬 How It Works

When the server starts, it waits for clients to connect.

Each client connects via TCP and opens input/output streams.

When a client sends a message:

The server reads it

The server broadcasts it to all connected clients

Each client’s ServerListener prints new messages to the console in real-time.

🔧 Requirements

Java 8+

Terminal or IDE (IntelliJ, VS Code, Eclipse)

📝 Use Cases

Learning basic socket programming

Practicing networking concepts

Building chat applications

Understanding multithreading in Java

📌 Future Improvements

Add GUI using Swing/JavaFX

Add usernames

Add private messaging

Use encryption for secure communication

Store chat logs
