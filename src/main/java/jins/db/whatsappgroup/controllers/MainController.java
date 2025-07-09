package jins.db.whatsappgroup.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Popup;
import javafx.stage.Window;
import jins.db.whatsappgroup.models.Membre;
import jins.db.whatsappgroup.models.Message;
import jins.db.whatsappgroup.services.impl.MembreImpl;
import jins.db.whatsappgroup.services.impl.MessageImpl;
import jins.db.whatsappgroup.tools.*;

import javax.sound.sampled.*;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import static jins.db.whatsappgroup.WhatsappgroupApplication.entityManager;


public class MainController {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Membre membre = ConnexionController.membre;
    private final static MessageImpl messageImpl = new MessageImpl(entityManager);
    private final static MembreImpl membreImpl = new MembreImpl(entityManager);
    private Popup emojiPopup ;

    @FXML
    private TextArea messageInput;
    @FXML
    private VBox messagesBox;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Button sendButon;


    @FXML
    public void initialize() {

        if (!membre.isBanned()) {
            List<Message> messages = messageImpl.findAllMessages();
            for (Message message : messages) {
                afficherMessage(message.getMembre().getPseudo(), message.getContenu(),  message.getDateEnvoi());

            }
            startConnection();
        }
        else if (sendButon != null) {
            sendButon.setDisable(true);
            afficherMessage("Systeme","Vous etes bani de ce groupe",null);
        }

    }

    private void startConnection() {
        try {
            String HOST = "localhost";
            int PORT = 12345;
            socket = new Socket(HOST, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("USERNAME:" + membre.getPseudo());

            String response = in.readLine();
            if (response.startsWith("BIENVENUE")) {
                Notification.NotifSuccess("Connecté", response);
                listenToServer(); // Lancer le thread d'écoute
            } else if (response.equals("ERREUR: Le groupe est plein!")){
                if (sendButon != null){
                    sendButon.setDisable(true);
                }
                Notification.NotifError("Erreur", "Le groupe est plein!");
                if (sendButon != null){
                    sendButon.setDisable(true);
                }
                closeConnection();
            }else {
                if (sendButon != null){
                    sendButon.setDisable(true);
                }
                Notification.NotifError("Erreur", "Nom d'utilisateur déjà pris ou refusé.");
                closeConnection();
            }

        } catch (IOException e) {
            Notification.NotifError("Connexion", "Erreur : " + e.getMessage());
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    private void listenToServer() {
        Thread listenThread = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {

                    if (line.contains("SYSTEME_ERROR_"+membre.getPseudo())){
                        closeConnection();
                        return;
                    }
                    if (line.startsWith("BROADCAST") && !line.contains("SYSTEME_ERROR")) {

                        int begin = line.indexOf('[');
                        int end = line.indexOf(']');

                        Platform.runLater(() -> {
                            try {
                                displaySound();
                            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                                System.out.println(e.getMessage());
                                throw new RuntimeException(e);
                            }
                        });

                        afficherMessage(line.substring(begin + 1, end), line.substring(end + 1), null);
                    } else if (!line.equals("Message envoyé à tous les clients") && !line.contains("SYSTEME_ERROR")) {
                        afficherMessage("SYSTEME", line, null);
                    }
                    if (line.equals("ERREUR: Nom d'utilisateur déjà pris !")){

                        Platform.runLater(() -> {
                            try {
                                displaySound();
                            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                                System.out.println(e.getMessage());
                                throw new RuntimeException(e);
                            }
                        });

                        afficherMessage("SYSTEME", line, null);
                        return;
                    }if (line.equals("Vous etes bani du groupe pour message injurieux!!")){
                        membre.setBanned(true);
                        membreImpl.updateMembre(membre);
                        sendButon.setDisable(true);
                        closeConnection();
                    }
                }
            } catch (IOException e) {
                System.out.println("Connexion perdue : " + e.getMessage());
            }
        });
        listenThread.setDaemon(true);
        listenThread.start();
    }

    @FXML
    void sendMessage(ActionEvent event) {
        String msg = messageInput.getText();
        if (msg.trim().isEmpty()) return;

        out.println(msg);
        afficherMessage(membre.getPseudo(), msg, null);

        messageInput.clear();
    }

    private void closeConnection() {
        try {
            if (out != null) out.println("SYSTÈME_QUIT");
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Erreur fermeture socket : " + e.getMessage());
        }
    }

