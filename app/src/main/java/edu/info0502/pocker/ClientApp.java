package edu.info0502.pocker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientApp {

    private static final String SERVER_ADDRESS = "10.11.18.72";
    private static final int SERVER_PORT = 8888;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;

    public void connectToServer() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // un thread pour recevoir les messages du serveur
            new Thread(this::receiveMessages).start();

            // envoyer les commande d'utilisateur
            handleUserInput();

        } catch (IOException e) {
            System.out.println("Erreur de connexion au serveur");
            e.printStackTrace();
        }
    }

    private void receiveMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            System.out.println("Déconnecté du serveur");
        }
    }

    private void handleUserInput() {
        try (BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {
            String input;
            while ((input = userInput.readLine()) != null) {
                System.out.println("Votre commande: " + input);
                out.println(input);
                if (input.equalsIgnoreCase("QUIT")) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    private void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ClientApp client = new ClientApp();
        client.connectToServer();
    }



}
