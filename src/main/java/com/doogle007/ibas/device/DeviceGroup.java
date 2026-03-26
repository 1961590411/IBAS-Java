package com.doogle007.ibas.device;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeviceGroup {
    public static List<DeviceGroup> DeviceGroupList = DeviceGroup.initGroupList();

    public static DeviceGroup defaultGroup = new DeviceGroup("Default Group", false, true);
    private static List<DeviceGroup> initGroupList() {
        //创建新ArrayList，为List添加文件中的所有设备组

        //完成后返回设备组
        return DeviceIO.initGroupList();
    }
    public boolean subscribe = false;

    private boolean isDefaultGroup = false;
    public List<Device> deviceList;
    private String name;

    public DeviceGroup(String name) {
        this(name, false);
    }

    public DeviceGroup(String name, boolean subscribe) {
        this(name, subscribe, false);
    }
    public DeviceGroup(String name, boolean subscribe, boolean defaultGroup) {
        this.setName(name);
        this.subscribe = subscribe;
        this.isDefaultGroup = defaultGroup;
        deviceList = new ArrayList<>();
    }

    public static DeviceGroup searchGroup(String name) {
        //TODO: 安全删除name = "Ungrouped Devices";
        if (name == null) {
            name = "";
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
        DeviceIO.writeGroupConfig(group); // 新增：创建组时同时保存 config.json
        return true;
    }

    public static void removeGroup(String name, boolean remain) {
        int index = searchGroupIndex(name);
        if (index < 0)
            return;
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
    }

    public static int searchGroupIndex(String name) {
        for (int index = 0; index < DeviceGroupList.size(); index++) {
            System.out.println(DeviceGroupList.get(index).getName());
            if (DeviceGroupList.get(index).getName().equals(name))
                return index;
        }
        return -1;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void moveDevice(Device device, DeviceGroup targetGroup) {
        //TODO: 因为现在转移设备组需要设备发来确认消息，并由updateDevice()来完成，所以也需要重做或者删除
        for (int index = 0; index < this.deviceList.size(); index++) {
            Device targetDevice = this.deviceList.get(index);
            if (targetDevice.name.equals(device.name)) {
                targetGroup.deviceList.add(device);
                this.deviceList.remove(index);
                DeviceIO.moveFileDevice(device.name, this.name, targetGroup.name);
                return;
            }
        }
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
        //TODO: 全部检索有点浪费性能，以后设备数量多了可能会改进
        for(DeviceGroup group : DeviceGroupList){
            //跳过本设备组
            if(group.name.equals(this.name))
                continue;
            //检索其他设备组
            for(Device targetDevice : group.deviceList){
                if(targetDevice.clientID.equals(device.clientID)){
                    device.switchAuto = targetDevice.switchAuto;
                    device.switchManual = targetDevice.switchManual;
                    deviceList.remove(targetDevice);
                    DeviceIO.deleteDevice(targetDevice);
                }
            }
        }
        //别忘了检查默认组
        if(!isDefaultGroup){
            for(Device targetDevice : defaultGroup.deviceList){
                if(targetDevice.clientID.equals(device.clientID)){
                    deviceList.remove(targetDevice);
                    DeviceIO.deleteDevice(targetDevice);
                }
            }
        }

        //保存JSON字符串
        DeviceIO.writeDevice(device);
    }

    public int searchDeviceIndex(String clientID) {
        System.out.println("正在设备组"+this.name+"中寻找设备"+clientID);
        System.out.println("当前组内设备:");
        for(Device device : this.deviceList)
            System.out.println(device.clientID + " " + device.name);
        for (int index = 0; index < this.deviceList.size(); index++) {
            Device device = this.deviceList.get(index);
            if (device.clientID.equals(clientID)) {
                System.out.println("搜寻成功! Index序列号 "+index+ "\n");
                return index;
            }
        }
        System.out.println("未找到匹配的ClientID\n");
        return -1;
    }
}
