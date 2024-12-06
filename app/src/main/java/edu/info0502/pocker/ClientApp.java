package edu.info0502.pocker;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ClientApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Poker Client Setup ===");

        // Get server address
        String serverAddress;
        while (true) {
            System.out.print("Enter server address (default localhost): ");
            serverAddress = scanner.nextLine().trim();
            if (serverAddress.isEmpty()) {
                serverAddress = "localhost";
                break;
            }
            if (serverAddress.matches("^[a-zA-Z0-9.]+$")) {
                break;
            }
            System.out.println("Please enter a valid server address");
        }

        // Get port number
        int port;
        while (true) {
            try {
                System.out.print("Enter port number (default 12345): ");
                String portInput = scanner.nextLine().trim();
                if (portInput.isEmpty()) {
                    port = 12345;
                    break;
                }
                port = Integer.parseInt(portInput);
                if (port > 0 && port < 65536) {
                    break;
                }
                System.out.println("Port must be between 1 and 65535");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number");
            }
        }

        // Get nickname
        String nickname;
        while (true) {
            System.out.print("Enter your nickname: ");
            nickname = scanner.nextLine().trim();
            if (!nickname.isEmpty() && nickname.matches("^[a-zA-Z0-9_-]{3,15}$")) {
                break;
            }
            System.out.println("Nickname must be 3-15 characters long and contain only letters, numbers, underscores, and hyphens");
        }

        try {
            System.out.println("\nConnecting to server...");
            Client client = new Client(serverAddress, port, nickname);
            client.start();
        } catch (Exception e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            System.exit(1);
        }
    }

    private static class Client {
        private final String serverAddress;
        private final int serverPort;
        private final String nickname;
        private final Main playerHand;
        private Main communityCards;
        private Socket socket;
        private ObjectInputStream serverInput;
        private ObjectOutputStream serverOutput;

        public Client(String serverAddress, int serverPort, String nickname) {
            this.serverAddress = serverAddress;
            this.serverPort = serverPort;
            this.nickname = nickname;
            this.playerHand = new Main();
        }

        public void start() {
            try {
                connectToServer();
                handleServerCommunication();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Connection error: " + e.getMessage());
            } finally {
                cleanup();
            }
        }

        private void connectToServer() throws IOException, ClassNotFoundException {
            socket = new Socket(serverAddress, serverPort);
            serverOutput = new ObjectOutputStream(socket.getOutputStream());
            serverInput = new ObjectInputStream(socket.getInputStream());

            System.out.println("Connected to server: " + serverAddress + ":" + serverPort);

            // Send nickname
            serverOutput.writeObject(nickname);
            serverOutput.flush();

            // Handle nickname response
            String response = (String) serverInput.readObject();
            if ("NICKNAME_TAKEN".equalsIgnoreCase(response)) {
                throw new IOException("Nickname \"" + nickname + "\" is already in use.");
            }
            System.out.println("Nickname accepted!");
        }

        private void handleServerCommunication() throws IOException, ClassNotFoundException {
            System.out.println("Waiting for cards...");
            
            // Start message reader thread
            Thread messageReader = new Thread(this::readServerMessages);
            messageReader.setDaemon(true);
            messageReader.start();

            // Handle user input
            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
            String message;
            while ((message = consoleInput.readLine()) != null) {
                if ("QUIT".equalsIgnoreCase(message)) {
                    serverOutput.writeObject("QUIT");
                    serverOutput.flush();
                    break;
                }
                serverOutput.writeObject(message);
                serverOutput.flush();
            }
        }

private void readServerMessages() {
    try {
        while (!socket.isClosed()) {
            Object serverObject = serverInput.readObject();

            if (serverObject instanceof Carte) {
                Carte card = (Carte) serverObject;
                if (playerHand.getCartes().size() < Main.getTailleMain()) {
                    playerHand.ajouterCarte(card);  // Add card to player hand if there's space
                    System.out.println("Received card: " + card);
                } else {
                    System.out.println("Player's hand is full, cannot add more cards.");
                }
            }
            else if (serverObject instanceof Main) {
                communityCards = (Main) serverObject;
                System.out.println("Community cards: " + communityCards);

                // Combine player's cards and community cards, but ensure we don't exceed the hand size
                Main finalHand = new Main();

                // Add player's cards to final hand (only up to the hand size limit)
                finalHand.ajouterCartes(playerHand.getCartes());

                // Add community cards, ensuring we don't exceed the total hand size
                int remainingSlots = Main.getTailleMain() - finalHand.getCartes().size();  // Use the getter method     
                List<Carte> communityCardsToAdd = communityCards.getCartes().subList(0, Math.min(remainingSlots, communityCards.getCartes().size()));
                finalHand.ajouterCartes(communityCardsToAdd);

                // Send final hand to the server
                serverOutput.writeObject(finalHand);
                serverOutput.flush();
                System.out.println("Final hand sent to the server for evaluation: " + finalHand);
            }
            else if (serverObject instanceof String) {
                System.out.println(serverObject);
            }
        }
    } catch (IOException | ClassNotFoundException e) {
        if (!socket.isClosed()) {
            System.err.println("Error reading from server: " + e.getMessage());
        }
    }
}


        private void cleanup() {
            try {
                if (serverOutput != null) serverOutput.close();
                if (serverInput != null) serverInput.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.err.println("Error during cleanup: " + e.getMessage());
            }
        }
    }
}
