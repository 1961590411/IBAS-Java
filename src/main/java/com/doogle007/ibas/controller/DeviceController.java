package com.doogle007.ibas.controller;

import com.doogle007.ibas.MainApplication;
import com.doogle007.ibas.device.Device;
import com.doogle007.ibas.device.DeviceGroup;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Objects;

public class DeviceController {
    public static DeviceController instance;
    public static DeviceGroup currentGroup;
    public static SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd-HH:mm");
    private final Image imageManualAlarm = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/doogle007/ibas/icons/manualAlarm.png")));
    private final Image imageAutoAlarm = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/doogle007/ibas/icons/autoAlarm.png")));
    private final Image imageOffline = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/doogle007/ibas/icons/offline.png")));
    private final Image imageSafe = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/doogle007/ibas/icons/safe.png")));
    private final Image imageOnline = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/doogle007/ibas/icons/online.png")));
    public Timeline timelineDevice;
    private boolean active = false;
    @FXML
    private VBox vBoxDevice;
    @FXML
    private Label connectingState;
    @FXML
    private HBox deviceName;
    @FXML
    private HBox logicalAddress;
    @FXML
    private HBox alarmState;
    @FXML
    private HBox postTime;
    @FXML
    private HBox loginState;
    @FXML
    private StackPane buttonEdit;
    @FXML
    private StackPane buttonAdd;
    @FXML
    private StackPane buttonDelete;
    @FXML
    private ChoiceBox<String> choiceBoxGroups;

    public static void refresh() {
        instance.active = true;
    }

