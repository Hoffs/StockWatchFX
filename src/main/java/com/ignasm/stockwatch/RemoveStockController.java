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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.BigDecimalStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    private String oldSymbolValue = "";
    private Timeline timeline;
    private StockPurchaseEntry stockPurchaseEntry;

    RemoveStockController(StockPurchaseEntry purchaseEntry) {
        this.stockPurchaseEntry = purchaseEntry;
    }

    @FXML
    public void initialize() {
        quantityField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
        quantityField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!observable.getValue()) {
                if (quantityField.getText().isEmpty()) quantityField.setText("0");
                else if (Integer.parseInt(quantityField.getText()) < 0) quantityField.setText("0");
                else if (Integer.parseInt(quantityField.getText()) > stockPurchaseEntry.getShareChange()) quantityField.setText(String.valueOf(stockPurchaseEntry.getShareChange()));
            }
            updateOverallPrice();
        });

        priceField.setTextFormatter(new TextFormatter<>(new BigDecimalStringConverter()));
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
    }

    private void updateOverallPrice() {
        if (!quantityField.getText().isEmpty()) {
            BigDecimal quantity;
            BigDecimal stockPrice;

            try {
                quantity = BigDecimal.valueOf(Integer.parseInt(quantityField.getText()));
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

    private void saveStock() {
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
        }
    }

    private void onSave(ActionEvent event) {
        saveStock();
        onClose(event);
    }

    private void onClose(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
