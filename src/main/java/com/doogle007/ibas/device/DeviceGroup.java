package com.doogle007.ibas.device;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeviceGroup {
    public static List<DeviceGroup> DeviceGroupList = DeviceGroup.initGroupList();

    public static DeviceGroup defaultGroup = new DeviceGroup("Default Group");
    private static List<DeviceGroup> initGroupList() {
        //创建新ArrayList，为List添加文件中的所有设备组

        //完成后返回设备组
        return DeviceIO.initGroupList();
    }
    public boolean subscribed = false;
    public List<Device> deviceList;
    private String name;

    public DeviceGroup(String name) {
        new DeviceGroup(name, false);
    }

    public DeviceGroup(String name, boolean subscribed) {
        this.setName(name);
        this.subscribed = subscribed;
        deviceList = new ArrayList<>();
    }

    public static DeviceGroup searchGroup(String name) {
        //TODO: 安全删除name = "Ungrouped Devices";
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
                if (device.name.equals(name))
                    return deviceGroup;
        return searchGroup(null);
    }

    public static boolean addGroup(DeviceGroup group) {
        if (searchGroupIndex(group.name) >= 0)
            return false;
        DeviceGroupList.addFirst(group);
        DeviceIO.createGroupFile(group.name);
        return true;
    }

    public static boolean removeGroup(String name, boolean remain) {
        int index = searchGroupIndex(name);
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

    public static int searchGroupIndex(String name) {
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
        //TODO: 因为现在转移设备组需要设备发来确认消息，所以也需要重做或者删除
        for (int index = 0; index < this.deviceList.size(); index++) {
            Device targetDevice = this.deviceList.get(index);
            if (targetDevice.name.equals(device.name)) {
                targetGroup.deviceList.add(device);
                this.deviceList.remove(index);
                DeviceIO.moveFileDevice(device.name, this.name, targetGroup.name);
                return true;
            }
        }
        return false;
    }

    public void updateDevice(Device device) {
        //在设备组中寻找该设备
        int index = searchDeviceIndex(device.clientID);

        //未寻找到：添加该设备
        //已寻找到：取代该设备
        if (index < 0)
            deviceList.add(device);
        else{
            Device targetDevice = this.deviceList.get(index);
            device.switchAuto = targetDevice.switchAuto;
            device.switchManual = targetDevice.switchManual;
            deviceList.set(index, device);
        }

        //检查其他所有设备组，比对设备ID，删除其他设备组中的该设备，用于防止未知的设备组转移
        //TODO: 我知道这样很浪费性能，但为了能跑先这样了，以后会解决的
        for(DeviceGroup group : DeviceGroupList){
            //跳过本设备组
            if(group.name.equals(this.name))
                continue;
            //检索其他设备组
            for(Device targetDevice : group.deviceList){
                if(targetDevice.clientID.equals(device.clientID)){
                    deviceList.remove(targetDevice);
                }
            }
        }
    }

    public int searchDeviceIndex(String deviceID) {
        for (int index = 0; index < this.deviceList.size(); index++) {
            Device device = this.deviceList.get(index);
            if (device.clientID.equals(deviceID))
                return index;
        }
        return -1;
    }
}
