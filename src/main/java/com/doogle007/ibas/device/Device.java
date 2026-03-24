package com.doogle007.ibas.device;

import com.doogle007.ibas.Logger;
import com.doogle007.ibas.controller.MessageController;
import org.json.JSONException;
import org.json.JSONObject;

public class Device implements Comparable<Device> {
    public String name;
    public String logicalAddress;
    public String clientID;
    public String group;
    public long timestamp;
    public boolean switchAuto;
    public boolean switchManual;
    public boolean online;

    public Device(String name, String logicalAddress, String clientID, String group) {
        this.name = name;
        this.logicalAddress = logicalAddress;
        this.clientID = clientID;
        this.group = group;

        this.timestamp = System.currentTimeMillis();
        this.switchAuto = false;
        this.switchManual = false;
        this.online = true;
    }

    public static Device jsonToDevice(JSONObject jsonObject) {
        try {
            String logicalAddress = null;
            String clientID;
            String group = null;
            String name = null;
            if (!jsonObject.isNull("ClientID")) {
                clientID = jsonObject.getString("ClientID");
            } else
                return null;
            if (!jsonObject.isNull("Name")) {
                name = jsonObject.getString("Name");
            }
            if (!jsonObject.isNull("Group")) {
                group = jsonObject.getString("Group");
            }
            if (!jsonObject.isNull("LogicalAddress")) {
                logicalAddress = jsonObject.getString("LogicalAddress");
            }
            Device device = new Device(name, logicalAddress, clientID,group);
            if (!jsonObject.isNull("AlarmAuto")) {
                device.switchAuto = jsonObject.getBoolean("AlarmAuto");
            }
            if (!jsonObject.isNull("AlarmManual")) {
                device.switchManual = jsonObject.getBoolean("AlarmManual");
            }
            if (!jsonObject.isNull("Timestamp")) {
                device.timestamp = jsonObject.getLong("Timestamp");
            }
            return device;
        } catch (JSONException e) {
            Logger.error(e.toString());
        }
        return null;
    }

    public static boolean allDisconnectCheck() {
        boolean hasDisconnect = false;
        for (DeviceGroup deviceGroup : DeviceGroup.DeviceGroupList)
            for (Device device : deviceGroup.deviceList)
                if(device.disconnectCheck())
                    hasDisconnect = true;
        return hasDisconnect;
    }

    public boolean disconnectCheck() {
        if(!this.online || System.currentTimeMillis() - this.timestamp <= 60000)
            return false;
        this.online = false;
        MessageController.addMessage("离线: 设备 " + this.name + " 长时间未响应,已离线.");
        return true;
    }

    public void delete(DeviceGroup currentGroup) {
        for (int index = 0; index < currentGroup.deviceList.size(); index++) {
            if (currentGroup.deviceList.get(index).equals(this)) {
                DeviceIO.deleteFileDevice(this.name, currentGroup.getName());
                currentGroup.deviceList.remove(index);
                return;
            }
        }
    }

    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Name", name);
        jsonObject.put("ClientID", clientID);
        jsonObject.put("Group", group);
        jsonObject.put("LogicalAddress", logicalAddress);
        jsonObject.put("Timestamp", timestamp);
        jsonObject.put("AlarmAuto", switchAuto);
        jsonObject.put("AlarmManual", switchManual);
        return jsonObject.toString();
    }

    @Override
    public int compareTo(Device o) {
        boolean v1, v2, v3, v4;
        v1 = this.online;
        v2 = o.online;
        if (v1 && !v2)
            return -1;
        if (!v1 && v2)
            return 1;
        v3 = this.switchAuto || this.switchManual;
        v4 = o.switchAuto || o.switchManual;
        if (v3 && !v4)
            return -1;
        if (!v3 && v4)
            return 1;
        return 0;
    }
}
