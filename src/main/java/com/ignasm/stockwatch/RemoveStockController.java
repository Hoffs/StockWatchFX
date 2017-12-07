package com.ignasm.stockwatch;

import com.ignasm.stockwatch.data.StockDataManager;
import com.ignasm.stockwatch.data.StockPriceEntry;
import com.ignasm.stockwatch.data.StockPurchaseEntry;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.BigDecimalStringConverter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Ignas Maslinskas
 * 20153209
 * PRIf-15/1
 */
public class RemoveStockController {
    public VBox mainBox;

    public VBox stockBox;
    public Label stockLabel;
    public JFXTextField stockField;

    public VBox overallPriceBox;
    public Label overallLabel;
    public JFXTextField overallField;

    public VBox stockQuantityBox;
    public JFXTextField quantityField;
    public Label quantityLabel;

    public VBox stockPriceBox;
    public JFXTextField priceField;
    public Label priceLabel;

    public HBox buttonsBox;
    public JFXButton buttonAccept;
    public JFXButton buttonClose;

    public Pane mainPane;

    private String oldSymbolValue = "";
    private Timeline timeline;
    private StockPurchaseEntry stockPurchaseEntry;

    @FXML
    private Label errorMessage;

    public RemoveStockController(StockPurchaseEntry purchaseEntry) {
        this.stockPurchaseEntry = purchaseEntry;
    }

    @FXML
    public void initialize() {
        mainBox.prefHeightProperty().bind(mainPane.heightProperty());
        mainBox.prefWidthProperty().bind(mainPane.widthProperty());
        // quantityField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
        quantityField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!observable.getValue()) {
                if (quantityField.getText().isEmpty()) quantityField.setText("0");
                else if (BigInteger.valueOf(Long.parseLong(quantityField.getText())).compareTo(BigInteger.ZERO) < 0)
                    quantityField.setText("0");
                else if (BigInteger.valueOf(Long.parseLong(quantityField.getText()))
                        .compareTo(BigInteger.valueOf(Integer.toUnsignedLong(stockPurchaseEntry.getShareChange()))) > 0)
                    quantityField.setText(String.valueOf(stockPurchaseEntry.getShareChange()));
            }
            updateOverallPrice();
        });
        priceField.setTextFormatter(new TextFormatter<String>(change -> {
            if (change.isReplaced()) {
                Pattern pricePattern = Pattern.compile("(0|[1-9]\\d*)(\\.\\d*)$");
                // Matcher matcher
                // System.out.println(change.getText().matches("(0|[1-9]\\d*)(\\.\\d+)"));
            }
            return change;
        }));

        // priceField.setTextFormatter(new TextFormatter<>(new BigDecimalStringConverter()));
        overallField.setTextFormatter(new TextFormatter<>(new BigDecimalStringConverter()));

        priceField.setOnKeyReleased(e -> updateOverallPrice());
        quantityField.setOnKeyReleased(e -> updateOverallPrice());

        buttonsBox.getStyleClass().add("button-box");
        buttonClose.setOnAction(this::onClose);
        buttonAccept.setOnAction(this::onSave);

        stockField.setText(stockPurchaseEntry.getStockEntry().getSymbol() + " " + stockPurchaseEntry.getStockEntry().getCompany());
        priceField.setText(String.valueOf(stockPurchaseEntry.getUnitPrice()));
        quantityField.setText(String.valueOf(stockPurchaseEntry.getShareChange()));
        updateOverallPrice();
        addValidations();
    }

    private void addValidations() {
        priceField.textProperty().addListener((observable, oldValue, newValue) ->
                priceField.setText(ErrorManager.checkPriceField(priceField.getText())));
        quantityField.textProperty().addListener((observable, oldValue, newValue) ->
                quantityField.setText(ErrorManager.checkQuantityField(quantityField.getText())));
    }

    private boolean checkErrors() {
        ErrorManager.clearErrorNodes(mainPane);
        boolean hasErrors = false;

        if (priceField.getText() == null || priceField.getText().isEmpty()) {
            ErrorManager.addError("Neužpildytas kainos laukas!", priceField, mainPane);
            hasErrors = true;
        }
        if (quantityField.getText() == null || quantityField.getText().isEmpty()) {
            ErrorManager.addError("Neužpildytas kiekio laukas!", quantityField, mainPane);
            hasErrors = true;
        }
        if (quantityField.getText() != null && quantityField.getText().equals("0")) {
            ErrorManager.addError("Negalima panaikinti 0-io akcijų!", quantityField, mainPane);
            hasErrors = true;
        }

        return hasErrors;
    }

    private void updateOverallPrice() {
        if (!quantityField.getText().isEmpty()) {
            BigDecimal quantity;
            BigDecimal stockPrice;

            try {
                quantity = BigDecimal.valueOf(Long.parseLong(quantityField.getText()));
            } catch (NumberFormatException e) {
                quantity = new BigDecimal(0);
            }

            try {
                stockPrice = BigDecimal.valueOf(Double.parseDouble(priceField.getText()));
            } catch (NumberFormatException e) {
                stockPrice = new BigDecimal(0);
            }

            BigDecimal overallPrice = quantity.multiply(stockPrice);
            overallField.setText(overallPrice.toString());
        }
    }

    private boolean saveStock() {
        if (stockPurchaseEntry != null) {
            StockPurchaseEntry sellEntry = new StockPurchaseEntry(
                    -1,
                    -1 * Integer.parseInt(quantityField.getText()),
                    Double.parseDouble(priceField.getText()),
                    stockPurchaseEntry.getCurrency(),
                    Double.parseDouble(overallField.getText()),
                    stockPurchaseEntry.getStockEntry()
            );
            StockDataManager.insertStockPriceEntry(new StockPriceEntry(
                    sellEntry.getStockEntry(),
                    sellEntry.getUnitPrice(),
                    sellEntry.getCurrency(),
                    LocalDateTime.now().toString()
            ));
            StockDataManager.insertStockPurchaseEntry(sellEntry);
            return true;
        }
        return false;
    }

    private void onSave(ActionEvent event) {
        if (!checkErrors() && saveStock()) {
            onClose(event);
        }
    }

    private void onClose(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
