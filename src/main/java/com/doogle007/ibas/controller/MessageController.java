package com.doogle007.ibas.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.util.Duration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MessageController {
    public static SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd-HH:mm");
    private static ArrayList<String> messages = new ArrayList<>();
    private Timeline timelineMessage;
    @FXML
    private ListView<String> messageList;

    public static void addMessage(String message) {
        long time = System.currentTimeMillis();
        messages.add(format.format(time) + " - " + message);
    }

    @FXML
    private void initialize() {
        timelineMessage = new Timeline(new KeyFrame(Duration.seconds(5), event -> updateMessage()));
        timelineMessage.setCycleCount(Timeline.INDEFINITE);
        timelineMessage.play();
        updateMessage();

    }

    protected void updateMessage() {
        if (HelloController.index != 1)
            timelineMessage.stop();
        else {
            messageList.getItems().clear();
            for (String message : messages)
                messageList.getItems().add(0, message);
        }
    }
}
