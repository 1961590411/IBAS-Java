package com.doogle007.ibas.controller;

import com.doogle007.ibas.Options;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class SettingController {
    private boolean newAddress = false;
    private boolean newPort = false;
    @FXML
    private TextField textFieldAddress;
    @FXML
    private TextField textFieldPort;
    @FXML
    private Button buttonSave;
    @FXML
    private Label labelTip;
    @FXML
    private void initialize() {
        buttonSave.setDisable(true);
        textFieldAddress.setText(Options.localAddress);
        textFieldPort.setText(Options.localPort);
        textFieldAddress.textProperty().addListener((observable, oldValue, newValue) -> {
            newAddress = !Options.localAddress.equals(newValue);
            buttonSave.setDisable(!(newAddress || newPort));
        });
        textFieldPort.textProperty().addListener((observable, oldValue, newValue) -> {
            newPort = !Options.localPort.equals(newValue);
            buttonSave.setDisable(!(newAddress || newPort));
        });
    }

    @FXML
    protected void onButtonSave(MouseEvent mouseEvent) {
        Options options = Options.getInstance();
        options.set("ServerAddress", textFieldAddress.getText());
        options.set("ServerPort", textFieldPort.getText());
        options.save();
        labelTip.setText("保存成功，重启软件以应用更改。");
    }
}
