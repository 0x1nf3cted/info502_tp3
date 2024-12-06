package edu.info0502.pocker;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerApp {
public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    System.out.println("=== Poker Server Setup ===");
    
    // Default values
    int port = 12345;
    int maxPlayers = 6;
    
    try {

        // Start the server
        System.out.println("\nStarting server...");
        Server server = new Server(port, maxPlayers);
        System.out.println("Server is running on port " + port);
        System.out.println("Maximum players: " + maxPlayers);
        server.start();
        
    } catch (Exception e) {
        System.err.println("Failed to start server: " + e.getMessage());
        System.exit(1);
    } finally {
        scanner.close();
    }
}
    private static class Server {
        private final Map<String, Main> playerHands = new ConcurrentHashMap<>();
        private final int port;
        private final int maxPlayers;
        private final ServerSocket serverSocket;
        private final ConcurrentHashMap<String, ClientHandler> clients;
        private final ExecutorService clientThreadPool;
        private Talon talon;
        private final Main communityCards;
        private boolean gameInProgress;
        private int currentPlayerIndex;
        private static final int MINIMUM_PLAYERS = 2;

        public Server(int port, int maxPlayers) throws IOException {
            this.port = port;
            this.maxPlayers = maxPlayers;
            this.serverSocket = new ServerSocket(port);
            this.clients = new ConcurrentHashMap<>();
            this.clientThreadPool = Executors.newFixedThreadPool(maxPlayers);
            this.talon = new Talon(1); // Using 1 deck
            this.communityCards = new Main();
            this.gameInProgress = false;
            this.currentPlayerIndex = 0;
        }

        public void start() {
            System.out.println("Server started on port " + port);
            
            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    handleNewClient(clientSocket);
                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        }

        private void handleNewClient(Socket clientSocket) {
            if (clients.size() >= maxPlayers) {
                try {
                    ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                    out.writeObject("Server is full. Please try again later.");
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error handling full server: " + e.getMessage());
                }
                return;
            }

            ClientHandler clientHandler = new ClientHandler(clientSocket);
            clientThreadPool.execute(clientHandler);
        }

        private void startGame() {
            if (clients.size() >= MINIMUM_PLAYERS && !gameInProgress) {
                gameInProgress = true;
                talon = new Talon(1); // Reset and shuffle deck
                dealInitialCards();
                dealCommunityCards();
            }
        }

        private void dealInitialCards() {
            // Deal 2 cards to each player
            for (ClientHandler client : clients.values()) {
                for (int i = 0; i < 2; i++) {
                    try {
                        Carte card = talon.tirerCarte();
                        client.sendCard(card);
                    } catch (IllegalStateException e) {
                        System.err.println("Error dealing cards: " + e.getMessage());
                        return;
                    }
                }
            }
        }

        private void dealCommunityCards() {
            // Deal 5 community cards
            try {
                for (int i = 0; i < 5; i++) {
                    communityCards.ajouterCarte(talon.tirerCarte());
                }
                broadcastCommunityCards(communityCards);
            } catch (IllegalStateException e) {
                System.err.println("Error dealing community cards: " + e.getMessage());
            }
        }

        private void broadcastCommunityCards(Main communityCards) {
            for (ClientHandler client : clients.values()) {
                client.sendCommunityCards(communityCards);
            }
        }

        private void broadcastMessage(String message, String excludeNickname) {
            for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
                if (!entry.getKey().equals(excludeNickname)) {
                    entry.getValue().sendMessage(message);
                }
            }
        }

        private class ClientHandler implements Runnable {
            private final Socket clientSocket;
            private ObjectInputStream input;
            private ObjectOutputStream output;
            private String nickname;
            private final Main playerHand;

            public ClientHandler(Socket socket) {
                this.clientSocket = socket;
                this.playerHand = new Main();
            }

            @Override
            public void run() {
                try {
                    setupStreams();
                    handleNicknameRegistration();
                    handleClientCommunication();
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Error handling client: " + e.getMessage());
                } finally {
                    cleanup();
                }
            }

            private void setupStreams() throws IOException {
                output = new ObjectOutputStream(clientSocket.getOutputStream());
                input = new ObjectInputStream(clientSocket.getInputStream());
            }

            private void handleNicknameRegistration() throws IOException, ClassNotFoundException {
                nickname = (String) input.readObject();
                
                if (clients.containsKey(nickname)) {
                    output.writeObject("NICKNAME_TAKEN");
                    output.flush();
                    throw new IOException("Nickname already taken");
                }

                output.writeObject("NICKNAME_ACCEPTED");
                output.flush();
                clients.put(nickname, this);
                
                broadcastMessage(nickname + " has joined the game!", nickname);
                System.out.println(nickname + " connected");
                
                startGame();
            }

        private void handleClientCommunication() throws IOException, ClassNotFoundException {
            while (!clientSocket.isClosed()) {
                Object message = input.readObject();

                if (message instanceof String) {
                    String strMessage = (String) message;
                    if ("QUIT".equalsIgnoreCase(strMessage)) {
                        break;
                    }
                    broadcastMessage(nickname + ": " + strMessage, nickname);
                } else if (message instanceof Main) {
                    Main finalHand = (Main) message;
                    evaluateHand(finalHand);
                    collectAndDetermineWinner(finalHand);
                }
            }
        }

        private synchronized void collectAndDetermineWinner(Main hand) {
            // Store the hand with the nickname
            playerHands.put(nickname, hand);

            // Check if all players have submitted their hands
            if (playerHands.size() == clients.size()) {
                determineWinner();
            }
        }

        private void determineWinner() {
            // Evaluate all hands and determine the winner
            String winner = null;
            Main bestHand = null;
            CombinaisonPoker bestCombination = null;

            for (Map.Entry<String, Main> entry : playerHands.entrySet()) {
                String player = entry.getKey();
                Main hand = entry.getValue();
                CombinaisonPoker combination = hand.evaluerMain();

                System.out.println(player + " has " + combination.name() + ": " + hand);

                if (bestCombination == null || combination.compareTo(bestCombination) > 0) {
                    bestHand = hand;
                    bestCombination = combination;
                    winner = player;
                }
            }

            // Announce the winner
            broadcastMessage("The winner is " + winner + " with " + bestCombination.name() + "!", null);
            playerHands.clear(); // Reset for next round
        }


            private void evaluateHand(Main finalHand) {
                try {
                    CombinaisonPoker combinaison = finalHand.evaluerMain();
                    sendMessage("Your hand: " + finalHand.toString() + " - " + combinaison.name());
                } catch (IllegalStateException e) {
                    sendMessage("Invalid hand: " + e.getMessage());
                }
            }

            public void sendCard(Carte card) {
                try {
                    if (card == null) {
                        throw new IllegalArgumentException("Cannot send a null card");
                    }
                    playerHand.ajouterCarte(card);
                    output.writeObject(card);
                    output.flush();
                    System.out.println("Sent card to " + nickname + ": " + card);
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid card: " + e.getMessage());
                } catch (IOException e) {
                    System.err.println("Error sending card to " + nickname + ": " + e.getMessage());
                    cleanup(); // Handle client disconnection
                }
            }


            public void sendCommunityCards(Main communityCards) {
                try {
                    output.writeObject(communityCards);
                    output.flush();
                } catch (IOException e) {
                    System.err.println("Error sending community cards to " + nickname + ": " + e.getMessage());
                }
            }

            public void sendMessage(String message) {
                try {
                    output.writeObject(message);
                    output.flush();
                } catch (IOException e) {
                    System.err.println("Error sending message to " + nickname + ": " + e.getMessage());
                }
            }

            private void cleanup() {
                try {
                    if (nickname != null) {
                        clients.remove(nickname);
                        broadcastMessage(nickname + " has left the game!", nickname);
                    }
                    if (input != null) input.close();
                    if (output != null) output.close();
                    if (clientSocket != null) clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error during cleanup: " + e.getMessage());
                }
            }
        }
    }
}