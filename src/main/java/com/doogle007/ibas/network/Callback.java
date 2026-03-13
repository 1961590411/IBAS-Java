package com.doogle007.ibas.network;

import com.doogle007.ibas.Logger;
import com.doogle007.ibas.controller.DeviceController;
import com.doogle007.ibas.device.DeviceIO;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import com.doogle007.ibas.device.Device;
import org.json.JSONObject;

public class Callback implements MqttCallback {
    private static void processMessage(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            JSONObject params = jsonObject.getJSONObject("params");
            Device device = Device.jsonToDevice(params.toString());
            if (device != null) {
                Device.AddDevice(device);
                DeviceIO.writeDevice(device);
            }
        } catch (Exception ignored) {
        }
    }

    public void connectionLost(Throwable cause) {
        Logger.warn("已失去连接");
        System.out.println("已失去连接");
    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // subscribe后得到的消息会执行到这里面
        Logger.info("接收消息主题:" + topic);
        Logger.info("接收消息Qos:" + message.getQos());
        Logger.info("接收消息内容:" + message);
        processMessage(message.toString());
        System.out.println("Message Arrived");
        try {
            if (DeviceController.instance != null)
                DeviceController.refresh();
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
    }

    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("deliveryComplete---------" + token.isComplete());
    }
}
