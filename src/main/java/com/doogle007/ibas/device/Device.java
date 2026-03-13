package com.doogle007.ibas.device;

import com.doogle007.ibas.Logger;
import com.doogle007.ibas.controller.MessageController;
import org.json.JSONException;
import org.json.JSONObject;

public class Device implements Comparable<Device> {
    public String device_name;
    public String logical_address;
    public long timestamp;
    public boolean switch_auto;
    public boolean switch_manual;
    public boolean online;

    public Device(String device_name, String logical_address, long timestamp, boolean switch_auto, boolean switch_manual, boolean online) {
        this.device_name = device_name;
        this.logical_address = logical_address;
        this.timestamp = timestamp;
        this.switch_auto = switch_auto;
        this.switch_manual = switch_manual;
        this.online = online;
    }

    public static Device jsonToDevice(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            boolean switchAuto = jsonObject.getBoolean("AlarmSwitchAuto");
            boolean switchManual = jsonObject.getBoolean("AlarmSwitchManual");
            String address = jsonObject.getString("LogicalAddress");
            String deviceID = jsonObject.getString("DeviceID");
            long timestamp = jsonObject.has("Timestamp") ? jsonObject.getLong("Timestamp") : System.currentTimeMillis();
            return new Device(deviceID, address, timestamp, switchAuto, switchManual, true);
        } catch (JSONException e) {
            Logger.error(e.toString());
        }
        return null;
    }

    public static void AddDevice(Device newDevice) {
        if (newDevice.switch_manual || newDevice.switch_auto)
            MessageController.addMessage("警报: 设备 " + newDevice.device_name + " 发出警报，请及时处理!");
        for (DeviceGroup deviceGroup : DeviceGroup.DeviceGroupList)
            for (int i = 0; i < deviceGroup.deviceList.size(); i++) {
                Device inner_device = deviceGroup.deviceList.get(i);
                if (inner_device.device_name.equals(newDevice.device_name)) {
                    if (inner_device.timestamp < newDevice.timestamp) {
                        deviceGroup.deviceList.set(i, newDevice);
                        MessageController.addMessage("更新: 设备 " + newDevice.device_name + " 状态更新。");
                    } else
                        MessageController.addMessage("通知: 设备 " + newDevice.device_name + " 上传了更新，但该更新是过时的。");
                    return;
                }
            }
        DeviceGroup.searchGroup(null).deviceList.add(newDevice);
        MessageController.addMessage("更新: 添加 " + newDevice.device_name + " 为新的设备。");
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
        MessageController.addMessage("离线: 设备 " + this.device_name + " 长时间未响应,已离线.");
        return true;
    }

    public void delete(DeviceGroup currentGroup) {
        for (int index = 0; index < currentGroup.deviceList.size(); index++) {
            if (currentGroup.deviceList.get(index).equals(this)) {
                DeviceIO.deleteFileDevice(this.device_name, currentGroup.getName());
                currentGroup.deviceList.remove(index);
                return;
            }
        }
    }

    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("DeviceID", device_name);
        jsonObject.put("LogicalAddress", logical_address);
        jsonObject.put("Timestamp", timestamp);
        jsonObject.put("AlarmSwitchAuto", switch_auto);
        jsonObject.put("AlarmSwitchManual", switch_manual);
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
        v3 = this.switch_auto || this.switch_manual;
        v4 = o.switch_auto || o.switch_manual;
        if (v3 && !v4)
            return -1;
        if (!v3 && v4)
            return 1;
        return 0;
    }
}
