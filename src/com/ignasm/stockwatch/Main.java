package com.ignasm.stockwatch;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Locale;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Font.loadFont(getClass().getResourceAsStream("/RobotoRegular.ttf"), 16);
        Locale.setDefault(new Locale("lt", "LT"));

        Parent root = FXMLLoader.load(getClass().getResource("MainWindow.fxml"));
        primaryStage.setTitle("Akcijos");
        primaryStage.setScene(new Scene(root, 960, 700));
        primaryStage.getScene().getStylesheets().add("com/ignasm/stockwatch/stylesheet.css");
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}