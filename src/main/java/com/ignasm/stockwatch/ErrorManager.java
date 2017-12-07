package com.ignasm.stockwatch;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ErrorManager {
    public static List<Node> errorNodes = new ArrayList<>();

    public static void addError(String text, Node node, Pane parent) {
        Label error = new Label(text);
        error.setTextFill(Color.RED);
        errorNodes.add(error);
        parent.getChildren().add(error);

        error.setLayoutX(node.localToScene(node.getBoundsInLocal()).getMinX());
        error.setLayoutY(node.localToScene(node.getBoundsInLocal()).getMaxY() + 2);
        error.setPrefWidth(300);
    }

    public static void clearErrorNodes(Pane parent) {
        Node[] errors = getPaneErrorNodes(parent);
        parent.getChildren().removeAll(errors);
        errorNodes.removeAll(Arrays.asList(errors));
    }

    private static Node[] getPaneErrorNodes(Pane parent) {
        return parent.getChildren().stream()
                .filter(node -> errorNodes.contains(node))
                .toArray(Node[]::new);
    }

    public static String checkPriceField(String text) {
        if (text.length() > 12) {
            text = text.substring(0, 12);
        }
        text = text.replaceAll(",", ".");
        String allowedChars = "1234567890.";
        StringBuilder newText = new StringBuilder();
        boolean hadSeparator = false;
        for (char c : text.toCharArray()) {
            if (allowedChars.indexOf(c) != -1 && (!hadSeparator || c != '.')) {
                newText.append(c);
            }
            if (c == '.') hadSeparator = true;
        }
        return newText.toString();
    }

    public static String checkQuantityField(String text) {
        if (text.length() > String.valueOf(Integer.MAX_VALUE).length() - 1) {
            text = text.substring(0, String.valueOf(Integer.MAX_VALUE).length() - 1); // General Motors has <10Billion shares, 12numbers = 10billion.
        }
        String allowedChars = "1234567890";
        StringBuilder newText = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (allowedChars.indexOf(c) != -1) {
                newText.append(c);
            }
        }
        return newText.toString();
    }
}
