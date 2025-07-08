package jins.db.whatsappgroup.serveur;

import jins.db.whatsappgroup.tools.Notification;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    private String username;
    public String confirmation;

    public Client(String username) {
        final String HOST = "localhost";
        final int PORT = 12345;


        try (
                Socket socket = new Socket(HOST, PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );
                Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Connecté au serveur " + HOST + ":" + PORT);
            Notification.NotifSuccess("Success", "Connecté au serveur " + HOST + ":" + PORT);


            //System.out.print("Entrez votre nom d'utilisateur : ");
            this.username = username;

            /*if (username.isEmpty()) {
                username = "Anonyme_" + System.currentTimeMillis();
            }*/

            out.println("USERNAME:" + username);

            confirmation = in.readLine();
            System.out.println(confirmation);

            if (!confirmation.startsWith("BIENVENUE")) {
                System.out.println("Erreur d'authentification. Déconnexion...");

            }

            System.out.println("=== Connecté en tant que " + username + " ===");
            System.out.println("Tapez 'quit' pour quitter");

            String messageUtilisateur;

            while (true) {
                System.out.print(username + " > ");
                messageUtilisateur = scanner.nextLine();

                if ("quit".equalsIgnoreCase(messageUtilisateur)) {
                    out.println("quit");
                    String reponse = in.readLine();
                    System.out.println("Serveur : " + reponse);
                    break;
                }

                out.println(messageUtilisateur);

                String reponse = in.readLine();
                if (reponse != null) {
                    System.out.println("Serveur : " + reponse);
                }
            }

        } catch (UnknownHostException e) {
            System.err.println("Hôte inconnu : " + HOST);
        } catch (IOException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
        }

        System.out.println("Connexion fermée");
    }
}
