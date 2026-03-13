package com.doogle007.ibas.device;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeviceGroup {
    public static List<DeviceGroup> DeviceGroupList = DeviceIO.initGroupList();

    public List<Device> deviceList;
    private String name;

    public DeviceGroup(String name) {
        this.setName(name);
        deviceList = new ArrayList<>();
    }

    public static DeviceGroup searchGroup(String name) {
        if (name == null) {
            name = "Ungrouped Devices";
        }
        for (DeviceGroup deviceGroup : DeviceGroupList) {
            if (deviceGroup.name.equals(name)) {
                return deviceGroup;
            }
        }
        DeviceGroup deviceGroup = new DeviceGroup(name);
        addGroup(deviceGroup);
        return deviceGroup;
    }

    public static DeviceGroup includeDevice(String name) {
        for (DeviceGroup deviceGroup : DeviceGroupList)
            for (Device device : deviceGroup.deviceList)
                if (device.device_name.equals(name))
                    return deviceGroup;
        return searchGroup(null);
    }

    public static boolean addGroup(DeviceGroup group) {
        if (existGroup(group.name) >= 0)
            return false;
        DeviceGroupList.add(0, group);
        DeviceIO.createGroupFile(group.name);
        return true;
    }

    public static boolean removeGroup(String name, boolean remain) {
        int index = existGroup(name);
        if (index < 0)
            return false;
        List<Device> removedList = remain ? DeviceGroupList.get(index).deviceList : null;
        DeviceGroupList.remove(index);
        if (remain) {
            DeviceGroup defaultGroup = searchGroup(null);
            for (Device device : removedList) {
                defaultGroup.deviceList.add(device);
                DeviceIO.writeDevice(device);
            }
        }
        try {
            DeviceIO.deleteGroupFile(name);
        } catch (IOException ignored) {
        }
        return true;
    }

    public static int existGroup(String name) {
        for (int index = 0; index < DeviceGroupList.size(); index++)
            if (DeviceGroupList.get(index).name.equals(name))
                return index;
        return -1;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean moveDevice(Device device, DeviceGroup targetGroup) {
        for (int index = 0; index < this.deviceList.size(); index++) {
            Device targetDevice = this.deviceList.get(index);
            if (targetDevice.device_name.equals(device.device_name)) {
                targetGroup.deviceList.add(device);
                this.deviceList.remove(index);
                DeviceIO.moveFileDevice(device.device_name, this.name, targetGroup.name);
                return true;
            }
        }
        return false;
    }
}
