package edu.info0502.pocker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerApp {

    private static final int PORT = 8888;
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private PokerHoldem currentGame;
    private boolean gameInProgress = false;

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serveur de poker démarré sur le port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Erreur du serveur: " + e.getMessage());
        }
    }

    public synchronized void startGame(String initiator) {
        if (gameInProgress) {
            sendMessageToPlayer(initiator, "Une partie est déjà en cours.");
            return;
        }
        if (clients.size() < 2) {
            sendMessageToPlayer(initiator, "Il faut au moins 2 joueurs pour commencer.");
            return;
        }

        gameInProgress = true;
        currentGame = new PokerHoldem(new ArrayList<>(clients.keySet()));
        broadcastMessage("SYSTEM", "La partie commence !");
        currentGame.demarrerPartie();

        for (String player : clients.keySet()) {
            Joueur joueur = currentGame.getJoueurParNom(player);
            sendMessageToPlayer(player, "Vos cartes: " + joueur.getCartesPrivees());
        }
        dealFlop();
    }

    private void dealFlop() {
        currentGame.distribuerFlop();
        broadcastMessage("SYSTEM", "Flop: " + currentGame.getCartesCommunes());
        dealTurn();
    }

    private void dealTurn() {
        currentGame.distribuerTurn();
        broadcastMessage("SYSTEM", "Turn: " + currentGame.getCartesCommunes());
        dealRiver();
    }

    private void dealRiver() {
        currentGame.distribuerRiver();
        broadcastMessage("SYSTEM", "River: " + currentGame.getCartesCommunes());
        showResults();
    }

    private void showResults() {
        Map<String, String> results = currentGame.calculerResultats();
        for (Map.Entry<String, String> entry : results.entrySet()) {
            broadcastMessage("SYSTEM", entry.getKey() + ": " + entry.getValue());
        }

        String winner = currentGame.determinerGagnant();
        broadcastMessage("SYSTEM", "Le gagnant est: " + winner);
        endGame();
    }

    private void endGame() {
        gameInProgress = false;
        currentGame = null;
        broadcastMessage("SYSTEM", "La partie est terminée.");
    }

    public void broadcastMessage(String sender, String message) {
        for (ClientHandler handler : clients.values()) {
            handler.sendMessage(sender + ": " + message);
        }
    }

    public void sendMessageToPlayer(String nickname, String message) {
        ClientHandler handler = clients.get(nickname);
        if (handler != null) {
            handler.sendMessage("PRIVÉ: " + message);
        }
    }

    class ClientHandler implements Runnable {
        private final Socket socket;
        private final ServerApp server;
        private PrintWriter out;
        private BufferedReader in;
        private String nickname;

        public ClientHandler(Socket socket, ServerApp server) {
            this.socket = socket;
            this.server = server;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                while (nickname == null) {
                    out.println("Entrez votre nickname:");
                    nickname = in.readLine();
                    if (server.clients.putIfAbsent(nickname, this) == null) {
                        out.println("Bienvenue " + nickname);
                        break;
                    }
                    out.println("Ce nickname est déjà pris. Essayez un autre.");
                    nickname = null;
                }

                String input;
                while ((input = in.readLine()) != null) {
                    processCommand(input);
                }
            } catch (IOException e) {
                System.err.println("Erreur avec le client " + nickname);
            } finally {
                server.clients.remove(nickname);
            }
        }

        private void processCommand(String command) {
            switch (command.toUpperCase()) {
                case "START":
                    server.startGame(nickname);
                    break;
                default:
                    out.println("Commande non reconnue.");
            }
        }

        public void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }
    }

    public static void main(String[] args) {
        new ServerApp().start();
    }
}
