package com.doogle007.ibas.controller;

import com.doogle007.ibas.device.DeviceGroup;
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

public class GroupController {
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
    protected void onButtonAddClick() {
        String name = textField.getText();
        DeviceGroup deviceGroup = new DeviceGroup(name);
        if (!DeviceGroup.addGroup(deviceGroup))
            labelFail.setText("该设备组已存在");
        else
            onButtonCloseClick();
    }

    @FXML
    protected void onButtonEditClick() {
        String name = textField.getText();
        if (DeviceGroup.searchGroupIndex(name) >= 0)
            labelFail.setText("该设备组已存在");
        else if (name.isEmpty())
            labelFail.setText("设备组名称不可为空");
        else {
            DeviceController.currentGroup.setName(name);
            onButtonCloseClick();
        }
    }

    @FXML
    protected void onButtonDeleteClick() {
        boolean check = checkBoxDelete.isSelected();
        if (DeviceController.currentGroup.equals(DeviceGroup.searchGroup(null)))
            labelFail.setText("未归类设备组不可以删除");
        else {
            DeviceGroup.removeGroup(DeviceController.currentGroup.getName(), check);
            DeviceController.currentGroup = DeviceGroup.searchGroup(null);
            onButtonCloseClick();
        }
    }
}