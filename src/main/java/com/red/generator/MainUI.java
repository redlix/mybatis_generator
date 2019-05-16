package com.red.generator;

import com.red.generator.controller.MainUIController;
import com.red.generator.utils.ConfigHelper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * main
 * <pre>
 *  Version         Date            Author          Description
 * ------------------------------------------------------------
 *  1.0.0           2019/05/12     red        -
 * </pre>
 *
 * @author redli
 * @version 1.0.0 2019-05-12 10:06
 * @since 1.0.0
 */
public class MainUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        ConfigHelper.createEmptyFiles();
        URL url = Thread.currentThread().getContextClassLoader().getResource("fxml/MainUI.fxml");
        FXMLLoader fxmlLoader =new FXMLLoader(url);
        Parent root = fxmlLoader.load();
        primaryStage.setResizable(true);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        MainUIController controller = fxmlLoader.getController();
        controller.setPrimaryStage(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
