package com.doogle007.ibas;

import com.doogle007.ibas.network.Connect;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class MainApplication extends Application {
    public static void main(String[] args) {
        try {
            Connect.start();
        } catch (Exception e) {
            Logger.error("无法连接至云服务器");
            Logger.error(e.getMessage());
        }

        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("hello-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 840, 490);
        scene.setFill(Color.TRANSPARENT);

        stage.setTitle("智能楼宇警报系统");
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.show();
    }
}
