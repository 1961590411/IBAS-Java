package com.doogle007.ibas.controller;

import com.doogle007.ibas.device.Device;
import com.doogle007.ibas.device.DeviceGroup;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class DeviceController {

    public static DeviceController instance;
    public boolean active = false;
    public Timeline timelineUpdate;

    @FXML
    private VBox vBoxDeviceList;

    @FXML
    private void initialize() {
        if (instance != null) {
            instance.timelineUpdate.stop();
        }
        instance = this;
        refresh();
        update();
        timelineUpdate = new Timeline(new KeyFrame(Duration.seconds(5), event -> update()));
        timelineUpdate.setCycleCount(Timeline.INDEFINITE);
        timelineUpdate.play();
    }

    public static void refresh() {
        instance.active = true;
    }

    private void update(){
        if(instance.active){
            updateDeviceList();
            active = false;
        }
    }

    private void updateDeviceList(){
        vBoxDeviceList.getChildren().clear();
        for(Device device : DeviceGroup.defaultGroup.deviceList)
            vBoxDeviceList.getChildren().add(createHBoxDevice(device));
        for(DeviceGroup deviceGroup : DeviceGroup.DeviceGroupList)
            for(Device device : deviceGroup.deviceList)
                vBoxDeviceList.getChildren().add(createHBoxDevice(device));
    }

    private HBox createHBoxDevice(Device device){
        HBox hBoxDevice = new HBox();
        hBoxDevice.setSpacing(10);
        hBoxDevice.setPrefWidth(700);
        hBoxDevice.setPrefHeight(50);
        hBoxDevice.setAlignment(Pos.CENTER_LEFT);

        Label labelDevice = new Label(device.name);

        hBoxDevice.getChildren().add(labelDevice);
        
        return hBoxDevice;
    }
}
