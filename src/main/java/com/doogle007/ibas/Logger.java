package com.doogle007.ibas;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    // 所有的日志都会放在这个文件夹下
    private static final String LOG_DIR = "logs";

    // 预定义时间格式，避免重复创建对象
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 核心写入逻辑
     */
    public static void log(String level, String message) {
        LocalDateTime now = LocalDateTime.now();

        // 1. 生成文件名：logs/app-2026-01-26.log
        String fileName = String.format("app-%s.log", now.format(DATE_FORMATTER));
        Path filePath = Path.of(LOG_DIR, fileName);

        // 2. 组装日志内容：[2026-01-26 04:59:32] [INFO] 你的消息
        String logLine = String.format("[%s] [%s] %s%n",
                now.format(TIME_FORMATTER),
                level.toUpperCase(),
                message);

        try {
            // 3. 检查并创建文件夹
            if (Files.notExists(filePath.getParent())) {
                Files.createDirectories(filePath.getParent());
            }

            // 4. 追加写入（UTF-8 编码）
            Files.writeString(filePath, logLine,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);

        } catch (IOException e) {
            // 小型项目直接打印到控制台，防止日志报错导致程序崩溃
            Logger.error("无法记录日志: " + e.getMessage());
        }
    }

    // --- 快捷调用接口 ---
    public static void info(String msg) {
        log("INFO", msg);
    }

    public static void warn(String msg) {
        log("WARN", msg);
    }

    public static void error(String msg) {
        log("ERROR", msg);
    }
}
