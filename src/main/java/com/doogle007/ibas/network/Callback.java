package com.doogle007.ibas.network;

import com.doogle007.ibas.Logger;
import com.doogle007.ibas.controller.MonitorController;
import com.doogle007.ibas.device.DeviceGroup;
import com.doogle007.ibas.device.DeviceIO;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import com.doogle007.ibas.device.Device;
import org.json.JSONException;
import org.json.JSONObject;

public class Callback implements MqttCallback {
    public void connectionLost(Throwable cause) {
        Logger.warn("已失去连接");
        Logger.warn(cause.toString());
        System.out.println("已失去连接");
    }

    public void messageArrived(String topic, MqttMessage message){
        // subscribe后得到的消息会执行到这里面
        Logger.info("接收消息主题:" + topic);
        Logger.info("接收消息Qos:" + message.getQos());
        Logger.info("接收消息内容:" + message);
        System.out.println("正在处理该主题");
        processTopic(topic, message.toString());
        System.out.println("Message Arrived");

        // 使用 Platform.runLater 确保在 JavaFX 主线程刷新 UI
        javafx.application.Platform.runLater(() -> {
            if (MonitorController.instance != null) {
                MonitorController.refresh();
            }
        });
    }

    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("deliveryComplete---------" + token.isComplete());
    }

    private void processTopic(String topic, String message) {
        if(topic.contains("IBAS/system/device/hello/")) {
            System.out.println("正在处理Hello主题");
            processHelloMessage(message);
        }
        else if (topic.contains("IBAS/system/device/group/")) {
            System.out.println("正在处理Group主题");
            String groupName = topic.replace("IBAS/system/device/group/", "");
            processGroupMessage(message, groupName);
        }
        else
            System.out.println("无法处理该主题");
    }

    private static void processGroupMessage(String message, String groupName) {
        try {
            //将String转换为JSONObject实例
            JSONObject jsonObject = new JSONObject(message);
            JSONObject params = jsonObject.getJSONObject("params");

            //无法处理没有ClientID的信息，直接返回
            if(params.isNull("ClientID")) return;

            //搜寻该设备组，若不存在则返回
            int index = DeviceGroup.searchGroupIndex(groupName);
            if(index < 0) return;

            //获取设备组，用ID寻找匹配的设备
            DeviceGroup group = DeviceGroup.DeviceGroupList.get(index);

            //搜寻该设备，若不存在则返回
            int index2 = group.searchDeviceIndex(params.getString("ClientID"));
            if(index2 < 0) return;

            //获取设备，并覆盖警报数据
            Device device = group.deviceList.get(index2);
            if(!params.isNull("AlarmManual"))
                device.switchManual = params.getBoolean("AlarmManual");
            if(!params.isNull("AlarmAuto"))
                device.switchAuto = params.getBoolean("AlarmAuto");

            //完成后覆盖List中的源设备实例
            group.deviceList.set(index, device);
            DeviceIO.writeDevice(device);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static void processHelloMessage(String message) {
        try {
            //将String转换为JSONObject实例
            JSONObject jsonObject = new JSONObject(message);
            JSONObject params = jsonObject.getJSONObject("params");

            //将JSONObject转换为Device实例
            Device device = Device.jsonToDevice(params);

            //只有在解析出错时，设备实例为空，方法直接返回
            if (device == null) {
                Logger.error("不符合格式的JSON，提前结束");
                return;
            }
            Logger.info("解析的设备名称: " + device.name);

            DeviceGroup deviceGroup;
            //判断设备是否隶属于某设备组
            //  已隶属：判断设备组是否登记
            //      已登记：绑定该设备组
            //      未登记：创建新设备组并登记，绑定该设备组
            //  未隶属：绑定至未归类设备组
            //更新绑定的设备组
            if (device.group != null){
                int index = DeviceGroup.searchGroupIndex(device.group);
                if (index < 0) {
                    deviceGroup = new DeviceGroup(device.group);
                    DeviceGroup.addGroup(deviceGroup);
                } else
                    deviceGroup = DeviceGroup.DeviceGroupList.get(index);
            } else
                deviceGroup = DeviceGroup.defaultGroup;
            deviceGroup.updateDevice(device);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
