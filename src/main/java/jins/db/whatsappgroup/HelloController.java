package jins.db.whatsappgroup;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import jins.db.whatsappgroup.tools.Outils;


import java.io.IOException;

public class HelloController {

    @FXML
    void login(ActionEvent event) throws IOException {
        Outils.load(event, "Seconnecter", "views/login.fxml");

    }

    @FXML
    void register(ActionEvent event) throws IOException {
        Outils.load(event, "Seconnecter", "views/register.fxml");
    }

}