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
        //读取文件，若不存在则创建文件夹
        List<String> fileList = readDeviceFileList(groupName);
        for (String fileName : fileList) {
            Path filePath = Paths.get("device/" + groupName + "/" + fileName);
            try {
                if (Files.exists(filePath)) {
                    String data = Files.readString(filePath);
                    JSONObject jsonObject = new JSONObject(data);
                    Device device = Device.jsonToDevice(jsonObject);
                    if (device != null) {
                        device.online = false;
                    }
                    deviceList.add(device);
                }
            } catch (IOException ignored) {
            }
        }
        return deviceList;
    }

    private static List<String> readDeviceFileList(String groupName) {
        Path path = Paths.get("device/" + groupName);
        if (!Files.isDirectory(path)) {
            return new ArrayList<>();
        }
        try (Stream<Path> stream = Files.list(path)) {
            return stream
                    .filter(Files::isRegularFile) // 仅限文件
                    .map(p -> p.getFileName().toString()) // 转换为字符串文件名
                    .filter(name -> name.toLowerCase().endsWith(".json")) // 过滤 JSON 后缀
                    .collect(Collectors.toList()); // 收集到 List 中
        } catch (IOException e) {
            Logger.error("读取目录出错: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // 保存组配置信息（包括 subscribe）
    public static void writeGroupConfig(DeviceGroup group) {
        Path path = Paths.get("device/" + group.getName() + "/config.json");
        try {
            JSONObject config = new JSONObject();
            config.put("name", group.getName());
            config.put("subscribe", group.subscribe);
            Files.writeString(path, config.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            Logger.error("保存组配置出错: " + e.getMessage());
        }
    }

    // 读取组配置信息
    public static boolean readGroupSubscribe(String groupName) {
        Path path = Paths.get("device/" + groupName + "/config.json");
        if (Files.exists(path)) {
            try {
                String data = Files.readString(path);
                JSONObject config = new JSONObject(data);
                return config.optBoolean("subscribe", false);
            } catch (IOException ignored) {}
        }
        return false;
    }

    public static void writeDevice(Device device) {
        System.out.println("保存设备中，设备名称: " +  device.name);
        DeviceGroup group = DeviceGroup.includeDevice(device.name);
        Path path = Paths.get("device/" + group.getName() + "/" + device.clientID + ".json");
        try {
            Files.writeString(path, device.toJson(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            Logger.info("保存的设备名称：" + device.name);
        } catch (IOException ignored) {
        }
    }

    public static List<DeviceGroup> initGroupList() {
        //TODO: 因为在DeviceGroup中新写了初始化设备组的方法，这一段需要重写一下
        //TODO: 还有，文件用唯一ID保存，不要用name了，实例-默认组ID：default_group
        List<DeviceGroup> deviceGroupList = new ArrayList<>();
        List<String> groupNameList;

        Path path = Paths.get("device");
        if (!Files.isDirectory(path)) {
            groupNameList = new ArrayList<>();
        } else try (Stream<Path> stream = Files.list(path)) {
            groupNameList = stream
                    .filter(Files::isDirectory) // 仅限文件
                    .map(p -> p.getFileName().toString()) // 转换为字符串文件名
                    .toList(); // 收集到 List 中
        } catch (IOException e) {
            Logger.error("读取目录出错: " + e.getMessage());
            return new ArrayList<>();
        }
        for (String fileName : groupNameList) {
            DeviceGroup group = new DeviceGroup(fileName);
            group.deviceList = readDeviceList(fileName);
            deviceGroupList.add(group);
        }
        for (String fileName : groupNameList) {
            boolean sub = readGroupSubscribe(fileName); // 新增：读取持久化的变量
            DeviceGroup group = new DeviceGroup(fileName, sub, fileName.equals("Default Group"));
            group.deviceList = readDeviceList(fileName);
            deviceGroupList.add(group);
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
        Path path = Paths.get("device/" + name);
        try {
            // 创建目录（包括任何不存在的父目录）
            Files.createDirectories(path);
        } catch (IOException ignored) {
        }
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

    public static void deleteDevice(Device targetDevice) {
        System.out.println("删除设备中，设备名称: " +  targetDevice.name);
        Path path = Paths.get("device/" + targetDevice.group + "/" + targetDevice.clientID + ".json");
        try {
            Files.delete(path);
            System.out.println("删除成功");
        } catch (IOException e) {
            Logger.error(e.toString());
        }
    }
}
