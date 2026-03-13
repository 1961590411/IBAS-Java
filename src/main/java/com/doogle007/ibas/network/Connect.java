package com.doogle007.ibas.network;

import com.doogle007.ibas.Options;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class Connect {
    private static MqttClient client;
    public static void start() {
        String subTopic = "TestClient";
        String content = "{\n\"msg\": \"Hello World\"\n}";
        String broker;
        String clientId = "TestUser";
        String address = Options.getInstance().get("ServerAddress", "120.26.133.159");
        String port = Options.getInstance().get("ServerPort", "1883");
        broker = "tcp://" + address + ":" + port;

        MemoryPersistence persistence = new MemoryPersistence();

        try {
            client = new MqttClient(broker, clientId, persistence);

            // MQTT 连接选项
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName("emqx_test");
            connOpts.setPassword("00000000".toCharArray());
            // 保留会话
            connOpts.setCleanSession(true);

            // 设置回调
            client.setCallback(new Callback());

            // 建立连接
            System.out.println("Connecting to broker: " + broker);
            client.connect(connOpts);

            System.out.println("Connected");
            System.out.println("Publishing message: " + content);

            // 订阅
            client.subscribe(subTopic);

        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    }

    public static void end() throws MqttException {
        client.disconnect();
        System.out.println("Disconnected");
        client.close();
        System.exit(0);
    }

    public static boolean isConnected() {
        return client.isConnected();
    }

}
