package com.doogle007.ibas.device;

import com.doogle007.ibas.Logger;

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
            Path filePath = Paths.get("device/" + groupName + "/" + fileName);
            try {
                if (Files.exists(filePath)) {
                    String data = Files.readString(filePath);
                    Device device = Device.jsonToDevice(data);
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

    public static void writeDevice(Device device) {
        DeviceGroup group = DeviceGroup.includeDevice(device.device_name);
        Path path = Paths.get("device/" + group.getName() + "/" + device.device_name + ".json");
        try {
            Files.writeString(path, device.toJson(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            Logger.info("保存的设备名称：" + device.device_name);
        } catch (IOException ignored) {
        }
    }

    public static List<DeviceGroup> initGroupList() {
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
}
