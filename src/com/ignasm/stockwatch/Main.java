package com.ignasm.stockwatch;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Locale;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Font.loadFont(getClass().getResourceAsStream("resources/RobotoRegular.ttf"), 16);
        Locale.setDefault(new Locale("lt", "LT"));

        Parent root = FXMLLoader.load(getClass().getResource("resources/MainWindow.fxml"));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("resources/logo40.png")));
        primaryStage.setTitle("Stock Watch");
        primaryStage.setScene(new Scene(root, 960, 700));
        primaryStage.getScene().getStylesheets().add("com/ignasm/stockwatch/resources/stylesheet.css");
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
