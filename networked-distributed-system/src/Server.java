import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 12345;
    private static final int HEARTBEAT_INTERVAL = 20000; // 20 seconds
    private static Server instance;
    private static Map<Integer, ClientHandler> clients = new ConcurrentHashMap<>();
    private static int coordinatorId = -1;

    // Private constructor to prevent instantiation
    private Server() {}

    // Static method to provide access to the single instance
    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    public static void main(String[] args) {
        Server server = Server.getInstance();
        server.start();
    }

    private void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            // Start the coordinator heartbeat thread
            new Thread(new CoordinatorHeartbeat()).start();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                int clientId = Integer.parseInt(in.readLine());

                if (clients.containsKey(clientId)) {
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    out.println("ID " + clientId + " is already in use. Connection closed.");
                    clientSocket.close();
                    continue;
                }

                ClientHandler clientHandler = new ClientHandler(clientSocket, clientId);
                clients.put(clientId, clientHandler);

                if (clients.size() == 1) {
                    coordinatorId = clientId;
                    clientHandler.sendMessage("You are the coordinator. You will perform heartbeat checks every 20 seconds.");
                } else {
                    clientHandler.sendMessage("The current coordinator is client " + coordinatorId + ". The coordinator will perform heartbeat checks every 20 seconds.");
                }

                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class CoordinatorHeartbeat implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(HEARTBEAT_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (coordinatorId != -1) {
                    System.out.println("Coordinator is performing a heartbeat check...");
                }
            }
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private int clientId;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket, int clientId) {
            this.socket = socket;
            this.clientId = clientId;
            try {
                this.out = new PrintWriter(socket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("quit")) {
                        break;
                    }
                    System.out.println("Message from client " + clientId + ": " + message);
                    handleMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        }

        private void handleMessage(String message) {
            if (message.equalsIgnoreCase("list")) {
                listClients();
            } else if (message.startsWith("private")) {
                sendPrivateMessage(message);
            } else if (message.startsWith("broadcast")) {
                broadcastMessage(message);
            } else {
                out.println("Unknown command.");
            }
        }

        private void listClients() {
            StringBuilder sb = new StringBuilder("Active clients:\n");
            for (Map.Entry<Integer, ClientHandler> entry : clients.entrySet()) {
                sb.append("Client ").append(entry.getKey()).append(" - ");
                if (entry.getKey() == coordinatorId) {
                    sb.append("Coordinator\n");
                } else {
                    sb.append("Member\n");
                }
            }
            out.println(sb.toString());
        }

        private void sendPrivateMessage(String message) {
            String[] parts = message.split(" ", 3);
            if (parts.length < 3) {
                out.println("Usage: private <client_id> <message>");
                return;
            }
            int targetClientId = Integer.parseInt(parts[1]);
            String msg = parts[2];
            ClientHandler targetClient = clients.get(targetClientId);
            if (targetClient != null) {
                targetClient.sendMessage("Private message from client " + clientId + ": " + msg);
                System.out.println("Private message from client " + clientId + " to client " + targetClientId + ": " + msg);
            } else {
                out.println("Client " + targetClientId + " not found.");
            }
        }

        private void broadcastMessage(String message) {
            String msg = message.substring(10); // remove "broadcast " prefix
            for (ClientHandler client : clients.values()) {
                if (client.clientId != clientId) {
                    client.sendMessage("Broadcast message from client " + clientId + ": " + msg);
                }
            }
            System.out.println("Broadcast message from client " + clientId + ": " + msg);
        }

        private void sendMessage(String message) {
            out.println(message);
        }

        private void closeConnection() {
            try {
                boolean wasCoordinator = (clientId == coordinatorId);
                clients.remove(clientId);
                socket.close();
                notifyClientsOfDisconnection(wasCoordinator);
                if (wasCoordinator && !clients.isEmpty()) {
                    coordinatorId = clients.keySet().iterator().next();
                    clients.get(coordinatorId).sendMessage("You are the new coordinator.");
                    System.out.println("The current coordinator has left the server.");
                    System.out.println("Client " + coordinatorId + " is the new coordinator.");
                }
                System.out.println("Client " + clientId + " disconnected.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void notifyClientsOfDisconnection(boolean wasCoordinator) {
            for (ClientHandler client : clients.values()) {
                if (client.clientId != clientId) {
                    if (wasCoordinator) {
                        client.sendMessage("The current coordinator has left the server.");
                    }
                    client.sendMessage("Client " + clientId + " has disconnected.");
                }
            }
        }
    }
}