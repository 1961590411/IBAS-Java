package com.doogle007.ibas.network;

import com.doogle007.ibas.Logger;
import com.doogle007.ibas.Options;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.List;

public class Connect {
    private static MqttClient client;
    private static final String helloSubTopic = "IBAS/system/device/hello/#";
    private static final List<String> groupSubTopicList = new ArrayList<>();
    public static void start() {
        //TODO: Improve me: 使用不重复的字符串作为clientID
        String clientId = "TestUser";
        String address = Options.getInstance().get("ServerAddress", "120.26.133.159");
        String port = Options.getInstance().get("ServerPort", "1883");
        String broker = "tcp://" + address + ":" + port;

        MemoryPersistence persistence = new MemoryPersistence();

        try {
            client = new MqttClient(broker, clientId, persistence);

            // MQTT 连接选项
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName("EMQX_test");
            connOpts.setPassword("IBAS".toCharArray());
            // 保留会话
            connOpts.setCleanSession(true);

            // 设置回调
            client.setCallback(new Callback());

            // 建立连接
            System.out.println("Connecting to broker: " + broker);
            client.connect(connOpts);

            System.out.println("Connected");

            // 订阅
            client.subscribe(helloSubTopic);

        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("exception " + me);
            Logger.error(me.toString());
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

    public static void groupSubTopicAdd(String groupName) throws MqttException {
        if(searchGroupIndex(groupName) != -1)
            return;
        groupSubTopicList.add(groupName);
        client.subscribe("IBAS/system/device/group/"+groupName);
        Logger.info("订阅新的设备组: " + groupName);
    }
    public static void groupSubTopicDel(String groupName) throws MqttException {
        if(searchGroupIndex(groupName) == -1)
            return;
        groupSubTopicList.remove(groupName);
        client.unsubscribe("IBAS/system/device/group/"+groupName);
        Logger.info("取消订阅设备组: " + groupName);
    }

    private static int searchGroupIndex(String groupName) {
        for (int i = 0; i < groupSubTopicList.size(); i++)
            if (groupSubTopicList.get(i).equals(groupName))
                return i;
        return -1;
    }
}
