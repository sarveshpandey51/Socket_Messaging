import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketClient {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 3000;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVER_IP, SERVER_PORT); // Socket Connection Established

            System.out.println("Connected to server.");

            // Start threads for sending and receiving messages
            Thread receiveThread = new Thread(new ReceiveMessages(socket)); // ReceiveMessages used as an Argument To
                                                                            // the Thread constructor
            receiveThread.start(); // Start Method used to start the execution of the thread

            Thread sendThread = new Thread(new SendMessages(socket));
            sendThread.start();

        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + SERVER_IP);
        } catch (IOException e) {
            System.err.println("Error connecting to the server: " + e.getMessage());
        }
    }

    static class ReceiveMessages implements Runnable { // Runnable interface implemented to handle Client requests
        private Socket socket;

        public ReceiveMessages(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String receivedMessage;
                while ((receivedMessage = inFromServer.readLine()) != null) {
                    System.out.println("Received from server: " + receivedMessage);
                }
                inFromServer.close();
            } catch (IOException e) {
                System.err.println("Error receiving message from server: " + e.getMessage());
            }
        }
    }

    static class SendMessages implements Runnable {

        private Socket socket; // instance variable named socket of type Socket.

        public SendMessages(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
                PrintWriter outToServer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

                String message;
                while ((message = userInput.readLine()) != null) {
                    outToServer.println(message);
                }

                // Close resources
                userInput.close();
                outToServer.close();
                socket.close();
            } catch (IOException e) {
                System.err.println("Error sending message to server: " + e.getMessage());
            }
        }
    }
}
