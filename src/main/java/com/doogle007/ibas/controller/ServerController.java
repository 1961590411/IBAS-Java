package com.doogle007.ibas.controller;

import com.doogle007.ibas.Options;
import com.doogle007.ibas.device.DeviceGroup;
import com.doogle007.ibas.network.Connect;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ServerController {
    @FXML
    private Label labelState;
    @FXML
    private void initialize() {
        String state = "-服务器-\n"
                + "\t状态："
                + (Connect.isConnected() ? "已连接" : "未连接") + "\n"
                + "\t地址：" + Options.localAddress + "\n"
                + "\t端口：" + Options.localPort + "\n"
                + "\n-设备-\n"
                + "\t总数：" + deviceCount() + "\n";
        labelState.setText(state);
    }
    private int deviceCount(){
        int count = 0;
        for(DeviceGroup group : DeviceGroup.DeviceGroupList)
            count += group.deviceList.size();
        return count;
    }
}
