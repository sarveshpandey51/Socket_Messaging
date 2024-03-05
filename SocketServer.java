import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class SocketServer {
    private static final int SERVER_PORT = 3000;
    private static final List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server started. Waiting for clients...");

            // Start a thread to listen for incoming client connections
            Thread clientListenerThread = new Thread(() -> {
                while (true) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                        // Start a new thread to handle client communication
                        ClientHandler clientHandler = new ClientHandler(clientSocket);
                        clients.add(clientHandler);
                        Thread clientThread = new Thread(clientHandler);
                        clientThread.start();
                    } catch (IOException e) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            });
            clientListenerThread.start();

            // Main thread for sending messages to clients
            BufferedReader serverInput = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String messageToSend = serverInput.readLine();
                sendToAllClients("Server: " + messageToSend);
            }
        } catch (IOException e) {
            System.err.println("Error starting the server: " + e.getMessage());
        }
    }

    static void sendToAllClients(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter outToClient;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (
                    BufferedReader inFromClient = new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter outToClient = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()),
                            true)) {
                this.outToClient = outToClient;

                String clientMessage;
                while ((clientMessage = inFromClient.readLine()) != null) {
                    System.out.println("Received from client " + clientSocket.getInetAddress().getHostAddress() + ": "
                            + clientMessage);

                    // Echo the message back to the client
                    outToClient.println("Server received: " + clientMessage);
                }
            } catch (IOException e) {
                System.err.println("Error handling client connection: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                }
            }
        }

        public void sendMessage(String message) {
            if (outToClient != null) {
                outToClient.println(message);
            }
        }
    }
}
