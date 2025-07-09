package jins.db.whatsappgroup.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import jins.db.whatsappgroup.models.Membre;
import jins.db.whatsappgroup.services.impl.MembreImpl;
import jins.db.whatsappgroup.tools.Notification;
import jins.db.whatsappgroup.tools.Outils;
import jins.db.whatsappgroup.tools.Utils;

import java.io.IOException;
import java.util.Optional;

import static jins.db.whatsappgroup.WhatsappgroupApplication.entityManager;

public class ConnexionController {

    MembreImpl  membreImpl = new MembreImpl(entityManager);
    protected static Membre membre;
    @FXML
    private PasswordField passwordTFD;
    @FXML
    private TextField passwordInsTFD;
    @FXML
    private TextField pseudoTFD;
    @FXML
    private TextField passwordUpTFD;
    @FXML
    private TextField pseudoUpTFD;
    @FXML
    private Pane excusePane;

    @FXML
    void initialize(){
        if (membre != null && pseudoUpTFD != null){
            pseudoUpTFD.setText(membre.getPseudo());
            System.out.println(membre.getPseudo());
        }
        if (membre != null && membre.isBanned() && excusePane != null){
            excusePane.setVisible(true);
        }
    }

    @FXML
    void login(ActionEvent event) {

        if(!pseudoTFD.getText().trim().isEmpty() && !passwordTFD.getText().trim().isEmpty()){
            membre = membreImpl.findMembreByPseudo(pseudoTFD.getText());
            if (membre == null) {
                Notification.NotifError("Error", "Vous n'etes pas dans la base de donnees");
            }
            else if (Utils.checkPassword(passwordTFD.getText(), membre.getPassword())) {
                Notification.NotifSuccess("Succes", "Vous vous etes bien connecte");
                System.out.println();
                try {
                    Outils.load(event, "WhatsApp Group", "views/main.fxml");
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    Notification.NotifError("Error", "Erreur de connexion: ");
                }

            }else {
                Notification.NotifError("Erreur", "Mot de passe incorrect");
            }
        }else {
            Notification.NotifError("Erreur", "Tous les champs sont obligatoires");
        }


    }

    @FXML
    void register(ActionEvent event) throws IOException {

        if(!pseudoTFD.getText().trim().isEmpty() && !passwordInsTFD.getText().trim().isEmpty()) {
            String pseudo = pseudoTFD.getText();
            String passwordhash = Utils.hashPassword(passwordInsTFD.getText());

            Membre membre = membreImpl.createMembre(pseudo, passwordhash);

            if (membre != null) {
                Notification.NotifSuccess("Succes", "Votre compte a bien ete creer");
                Outils.load(event, "Login", "views/login.fxml");
            }else {
                Notification.NotifError("Erreur", "Erreur de d'inscription");
            }
        }else {
            Notification.NotifError("Erreur", "Tous les champs sont obligatoires");
        }
    }

    @FXML
    void menu(ActionEvent event) throws IOException {
        Outils.load(event, "Menu", "menu.fxml");
    }
    @FXML
    void update(ActionEvent event) throws IOException {
        if (!pseudoUpTFD.getText().trim().isEmpty() && !passwordUpTFD.getText().trim().isEmpty()){

            if (membreImpl.findMembreByPseudo(pseudoUpTFD.getText()) == null || pseudoUpTFD.getText().trim().equalsIgnoreCase(membre.getPseudo())) {
                Membre membre1 = new Membre();
                membre1.setId(membre.getId());
                membre1.setPseudo(pseudoUpTFD.getText());
                String pw = Utils.hashPassword(passwordUpTFD.getText());
                membre1.setPassword(pw);
                membreImpl.updateMembre(membre1);
                Notification.NotifSuccess("Succes", "Vos informations ont bien ete modifier");
                menu(event);
            }else Notification.NotifError("Erreur", "Ce Pseudo existe deja !");
        }else Notification.NotifError("Erreur", "Tous les champs sont obligatoires!");
    }

    @FXML
    void delete(ActionEvent event){

        if (membre != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de Suppression");
            alert.setHeaderText("Voulez-vous vraiment supprimer ce compte?");
            alert.setContentText("Cette action est irreversible !");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    boolean isDelete = membreImpl.deleteMembre(membre);
                    if (isDelete){
                        Notification.NotifSuccess("Succes", "Compte supprime");
                        menu(event);
                    }else Notification.NotifError("Erreur", "Erreur de suppression");

                }catch (Exception e){
                    Notification.NotifError("Erreur", "Erreur de suppression");
                }
            }else return;


        }else Notification.NotifError("Erreur", "Erreur de suppression");

    }

    @FXML
    void sexcuser(ActionEvent event) throws IOException {
        if (membre != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation d'excuse");
            alert.setHeaderText("Vos excuses sont-ils vraiment sinceres?");
            alert.setContentText("En vous excusant, vous promettez de ne plus recommencer !");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                membre.setBanned(false);
                membreImpl.updateMembre(membre);
                Notification.NotifSuccess("Succes", "Vos excuses sont acceptes");
                excusePane.setVisible(false);
                menu(event);
            }else Notification.NotifError("Erreur", "Vous etes toujours bani");

        }
    }
}
