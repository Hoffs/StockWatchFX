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
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.converter.BigDecimalStringConverter;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
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
    public Pane mainPane;

    private String oldSymbolValue = "";
    private Timeline timeline;
    private String lastCurrency;

    @FXML
    public void initialize() {
        mainBox.prefHeightProperty().bind(mainPane.heightProperty());
        mainBox.prefWidthProperty().bind(mainPane.widthProperty());
        buttonsBox.getStyleClass().add("button-box");

        overallField.setTextFormatter(new TextFormatter<>(new BigDecimalStringConverter()));

        symbolField.setOnKeyReleased(e -> verifySymbolEvent());
        priceField.setOnKeyReleased(e -> updateOverallPrice());
        quantityField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (quantityField.getText().length() == 0) return;
            if (BigInteger.valueOf(Long.parseLong(quantityField.getText())).compareTo(BigInteger.ZERO) < 0)
                quantityField.setText("0");
            updateOverallPrice();
        });

        buttonClose.setOnAction(this::onClose);
        buttonAccept.setOnAction(this::onSave);

        addValidations();
    }

    private void addValidations() {
        priceField.textProperty().addListener((observable, oldValue, newValue) ->
                priceField.setText(ErrorManager.checkPriceField(priceField.getText())));
        quantityField.textProperty().addListener((observable, oldValue, newValue) ->
                quantityField.setText(ErrorManager.checkQuantityField(quantityField.getText())));
        symbolField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (symbolField.getText() != null && symbolField.getText().length() > 12) {
                symbolField.setText(symbolField.getText(0, 12));
            }
        });
        companyField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (companyField.getText() != null && companyField.getText().length() > 40) {
                companyField.setText(companyField.getText(0, 40));
            }
        });
    }

    private boolean checkErrors() {
        ErrorManager.clearErrorNodes(mainPane);
        boolean hasErrors = false;
        if (quantityField.getText() != null && quantityField.getText().equals("0")) {
            ErrorManager.addError("Negalima pridėti 0-io akcijų!", quantityField, mainPane);
            hasErrors = true;
        }
        if (symbolField.getText() == null || symbolField.getText().isEmpty()) {
            ErrorManager.addError("Neužpildytas simbolio laukas!", symbolField, mainPane);
            hasErrors = true;
        }
        if (companyField.getText() == null || companyField.getText().isEmpty()) {
            ErrorManager.addError("Neužpildytas įmonės laukas!", companyField, mainPane);
            hasErrors = true;
        }
        if (quantityField.getText() == null || quantityField.getText().isEmpty()) {
            ErrorManager.addError("Neužpildytas kiekio laukas!", quantityField, mainPane);
            hasErrors = true;
        }
        if (priceField.getText() == null || priceField.getText().isEmpty()) {
            ErrorManager.addError("Neužpildytas akcijos kainos laukas!", priceField, mainPane);
            hasErrors = true;
        }
        return hasErrors;
    }

    private void updateOverallPrice() {
        if (!quantityField.getText().isEmpty()) {
            BigDecimal quantity;
            try {
                quantity = BigDecimal.valueOf(Long.parseLong(quantityField.getText()));
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
            if (currentStock == null || !currentStock.isValid()) throw new IOException();
            fillOtherFields(currentStock);
        } catch (IOException e) {
            verifySymbolFallback();
        }
    }

    private void verifySymbolFallback() {
        try {
            SimpleStock currentStock = YahooFinanceWrapper.getSimpleStock(symbolField.getText());
            fillOtherFields(currentStock);
        } catch (IOException ignored) {
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
        priceField.setText((stock.getPrice() != null) ? stock.getPrice() : "0");
        updateOverallPrice();
    }

    private boolean saveStock() {
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
            return true;
        } else {
            System.out.println("Stock wasn't inserted?");
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