    @FXML
    private void initialize() {
        if (currentGroup == null)
            currentGroup = DeviceGroup.searchGroup(null);
        if (instance != null) {
            instance.timelineDevice.stop();
        }
        instance = this;
        refresh();
        update();
        timelineDevice = new Timeline(new KeyFrame(Duration.seconds(5), event -> update()));
        timelineDevice.setCycleCount(Timeline.INDEFINITE);
        timelineDevice.play();
        for (DeviceGroup group : DeviceGroup.DeviceGroupList)
            choiceBoxGroups.getItems().add(group.getName());
        choiceBoxGroups.getSelectionModel().select(currentGroup.getName());
        buttonEdit.setDisable(true);
        buttonDelete.setDisable(true);
        buttonEdit.setOpacity(0.5);
        buttonDelete.setOpacity(0.5);
        choiceBoxGroups.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            currentGroup = DeviceGroup.searchGroup(newValue);
            boolean active = currentGroup.getName().equals(DeviceGroup.searchGroup(null).getName());
            buttonEdit.setDisable(active);
            buttonDelete.setDisable(active);
            buttonEdit.setOpacity(active ? 0.5 : 1);
            buttonDelete.setOpacity(active ? 0.5 : 1);
            updateList();
        });
    }

    private void update() {
        if (instance.active) {
            connectingState.setText("收到新的消息了!");
        }
        if (Device.allDisconnectCheck()) {
            refresh();
            connectingState.setText("设备长时间未反应!");
        } else {
            connectingState.setText("还没收到新的消息哦!");
        }
        if (instance.active) {
            updateList();
            instance.active = false;
        }
    }

    protected void updateList() {
        vBoxDevice.getChildren().clear();
        //插入排序，红色在上，绿色居中，灰色靠下
        for (int index = 0; index < currentGroup.deviceList.size(); index++) {
            currentGroup.deviceList.sort(Comparator.naturalOrder());
        }
        for (Device device : currentGroup.deviceList) {
            HBox hBoxDevice = new HBox();
            hBoxDevice.setPrefHeight(50);
            hBoxDevice.setMinHeight(hBoxDevice.getHeight());
            hBoxDevice.setAlignment(Pos.CENTER_LEFT);
            Label deviceName = new Label(device.device_name);
            Label logicalAddress = new Label(device.logical_address);
            HBox alarmState = new HBox();

            ImageView manualAlarm = new ImageView(imageManualAlarm);
            ImageView autoAlarm = new ImageView(imageAutoAlarm);
            ImageView offline = new ImageView(imageOffline);
            ImageView online = new ImageView(imageOnline);
            ImageView safe = new ImageView(imageSafe);

            manualAlarm.setFitHeight(32);
            autoAlarm.setFitHeight(32);
            offline.setFitHeight(32);
            online.setFitHeight(32);
            safe.setFitHeight(32);
            manualAlarm.setFitWidth(32);
            autoAlarm.setFitWidth(32);
            offline.setFitWidth(32);
            online.setFitWidth(32);
            safe.setFitWidth(32);
            manualAlarm.setPreserveRatio(true);
            autoAlarm.setPreserveRatio(true);
            offline.setPreserveRatio(true);
            online.setPreserveRatio(true);
            safe.setPreserveRatio(true);

            if (device.switch_manual)
                alarmState.getChildren().add(manualAlarm);
            if (device.switch_auto)
                alarmState.getChildren().add(autoAlarm);
            if (!device.switch_manual && !device.switch_auto && device.online)
                alarmState.getChildren().add(safe);

            Label postTime = new Label(timeAgo(device.timestamp));

            HBox loginState = new HBox();
            loginState.getChildren().add(device.online ? online : offline);
            loginState.setAlignment(Pos.CENTER);

            if (!device.online)
                hBoxDevice.setStyle("-fx-border-color: #5F5F5F; -fx-border-width: 2; -fx-background-color: #F8F8F8; -fx-background-radius: 10; -fx-border-radius: 10");
            else if (device.switch_manual || device.switch_auto)
                hBoxDevice.setStyle("-fx-border-color: #FF5F5F; -fx-border-width: 2; -fx-background-color: #F8F8F8; -fx-background-radius: 10; -fx-border-radius: 10");
            else
                hBoxDevice.setStyle("-fx-border-color: #5FFF5F; -fx-border-width: 2; -fx-background-color: #F8F8F8; -fx-background-radius: 10; -fx-border-radius: 10");

            deviceName.prefWidthProperty().bind(this.deviceName.widthProperty().add(6));
            deviceName.setPadding(new Insets(5, 5, 5, 5));
            logicalAddress.prefWidthProperty().bind(this.logicalAddress.widthProperty().add(6));
            logicalAddress.setPadding(new Insets(5, 5, 5, 5));
            alarmState.prefWidthProperty().bind(this.alarmState.widthProperty().add(6));
            alarmState.setPadding(new Insets(5, 5, 5, 5));
            alarmState.setSpacing(5);
            alarmState.setAlignment(Pos.CENTER);
            postTime.prefWidthProperty().bind(this.postTime.widthProperty().add(6));
            postTime.setPadding(new Insets(5, 5, 5, 5));
            postTime.setAlignment(Pos.CENTER);
            loginState.prefWidthProperty().bind(this.loginState.widthProperty());
            loginState.setPadding(new Insets(5, 5, 5, 5));

            hBoxDevice.getChildren().addAll(deviceName, logicalAddress, alarmState, postTime, loginState);

            hBoxDevice.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
                if (!device.online)
                    hBoxDevice.setStyle("-fx-border-color: #5F5F5F; -fx-border-width: 2; -fx-background-color: #E8E8E8; -fx-background-radius: 10; -fx-border-radius: 10");
                else if (device.switch_manual || device.switch_auto)
                    hBoxDevice.setStyle("-fx-border-color: #FF5F5F; -fx-border-width: 2; -fx-background-color: #E8E8E8; -fx-background-radius: 10; -fx-border-radius: 10");
                else
                    hBoxDevice.setStyle("-fx-border-color: #5FFF5F; -fx-border-width: 2; -fx-background-color: #E8E8E8; -fx-background-radius: 10; -fx-border-radius: 10");
            });

            hBoxDevice.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
                if (!device.online)
                    hBoxDevice.setStyle("-fx-border-color: #5F5F5F; -fx-border-width: 2; -fx-background-color: #F8F8F8; -fx-background-radius: 10; -fx-border-radius: 10");
                else if (device.switch_manual || device.switch_auto)
                    hBoxDevice.setStyle("-fx-border-color: #FF5F5F; -fx-border-width: 2; -fx-background-color: #F8F8F8; -fx-background-radius: 10; -fx-border-radius: 10");
                else
                    hBoxDevice.setStyle("-fx-border-color: #5FFF5F; -fx-border-width: 2; -fx-background-color: #F8F8F8; -fx-background-radius: 10; -fx-border-radius: 10");
            });

            ContextMenu contextMenu = new ContextMenu();
            Menu menuMove = new Menu("移动设备至...");
            for (DeviceGroup group : DeviceGroup.DeviceGroupList) {
                MenuItem menuItem = new MenuItem(group.getName());
                menuItem.setOnAction(event -> {
                    currentGroup.moveDevice(device, group);
                    updateList();
                });
                menuMove.getItems().add(menuItem);
            }

            MenuItem itemDelete = new MenuItem("删除此设备");
            itemDelete.setOnAction(event -> {
                device.delete(currentGroup);
                updateList();
            });
            contextMenu.getItems().addAll(menuMove, itemDelete);

            hBoxDevice.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if (event.getButton() == MouseButton.SECONDARY)
                    contextMenu.show(hBoxDevice, event.getScreenX(), event.getScreenY());
            });

            vBoxDevice.getChildren().add(hBoxDevice);
        }
    }

    private String timeAgo(long timestamp) {
        long delta = System.currentTimeMillis() - timestamp;
        String timeAgo;
        if(delta < 60000)
            timeAgo = "刚才";
        else if (delta < 3600000)
            timeAgo = delta / 60000 + " 分钟前";
        else if (delta < 86400000)
            timeAgo = delta / 3600000 + " 小时前";
        else
            timeAgo = delta / 86400000 + " 天前";

        return timeAgo;
    }

    @FXML
    protected void onButtonEditClick() throws IOException {
        Stage stage = new Stage();
        Stage stageParent = (Stage) buttonEdit.getScene().getWindow();
        stage.initOwner(stageParent);
        stage.initModality(Modality.WINDOW_MODAL);
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("groupEdit.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 340, 240);
        scene.setFill(Color.TRANSPARENT);
        stage.setTitle("编辑设备组");
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.showAndWait();
        updateChoiceBox();
    }

    @FXML
    protected void onButtonAddClick() throws IOException {
        Stage stage = new Stage();
        Stage stageParent = (Stage) buttonAdd.getScene().getWindow();
        stage.initOwner(stageParent);
        stage.initModality(Modality.WINDOW_MODAL);
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("groupAdd.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 340, 240);
        scene.setFill(Color.TRANSPARENT);
        stage.setTitle("添加设备组");
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.showAndWait();
        updateChoiceBox();
    }

    @FXML
    protected void onButtonDeleteClick() throws IOException {
        Stage stage = new Stage();
        Stage stageParent = (Stage) buttonDelete.getScene().getWindow();
        stage.initOwner(stageParent);
        stage.initModality(Modality.WINDOW_MODAL);
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("groupDelete.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 340, 240);
        scene.setFill(Color.TRANSPARENT);
        stage.setTitle("删除设备组");
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.showAndWait();
        updateChoiceBox();
    }

    @FXML
    protected void onButtonAddEnter(){
        onButtonEnter(buttonAdd);
    }
    @FXML
    protected void onButtonEditEnter(){
        onButtonEnter(buttonEdit);
    }
    @FXML
    protected void onButtonDeleteEnter(){
        onButtonEnter(buttonDelete);
    }
    @FXML
    protected void onButtonAddExit(){
        onButtonExit(buttonAdd);
    }
    @FXML
    protected void onButtonEditExit(){
        onButtonExit(buttonEdit);
    }
    @FXML
    protected void onButtonDeleteExit(){
        onButtonExit(buttonDelete);
    }

    private void onButtonEnter(StackPane button){
        button.setStyle(" -fx-background-color: #dfdfdf; -fx-background-radius: 8;");
    }
    private void onButtonExit(StackPane button){
        button.setStyle("");
    }
    private void updateChoiceBox() {
        String name = currentGroup.getName();
        choiceBoxGroups.getItems().clear();
        for (DeviceGroup group : DeviceGroup.DeviceGroupList)
            choiceBoxGroups.getItems().add(group.getName());
        currentGroup = DeviceGroup.searchGroup(name);
        choiceBoxGroups.getSelectionModel().select(name);
    }
}