package com.doogle007.ibas;
import java.io.*;
import java.util.Properties;

public class Options {
    public static final String localAddress = Options.getInstance().get("ServerAddress","");
    public static final String localPort = Options.getInstance().get("ServerPort","");
    private static final String FILE_PATH = "options.ini";
    private final Properties properties = new Properties();
    private static Options instance;

    // 私有构造函数：加载文件
    private Options() {
        if(!load()){
            set("ServerAddress", "120.26.133.159");
            set("ServerPort", "1883");
            save();
        }
    }

    // 获取单例
    public static synchronized Options getInstance() {
        if (instance == null) {
            instance = new Options();
        }
        return instance;
    }

    // 从磁盘加载配置
    private boolean load() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try (InputStream input = new FileInputStream(FILE_PATH)) {
                properties.load(input);
            } catch (IOException e) {
                System.out.println("无法读取配置文件: " + e.getMessage());
            }
            return true;
        }
        return false;
    }

    // 保存配置到磁盘
    public void save() {
        try (OutputStream output = new FileOutputStream(FILE_PATH)) {
            properties.store(output, "User Settings");
        } catch (IOException e) {
            System.out.println("无法保存配置文件: " + e.getMessage());
        }
    }

    // 获取设置（带默认值，防止空指针）
    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    // 设置新值并立即保存
    public void set(String key, String value) {
        properties.setProperty(key, value);
        save();
    }
}
