package jins.db.whatsappgroup.controllers;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import jins.db.whatsappgroup.models.Membre;
import jins.db.whatsappgroup.models.Message;
import jins.db.whatsappgroup.services.impl.MembreImpl;
import jins.db.whatsappgroup.services.impl.MessageImpl;
import jins.db.whatsappgroup.tools.Notification;
import jins.db.whatsappgroup.tools.Outils;

import javax.sound.sampled.*;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static jins.db.whatsappgroup.HelloApplication.entityManager;


public class MainController {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Membre membre = ConnexionController.membre;
    private final static MessageImpl messageImpl = new MessageImpl(entityManager);
    private final static MembreImpl membreImpl = new MembreImpl(entityManager);
    private boolean isEmojiVis = false;
    private GridPane emojiGrid = new GridPane();




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
            displayEmoji();
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
                Notification.NotifError("Erreur", "Le groupe est plein!");
                closeConnection();
            }else {
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

                        String ms = line.substring(begin + 1, end);
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
        Label messageLabel = new Label(message);

        messageLabel.setWrapText(true);
        messageLabel.setMinHeight(Region.USE_PREF_SIZE);
        //String color = "#DCF8C6";
        messageLabel.setStyle("-fx-padding: 10; -fx-background-radius: 10; " +
                (isMoi ? "-fx-background-color: #4fe100;" : "-fx-background-color: #E4E6EB;")+
                "-fx-font-family: 'Times New Roman'; -fx-font-size: 20"
        );
        messageLabel.setMaxWidth(300);
        VBox vBox = isMoi ? new VBox(messageLabel, timeLabel) :  new VBox(pseudo, messageLabel,timeLabel);
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
        Outils.load(event, "Menu", "hello-view.fxml");
    }

    @FXML
    void showProfil(ActionEvent event) throws IOException {
        closeConnection();
        Outils.load(event, "Profil", "views/profil.fxml");
    }
    void showAlert(String content)  {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Nouveau Message");
        alert.setHeaderText("Vous avez recu un nouveau message");
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void affiche_emoji(ActionEvent event) throws IOException {
        isEmojiVis = ! isEmojiVis;
        emojiGrid.setVisible(isEmojiVis);
        messagesBox.getChildren().add(emojiGrid);
    }
    void displayEmoji2(){

        Collection<Emoji> emojis = EmojiManager.getAll();
        Iterator<Emoji> iterator = emojis.iterator();
        int column = 0;
        int row = 0;
        while (iterator.hasNext()) {
            Emoji emoji = iterator.next();
            Button emojiButton = new Button(emoji.getUnicode());
            emojiButton.setStyle("-fx-font-size: 24; -fx-font-family: 'Segoe UI Emoji';");
            emojiButton.setOnMouseClicked((event) -> {
                if (messageInput != null){
                    messageInput.setText(messageInput.getText()+" "+emoji.getUnicode());
                }
                isEmojiVis = false;
                emojiGrid.setVisible(false);
            });
            emojiGrid.add(emojiButton, column, row);
            column++;
            if (column == 4) {
                column = 0;
                row++;
            }
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
    void displayEmoji() {
        emojiGrid.getChildren().clear(); // Nettoyer avant d'ajouter

        Collection<Emoji> emojis = EmojiManager.getAll();
        int column = 0;
        int row = 0;

        for (Emoji emoji : emojis) {
            Button emojiButton = new Button(emoji.getUnicode());
            emojiButton.setPrefSize(50, 50); // Taille carrée
            emojiButton.setStyle("""
            -fx-font-size: 24;
            -fx-font-family: 'Segoe UI Emoji';
            -fx-background-radius: 8;
            -fx-background-color: transparent;
            -fx-cursor: hand;
        """);

            // Style au survol
            emojiButton.setOnMouseEntered(e ->
                    emojiButton.setStyle(emojiButton.getStyle() + "-fx-background-color: #e0e0e0;")
            );
            emojiButton.setOnMouseExited(e ->
                    emojiButton.setStyle(emojiButton.getStyle().replace("-fx-background-color: #e0e0e0;", ""))
            );

            emojiButton.setOnMouseClicked(event -> {
                if (messageInput != null) {
                    messageInput.setText(messageInput.getText() + emoji.getUnicode());
                }
                isEmojiVis = false;
                emojiGrid.setVisible(false);
            });

            emojiGrid.add(emojiButton, column, row);
            column++;
            if (column == 4) { // Plus d'emojis par ligne
                column = 0;
                row++;
            }
        }

        // Appliquer marges et padding
        emojiGrid.setHgap(10);
        emojiGrid.setVgap(10);
        emojiGrid.setPadding(new Insets(10));
    }


}