    public void afficherMessage(String expediteur, String message, LocalDateTime date) {

        if (date == null) {
            date = LocalDateTime.now();
        }
        Label timeLabel = new Label(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        timeLabel.setStyle("-fx-font-size: 10; -fx-text-fill: grey;");
        boolean isMoi = expediteur.equals(membre.getPseudo());
        Label pseudo = new Label(expediteur.toUpperCase());
        pseudo.setStyle("-fx-font-size: 15;  -fx-text-fill: grey;");
        TextFlow formatted = new TextFlow();

        formatted.setStyle("-fx-padding: 10; -fx-background-radius: 10; " +
                (isMoi ? "-fx-background-color: #4fe100;" : "-fx-background-color: #E4E6EB;")+
                "-fx-font-family: 'Segoe UI Emoji'; -fx-font-size: 20"
        );
        formatted.setPrefHeight(Region.USE_PREF_SIZE);
        formatted.setMaxWidth(300);
        displayFormattedMessage(message, formatted);
        VBox vBox = isMoi ? new VBox(formatted, timeLabel) :  new VBox(pseudo, formatted,timeLabel);
        HBox bubble = new HBox(vBox);
        bubble.setFillHeight(true);
        bubble.setSpacing(50);
        bubble.setAlignment(isMoi ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        bubble.setPadding(new Insets(5));
        messagesBox.heightProperty().addListener((observable, oldValue, newValue) -> {
            double height = 460;
            if (newValue.doubleValue() > height) {
                scrollPane.setVvalue(1);
            }
        });
        messagesBox.setSpacing(10);
        Platform.runLater(() -> {
            messagesBox.getChildren().add(bubble);
            scrollPane.setContent(messagesBox);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setFitToWidth(true);
        });
    }

    @FXML
    void logOut(ActionEvent event) throws IOException {
        closeConnection();
        Outils.load(event, "Menu", "menu.fxml");
    }

    @FXML
    void showProfil(ActionEvent event) throws IOException {
        closeConnection();
        Outils.load(event, "Profil", "views/profil.fxml");
    }
    @FXML
    void affiche_emoji(ActionEvent event) throws Exception {
        if (emojiPopup != null && emojiPopup.isShowing()) {
            emojiPopup.hide(); // Toggle: si déjà visible, on ferme
            return;
        }
       try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/jins/db/whatsappgroup/views/EmojiList.fxml"));
            Parent emojiPane = loader.load();

            emojiPopup = new Popup();
            emojiPopup.getContent().add(emojiPane);
            emojiPopup.setAutoHide(true); // se ferme quand on clique en dehors


            EmojiListController emojiController = loader.getController();
            emojiController.setMessageInput(this.messageInput);


            Node sourceNode = (Node) event.getSource();
            Scene scene = sourceNode.getScene();
            Window window = scene.getWindow();


            Bounds bounds = sourceNode.localToScreen(sourceNode.getBoundsInLocal());
            emojiPopup.show(window, bounds.getMinX(), -bounds.getMinY()+800);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void displaySound() throws LineUnavailableException, UnsupportedAudioFileException, IOException {
        // Charger l'audio d'origine
        AudioInputStream originalAudio = AudioSystem.getAudioInputStream(
                Objects.requireNonNull(MainController.class.getResource("/jins/db/whatsappgroup/audio/notif.wav"))
        );

        // Convertir en format supporté
        AudioFormat targetFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                44100f,
                16,
                2,
                4,       // 2 canaux * 2 octets
                44100f,
                false    // little-endian
        );

        AudioInputStream convertedAudio = AudioSystem.getAudioInputStream(targetFormat, originalAudio);

        // Lire le son
        Clip clip = AudioSystem.getClip();
        clip.open(convertedAudio);
        clip.start();
    }

    public void displayFormattedMessage(String rawMessage, TextFlow outputFlow) {
        outputFlow.getChildren().clear();

        int i = 0;
        while (i < rawMessage.length()) {
            int codePoint = rawMessage.codePointAt(i);
            String currentChar = new String(Character.toChars(codePoint));

            String shortName = EmojiOne.getInstance().unicodeToShortname(currentChar);
            Emoji emoji = EmojiOne.getInstance().getEmoji(shortName);

            if (emoji != null) {
                // Emoji reconnu : afficher une image
                String hex = emoji.getHex(); // ex: 1f602
                Image img = new Image(getClass().getResourceAsStream("/jins/db/whatsappgroup/png_40/" + hex + ".png"), 20, 20, true, true);
                ImageView imageView = new ImageView(img);

                imageView.setTranslateY(3); // ajuste verticalement si besoin
                outputFlow.getChildren().add(imageView);
            } else {
                // Sinon, ajouter le caractère normalement
                outputFlow.getChildren().add(new Text(currentChar));
            }

            i += Character.charCount(codePoint);
        }
    }

}
