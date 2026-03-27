package com.doogle007.ibas.controller;

import com.doogle007.ibas.device.DeviceGroup;
import com.doogle007.ibas.device.DeviceIO;
import com.doogle007.ibas.network.Connect;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import org.eclipse.paho.client.mqttv3.MqttException;

public class SubscribeController {
    private double offsetX, offsetY;
    @FXML
    Label labelFail;
    @FXML
    Label labelNotice;
    @FXML
    private StackPane buttonClose;
    @FXML
    private CheckBox checkBoxDelete;
    @FXML
    private Button buttonDelete;
    @FXML
    private Button buttonAdd;
    @FXML
    private Button buttonEdit;
    @FXML
    private TextField textField;
    @FXML
    private HBox root;

    @FXML
    private void initialize() {
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
        stage.close();
    }

    @FXML
    protected void onButtonAddClick() throws MqttException {
        //获取设备组名称，并搜寻序列号
        String name = textField.getText();
        int index = DeviceGroup.searchGroupIndex(name);

        //若序列号无效，则新建设备组并保存，结束
        //若序列号有效，则获取设备组
        //  若设备组已订阅，提示用户，不执行其他操作
        //  若设备组未订阅，订阅并保存，结束
        if(index < 0) {
            DeviceGroup deviceGroup = new DeviceGroup(name);
            deviceGroup.subscribe = true;
            DeviceGroup.addGroup(deviceGroup);
            Connect.groupSubTopicAdd(name);
            onButtonCloseClick();
        } else {
            DeviceGroup deviceGroup = DeviceGroup.DeviceGroupList.get(index);
            if(deviceGroup.subscribe)
                labelFail.setText("该设备组已订阅");
            else {
                deviceGroup.subscribe = true;
                DeviceIO.writeGroupConfig(deviceGroup);
                Connect.groupSubTopicAdd(name);
                onButtonCloseClick();
            }
        }
    }

    @FXML
    protected void onButtonEditClick() {
        String name = textField.getText();
        if (DeviceGroup.searchGroupIndex(name) >= 0)
            labelFail.setText("该设备组已存在");
        else if (name.isEmpty())
            labelFail.setText("设备组名称不可为空");
        else {
            MonitorController.currentGroup.setName(name);
            onButtonCloseClick();
        }
    }

    @FXML
    protected void onButtonDeleteClick() throws MqttException {
        //取消订阅设备组
        if(!MonitorController.currentGroup.subscribe) {
            labelFail.setText("该设备组未被订阅！");
            return;
        }
        MonitorController.currentGroup.subscribe = false;
        DeviceIO.writeGroupConfig(MonitorController.currentGroup);
        Connect.groupSubTopicDel(MonitorController.currentGroup.getName());
        onButtonCloseClick();



        /*
        //TODO 安全删除注释代码，记得删除checkbox
        boolean check = checkBoxDelete.isSelected();
        if (MonitorController.currentGroup.equals(DeviceGroup.searchGroup(null)))
            labelFail.setText("未归类设备组不可以删除");
        else {
            DeviceGroup.removeGroup(MonitorController.currentGroup.getName(), check);
            MonitorController.currentGroup = DeviceGroup.searchGroup(null);
            onButtonCloseClick();
        }
         */
    }
}