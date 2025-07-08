module jins.db.whatsappgroup {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens jins.db.whatsappgroup to javafx.fxml;
    exports jins.db.whatsappgroup;
}