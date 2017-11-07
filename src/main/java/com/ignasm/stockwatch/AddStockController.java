package com.ignasm.stockwatch;

import com.ignasm.stockwatch.data.*;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.converter.BigDecimalStringConverter;
import javafx.util.converter.IntegerStringConverter;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Ignas Maslinskas
 * 20153209
 * PRIf-15/1
 */
public class AddStockController {
    public VBox mainBox;

    public VBox stockSymbolBox;
    public Label symbolLabel;
    public JFXTextField symbolField;

    public VBox stockCompanyBox;
    public Label companyLabel;
    public JFXTextField companyField;

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
    private String lastCurrency;

    @FXML
    public void initialize() {
        buttonsBox.getStyleClass().add("button-box");

        quantityField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
        priceField.setTextFormatter(new TextFormatter<>(new BigDecimalStringConverter()));
        overallField.setTextFormatter(new TextFormatter<>(new BigDecimalStringConverter()));

        symbolField.setOnKeyReleased(e -> verifySymbolEvent());
        priceField.setOnKeyReleased(e -> updateOverallPrice());
        quantityField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (Integer.parseInt(quantityField.getText()) < 0) quantityField.setText("0");
            updateOverallPrice();
        });

        buttonClose.setOnAction(this::onClose);
        buttonAccept.setOnAction(this::onSave);
    }

    private void updateOverallPrice() {
        if (!quantityField.getText().isEmpty()) {
            BigDecimal quantity;
            try {
                quantity = BigDecimal.valueOf(Integer.parseInt(quantityField.getText()));
            } catch (NumberFormatException e) {
                quantity = new BigDecimal(0);
            }

            BigDecimal stockPrice;
            try {
                stockPrice = BigDecimal.valueOf(Double.parseDouble(priceField.getText()));
            } catch (NumberFormatException e) {
                stockPrice = new BigDecimal(0);
            }

            BigDecimal overallPrice = quantity.multiply(stockPrice);
            overallField.setText(overallPrice.toString());
        }
    }

    private void verifySymbolEvent() {
        if (!oldSymbolValue.equals(symbolField.getText())) {
            oldSymbolValue = symbolField.getText();

            if (timeline != null) timeline.stop();

            timeline = new Timeline(new KeyFrame(
                    Duration.millis(500),
                    e -> verifySymbol()
            ));

            timeline.play();
        }
    }

    private void verifySymbol() {
        if (symbolField.getText().isEmpty()) return;

        try {
            Stock currentStock = YahooFinance.get(symbolField.getText());
            if (!currentStock.isValid()) throw new IOException();
            fillOtherFields(currentStock);
            // System.out.println(currentStock.getQuote().getPrice());
        } catch (IOException e) {
            System.out.println("Failed YahooFinanceAPI. Using fallback...");
            // e.printStackTrace();
        }

        try {
            SimpleStock currentStock = YahooFinanceWrapper.getSimpleStock(symbolField.getText());
            fillOtherFields(currentStock);
            System.out.println("Fallback completed!");
        } catch (IOException e) {
            System.out.println("Failed YahooFinanceWrapper");
            e.printStackTrace();
        }
    }

    private void fillOtherFields(Stock stock) {
        companyField.setText(stock.getName());
        lastCurrency = stock.getCurrency();
        if (stock.getQuote().getPrice() != null) {
            priceField.setText(String.valueOf(stock.getQuote().getPrice()));
        } else {
            priceField.setText("0");
        }
        quantityField.setText("1");
        updateOverallPrice();
    }

    private void fillOtherFields(SimpleStock stock) {
        companyField.setText(stock.getCompanyName());
        lastCurrency = stock.getCurrency();
        quantityField.setText("1");
        priceField.setText(stock.getPrice());
        updateOverallPrice();
    }

    private void saveStock() {
        StockEntry stockEntry = new StockEntry(
                -1,
                companyField.getText(),
                symbolField.getText().toUpperCase()
        );

        StockPurchaseEntry purchaseEntry = new StockPurchaseEntry(
                -1,
                Integer.parseInt(quantityField.getText()),
                Double.parseDouble(priceField.getText()),
                (lastCurrency != null) ? lastCurrency : "USD",
                -1 * Double.parseDouble(overallField.getText()),
                stockEntry
        );

        StockDataManager.insertStockEntry(stockEntry);
        purchaseEntry.setStockEntry(StockDataManager.getStockEntryBySymbol(stockEntry.getSymbol()));
        if (purchaseEntry.getStockEntry() != null) {
            StockDataManager.insertStockPriceEntry(new StockPriceEntry(
                    purchaseEntry.getStockEntry(),
                    purchaseEntry.getUnitPrice(),
                    purchaseEntry.getCurrency(),
                    LocalDateTime.now().toString()
            ));
            StockDataManager.insertStockPurchaseEntry(purchaseEntry);
        } else {
            System.out.println("Stock wasn't inserted?");
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
