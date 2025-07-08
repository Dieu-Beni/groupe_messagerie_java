package jins.db.whatsappgroup.serveur;


import jins.db.whatsappgroup.models.Membre;
import jins.db.whatsappgroup.services.impl.MembreImpl;
import jins.db.whatsappgroup.services.impl.MessageImpl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;


public class Serveur {
    private static final Map<String, Socket> clientsConnectes = new ConcurrentHashMap<>();
    private static final List<String> mots_imjurieux = new ArrayList<String>(
            Arrays.asList("GENOCID", "TERRORISM", "ATTACK", "CHELSEA", "JAVA NEKHOUL")
    );
    static EntityManagerFactory emf = Persistence.createEntityManagerFactory("wahtasapp_group");
    static EntityManager entityManager = emf.createEntityManager();
    static MessageImpl messageImpl = new MessageImpl(entityManager);
    static MembreImpl membreImpl = new MembreImpl(entityManager);

    public static void main(String[] args) {
        final int PORT = 12345;

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serveur démarré sur le port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouvelle connexion depuis : " + clientSocket.getInetAddress());

                new Thread(() -> traiterClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Erreur serveur : " + e.getMessage());
        }
    }

    private static void traiterClient(Socket clientSocket) {
        String username = null;

        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream())
                );
                PrintWriter out = new PrintWriter(
                        clientSocket.getOutputStream(), true
                )
        ) {

            while (true) {
                String premierMessage = in.readLine();
                if (premierMessage != null && premierMessage.startsWith("USERNAME:") && clientsConnectes.size() <= 6) {
                    username = premierMessage.substring(9); // Enlever "USERNAME:"

                    if (clientsConnectes.containsKey(username)) {
                        out.println("ERREUR: Nom d'utilisateur déjà pris !");
                        diffuserMessage("SYSTEME_ERROR_"+username,"Deconnecte","");
                        return;
                    }

                    out.println("BIENVENUE " + username + " ! Vous êtes connecté.");
                    System.out.println("Utilisateur connecté : " + username);
                    clientsConnectes.put(username, clientSocket);
                    diffuserMessage("SYSTÈME", username + " a rejoint le chat", username);
                    break;

                } else if (clientsConnectes.size() > 6){
                    out.println("ERREUR: Le groupe est plein!");
                }else {
                    out.println("ERREUR: Authentification requise");
                }
            }

            String messageRecu;

            while ((messageRecu = in.readLine()) != null) {
                System.out.println("[" + username + "] : " + messageRecu);
                if (messageRecu.equals("SYSTÈME_QUIT")) {
                    out.println("Au revoir ! " + username);
                    break;
                }
                if (estInjurieux(messageRecu)) {
                    out.println("Vous etes bani du groupe pour message injurieux!!");
                    diffuserMessage("SYSTÈME", username + " est bani du groupe", username);
                    break;
                }

                diffuserMessage(username, messageRecu, username);
                //Sauvegarde des messages
                Membre membre = membreImpl.findMembreByPseudo(username);
                System.out.println(messageImpl.sendMessage(messageRecu,membre));
                out.println("Message envoyé à tous les clients");
            }



        } catch (IOException e) {
            System.err.println("Erreur avec le client " + username + " : " + e.getMessage());
        } finally {
            if (username != null) {
                clientsConnectes.remove(username);
                System.out.println("Utilisateur déconnecté : " + username);
                diffuserMessage("SYSTÈME", username + " a quitté le chat", username);
            }
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Erreur fermeture socket : " + e.getMessage());
            }
        }
    }

    private static void diffuserMessage(String expediteur, String message, String expediteurOriginal) {
        String messageComplet = "[" + expediteur + "] " + message;

        for (Map.Entry<String, Socket> entry : clientsConnectes.entrySet()) {
            String nomClient = entry.getKey();
            Socket socketClient = entry.getValue();

            if (!nomClient.equals(expediteurOriginal)) {
                try {
                    PrintWriter out = new PrintWriter(socketClient.getOutputStream(), true);
                    out.println("BROADCAST: " + messageComplet);
                } catch (IOException e) {
                    System.err.println("Erreur envoi message à " + nomClient + " : " + e.getMessage());
                    clientsConnectes.remove(nomClient);
                }
            }

        }
    }
    private static boolean estInjurieux(String message){
        return mots_imjurieux.stream()
                .anyMatch(mot -> Pattern.compile("\\b" + Pattern.quote(mot) + "\\b", Pattern.CASE_INSENSITIVE)
                        .matcher(message).find());

    }

}
