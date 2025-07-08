module jins.db.whatsappgroup {
    requires javafx.controls;
    requires javafx.fxml;


    requires java.persistence;
    requires static lombok;
    requires TrayNotification;
    requires jbcrypt;
    requires java.sql;            // ← nécessaire ici
    requires org.hibernate.orm.core;
    requires emoji.java;
    requires java.desktop;


    opens jins.db.whatsappgroup to javafx.fxml;
    opens jins.db.whatsappgroup.models to org.hibernate.orm.core;
    opens jins.db.whatsappgroup.tools;
    opens jins.db.whatsappgroup.controllers to javafx.fxml;
    exports jins.db.whatsappgroup;
}