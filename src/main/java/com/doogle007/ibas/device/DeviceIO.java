package com.doogle007.ibas.device;

import com.doogle007.ibas.Logger;
import org.json.JSONObject;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeviceIO {
    public static List<Device> readDeviceList(String groupName) {
        List<Device> deviceList = new ArrayList<>();
        List<String> fileList = readDeviceFileList(groupName);
        for (String fileName : fileList) {
            // 关键修改：路径增加 /devices/
            Path filePath = Paths.get("device/" + groupName + "/devices/" + fileName);
            try {
                if (Files.exists(filePath)) {
                    String data = Files.readString(filePath);
                    JSONObject jsonObject = new JSONObject(data);
                    Device device = Device.jsonToDevice(jsonObject);
                    if (device != null) {
                        device.online = false;
                        deviceList.add(device);
                    }
                }
            } catch (IOException ignored) {}
        }
        return deviceList;
    }

    private static List<String> readDeviceFileList(String groupName) {
        // 关键修改：路径改为 device/{groupName}/devices/
        Path path = Paths.get("device/" + groupName + "/devices");
        if (!Files.isDirectory(path)) {
            return new ArrayList<>();
        }
        try (Stream<Path> stream = Files.list(path)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .filter(name -> name.toLowerCase().endsWith(".json"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            Logger.error("读取设备目录出错: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 保存组配置信息
     */
    public static void writeGroupConfig(DeviceGroup group) {
        Path path = Paths.get("device/" + group.getName() + "/config.json");
        try {
            // 确保目录存在
            Files.createDirectories(path.getParent());
            JSONObject config = new JSONObject();
            config.put("name", group.getName());
            config.put("subscribe", group.subscribe);
            Files.writeString(path, config.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Logger.info("已更新组配置: " + group.getName());
        } catch (IOException e) {
            Logger.error("保存组配置出错: " + e.getMessage());
        }
    }

    /**
     * 读取组订阅状态，如果文件不存在或读取失败，抛出异常以便外层处理创建逻辑
     */
    private static boolean readGroupSubscribe(String groupName) throws IOException {
        Path path = Paths.get("device/" + groupName + "/config.json");
        if (!Files.exists(path)) {
            throw new IOException("Config file missing for group: " + groupName);
        }
        String data = Files.readString(path);
        JSONObject config = new JSONObject(data);
        return config.optBoolean("subscribe", false);
    }

    public static void writeDevice(Device device) {
        if(DeviceGroup.searchGroupIndex(device.group) < 0){
            Logger.error("写入设备时出现错误: 未找到归属的设备组");
            return;
        }

        //构建文件路径
        Path dirPath = Paths.get("device/" + device.group + "/devices");
        try {
            //保存并自动创建子文件夹
            Files.createDirectories(dirPath);
            Path filePath = dirPath.resolve(device.clientID + ".json");
            Files.writeString(filePath, device.toJson(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            Logger.error("写入设备文件失败: " + e.getMessage());
        }
    }
    private static List<String> readGroupDirectories() {
        Path path = Paths.get("device");
        if (!Files.isDirectory(path)) {
            // 如果 device 目录不存在，尝试创建它
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                Logger.error("创建 device 根目录失败: " + e.getMessage());
            }
            return new ArrayList<>();
        }

        try (Stream<Path> stream = Files.list(path)) {
            return stream
                    .filter(Files::isDirectory) // 过滤，只保留文件夹
                    .map(p -> p.getFileName().toString()) // 获取文件夹名称
                    .collect(Collectors.toList());
        } catch (IOException e) {
            Logger.error("读取组目录出错: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    public static List<DeviceGroup> initGroupList() {
        List<DeviceGroup> deviceGroupList = new ArrayList<>();
        List<String> groupNameList = readGroupDirectories(); // 获取所有组名

        for (String groupName : groupNameList) {
            boolean subscribe;
            try {
                subscribe = readGroupSubscribe(groupName);
            } catch (Exception e) {
                subscribe = false;
                // 如果没有配置则创建默认配置
                DeviceGroup stub = new DeviceGroup(groupName, false, groupName.equals("Default Group"));
                writeGroupConfig(stub);
            }

            DeviceGroup group = new DeviceGroup(groupName, subscribe, groupName.equals("Default Group"));
            group.deviceList = readDeviceList(groupName);
            deviceGroupList.add(group);

            // --- 新增启动订阅逻辑 ---
            if (subscribe) {
                // 确保 MQTT 连接已初始化后调用
                // 注意：这里需要确保 Connect 类已经准备就绪
                try {
                    com.doogle007.ibas.network.Connect.groupSubTopicAdd(groupName);
                    Logger.info("启动自动订阅组主题: " + groupName);
                } catch (Exception e) {
                    Logger.error("启动订阅失败: " + groupName + ", 原因: " + e.getMessage());
                }
            }
        }
        return deviceGroupList;
    }

    public static void deleteGroupFile(String name) throws IOException {
        Path directory = Paths.get("device/" + name);
        if (!Files.exists(directory)) return;

        Files.walkFileTree(directory, new SimpleFileVisitor<>() {
            // 1. 先删除文件
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            // 2. 再删除目录本身（此时目录已空）
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
        System.out.println("文件夹删除成功！");
    }

    public static void createGroupFile(String name) {
        Path groupPath = Paths.get("device/" + name);
        Path devicesPath = groupPath.resolve("devices"); // 新增子目录路径
        try {
            Files.createDirectories(groupPath);
            Files.createDirectories(devicesPath); // 同时创建设备存放目录
        } catch (IOException ignored) {}
    }

    public static void moveFileDevice(String device, String fromGroup, String toGroup) {
        Path sourcePath = Paths.get("device/" + fromGroup + "/" + device + ".json");
        Path targetPath = Paths.get("device/" + toGroup + "/" + device + ".json");
        try {
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void deleteFileDevice(String device, String group) {
        Path path = Paths.get("device/" + group + "/" + device + ".json");
        try {
            Files.delete(path);
        } catch (IOException ignored) {
        }
    }

    public static void deleteDevice(Device targetDevice, DeviceGroup targetGroup) {
        System.out.println("删除设备中，设备名称: " +  targetDevice.name);
        System.out.println("文件路径: " + "device/" + targetGroup.getName() + "/devices/" + targetDevice.clientID + ".json");
        Path path = Paths.get("device/" + targetGroup.getName() + "/devices/" + targetDevice.clientID + ".json");
        try {
            Files.delete(path);
            System.out.println("删除成功");
        } catch (IOException e) {
            Logger.error(e.toString());
        }
    }
}
