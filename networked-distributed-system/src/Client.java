import java.io.*;
import java.net.*;

public class Client {
    private static PrintWriter out;
    private static BufferedReader in;
    private static boolean running = true;

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java Client <ID> <server_ip> <server_port>");
            return;
        }

        int clientId = Integer.parseInt(args[0]);
        String serverIp = args[1];
        int serverPort = Integer.parseInt(args[2]);

        // Handle Ctrl-C to gracefully shut down the client
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (running) {
                out.println("quit");
                running = false;
                System.out.println("Client is shutting down...");
            }
        }));

        try (Socket socket = new Socket(serverIp, serverPort)) {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send client ID to the server
            out.println(clientId);

            new Thread(new ServerListener()).start();

            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            String userInput;
            while (running && (userInput = consoleReader.readLine()) != null) {
                out.println(userInput);
                if (userInput.equalsIgnoreCase("quit")) {
                    running = false;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ServerListener implements Runnable {
        @Override
        public void run() {
            String serverMessage;
            try {
                while ((serverMessage = in.readLine()) != null) {
                    System.out.println(serverMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}