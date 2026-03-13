module com.doogle007.ibas {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.naming;
    requires org.json;
    requires java.sql;
    requires java.desktop;
    requires org.eclipse.paho.client.mqttv3;
    requires javafx.graphics;

    opens com.doogle007.ibas to javafx.fxml;
    exports com.doogle007.ibas;
    exports com.doogle007.ibas.controller;
    opens com.doogle007.ibas.controller to javafx.fxml;
}