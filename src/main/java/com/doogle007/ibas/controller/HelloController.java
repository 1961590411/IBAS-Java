package com.doogle007.ibas.controller;

import com.doogle007.ibas.MainApplication;
import com.doogle007.ibas.network.Connect;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloController {
    public static int index = -1;

    private double offsetX, offsetY;

    @FXML
    private StackPane buttonClose;
    @FXML
    private HBox buttonDevice;
    @FXML
    private HBox buttonMessage;
    @FXML
    private HBox buttonServer;
    @FXML
    private HBox buttonSetting;
    @FXML
    private HBox buttonAbout;
    @FXML
    private Pane innerPane;
    @FXML
    private HBox root;
    @FXML
    private Label labelConnectState;

    @FXML
    private void initialize() {
        labelConnectState.setText(Connect.isConnected() ? "云服务器已连接" : "!!连接失败，请在设置中检查服务器配置!!");
        root.setOnMousePressed(e -> {
            offsetX = e.getSceneX();
            offsetY = e.getSceneY();
        });
        root.setOnMouseDragged(e -> {
            Stage stage = (Stage) buttonClose.getScene().getWindow();
            stage.setX(e.getScreenX() - offsetX);
            stage.setY(e.getScreenY() - offsetY);
        });
    }

    @FXML
    protected void onButtonCloseEnter() {
        for(Node node : buttonClose.getChildren())
            if(node instanceof Shape)
                ((Shape) node).setStroke(Paint.valueOf("ffffff"));
        buttonClose.setBackground(new Background(new BackgroundFill(Paint.valueOf("ff5f5f"),null,null)));
    }

    @FXML
    protected void onButtonCloseExit() {
        for(Node node : buttonClose.getChildren())
            if(node instanceof Shape)
                ((Shape) node).setStroke(Paint.valueOf("9f9f9f"));
        buttonClose.setBackground(Background.EMPTY);
    }

    @FXML
    protected void onButtonCloseClick() {
        Stage stage = (Stage) buttonClose.getScene().getWindow();
        try {
            Connect.end();
        } catch (Exception ignored) {
        }
        stage.close();
    }

    @FXML
    protected void onButtonDeviceClick() throws IOException {
        if (index != 0) {
            buttonDevice.setStyle("-fx-background-color: #bbbbbb;");
            buttonMessage.setStyle("-fx-background-color: #dddddd;");
            buttonServer.setStyle("-fx-background-color: #dddddd;");
            buttonSetting.setStyle("-fx-background-color: #dddddd;");
            buttonAbout.setStyle("-fx-background-color: #dddddd;");

            index = 0;
            FXMLLoader fxmlAbout = new FXMLLoader(MainApplication.class.getResource("deviceList.fxml"));
            innerPane.getChildren().clear();
            innerPane.getChildren().add(fxmlAbout.load());
        }
    }

    @FXML
    protected void onButtonMessageClick() throws IOException {
        if (index != 1) {
            buttonDevice.setStyle("-fx-background-color: #dddddd;");
            buttonMessage.setStyle("-fx-background-color: #bbbbbb;");
            buttonServer.setStyle("-fx-background-color: #dddddd;");
            buttonSetting.setStyle("-fx-background-color: #dddddd;");
            buttonAbout.setStyle("-fx-background-color: #dddddd;");
            index = 1;
            FXMLLoader fxmlAbout = new FXMLLoader(MainApplication.class.getResource("message.fxml"));
            innerPane.getChildren().clear();
            innerPane.getChildren().add(fxmlAbout.load());
        }
    }

    @FXML
    protected void onButtonServerClick() throws IOException {
        if (index != 2) {
            buttonDevice.setStyle("-fx-background-color: #dddddd;");
            buttonMessage.setStyle("-fx-background-color: #dddddd;");
            buttonServer.setStyle("-fx-background-color: #bbbbbb;");
            buttonSetting.setStyle("-fx-background-color: #dddddd;");
            buttonAbout.setStyle("-fx-background-color: #dddddd;");

            index = 2;

            FXMLLoader fxmlAbout = new FXMLLoader(MainApplication.class.getResource("server.fxml"));
            innerPane.getChildren().clear();
            innerPane.getChildren().add(fxmlAbout.load());
        }
    }

    @FXML
    protected void onButtonSettingClick() throws IOException {
        if (index != 3) {
            buttonDevice.setStyle("-fx-background-color: #dddddd;");
            buttonMessage.setStyle("-fx-background-color: #dddddd;");
            buttonServer.setStyle("-fx-background-color: #dddddd;");
            buttonSetting.setStyle("-fx-background-color: #bbbbbb;");
            buttonAbout.setStyle("-fx-background-color: #dddddd;");

            index = 3;

            FXMLLoader fxmlAbout = new FXMLLoader(MainApplication.class.getResource("setting.fxml"));
            innerPane.getChildren().clear();
            innerPane.getChildren().add(fxmlAbout.load());
        }
    }

    @FXML
    protected void onButtonAboutClick() throws IOException {
        if (index != 4) {
            buttonDevice.setStyle("-fx-background-color: #dddddd;");
            buttonMessage.setStyle("-fx-background-color: #dddddd;");
            buttonServer.setStyle("-fx-background-color: #dddddd;");
            buttonSetting.setStyle("-fx-background-color: #dddddd;");
            buttonAbout.setStyle("-fx-background-color: #bbbbbb;");

            index = 4;

            FXMLLoader fxmlAbout = new FXMLLoader(MainApplication.class.getResource("aboutInfo.fxml"));
            innerPane.getChildren().clear();
            innerPane.getChildren().add(fxmlAbout.load());
        }
    }
}