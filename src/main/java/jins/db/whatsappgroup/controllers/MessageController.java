package jins.db.whatsappgroup.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import jins.db.whatsappgroup.models.Membre;
import java.net.URL;
import java.util.ResourceBundle;

public class MessageController implements Initializable {
    @FXML
    private VBox messagesBox;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private TextField messageInput;

    @FXML
    private Button sendButton;

    private final Membre membre = ConnexionController.membre;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        sendButton.setOnAction(e -> envoyerMessage());
    }

    @FXML
    private void envoyerMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty()) {
            afficherMessage(membre.getPseudo(), message);
            messageInput.clear();

        }
    }

    public void afficherMessage(String expediteur, String message) {
        boolean isMoi = expediteur.equals(membre.getPseudo());

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-padding: 10; -fx-background-radius: 10; " +
                (isMoi ? "-fx-background-color: #DCF8C6;" : "-fx-background-color: #E4E6EB;"));
        messageLabel.setMaxWidth(300);

        ImageView avatar = new ImageView(new Image(getClass().getResourceAsStream("/assets/user.png")));
        avatar.setFitWidth(30);
        avatar.setFitHeight(30);
        avatar.setClip(new Circle(15, 15, 15));

        HBox bubble = new HBox(isMoi ? messageLabel : avatar, isMoi ? avatar : messageLabel);
        bubble.setSpacing(10);
        bubble.setAlignment(isMoi ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        bubble.setPadding(new Insets(5));

        Platform.runLater(() -> {
            messagesBox.getChildren().add(bubble);
            scrollPane.setVvalue(1.0);
        });
    }
}
