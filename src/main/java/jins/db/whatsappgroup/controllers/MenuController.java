package jins.db.whatsappgroup.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import jins.db.whatsappgroup.tools.Outils;


import java.io.IOException;

public class MenuController {

    @FXML
    void login(ActionEvent event) throws IOException {
        Outils.load(event, "Seconnecter", "views/login.fxml");

    }

    @FXML
    void register(ActionEvent event) throws IOException {
        Outils.load(event, "Seconnecter", "views/register.fxml");
    }

}