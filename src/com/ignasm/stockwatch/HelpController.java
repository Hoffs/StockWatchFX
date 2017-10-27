package com.ignasm.stockwatch;

import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Ignas Maslinskas
 * 20153209
 * PRIf-15/1
 */
public class HelpController {

    public VBox helpVerticalBox;
    public JFXButton closeButton;

    @FXML
    public void initialize() {
        closeButton.setOnAction(this::onClose);
    }

    private void onClose(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
