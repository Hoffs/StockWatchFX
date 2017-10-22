package com.ignasm.stockwatch;

import com.ignasm.stockwatch.data.StockDataManager;
import com.ignasm.stockwatch.data.StockPriceEntry;
import com.ignasm.stockwatch.data.StockPurchaseEntry;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXSnackbar;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {
    public VBox mainBox;
    public HBox menuBox;

    public HBox profitsBox;
    public VBox vProfitsBox;
    public Label profitLabel;
    public Text profitText;

    public VBox tableBox;

    public HBox filterBox;
    public Label tableLabel;
    public JFXDatePicker datePickerStart;
    public JFXDatePicker datePickerEnd;
    public JFXButton filterButton;

    public TableView<StockPurchaseEntry> dataTable;
    public TableColumn<StockPurchaseEntry, String> symbolColumn;
    public TableColumn<StockPurchaseEntry, String> companyColumn;
    public TableColumn<StockPurchaseEntry, Double> quantityColumn;
    public TableColumn<StockPurchaseEntry, Double> priceColumn;
    public TableColumn<StockPurchaseEntry, Double> changeColumn;
    public TableColumn<StockPurchaseEntry, Node> removeColumn;

    public JFXButton addStockButton;
    public JFXButton helpButton;

    private ObservableList<StockPurchaseEntry> activityEntries = FXCollections.observableArrayList();

    private JFXSnackbar snackbar; //         snackbar.enqueue();

    @FXML
    public void initialize() {
        snackbar = new JFXSnackbar(mainBox);
        setupButtons(helpButton, addStockButton);
        setupCss();
        setupColumns();
        updateSavedStockPrices();
        updateProfit();

        prepareTable();
        updateData();

        addStockButton.setOnAction(e -> openAddStockWindow());

        StockDataManager.getSavedStockActivityEntries();
    }

    private void openAddStockWindow() {
        Parent root;
        try {
            Stage newStage = new Stage();
            root = FXMLLoader.load(getClass().getResource("AddStock.fxml"));
            newStage.setScene(new Scene(root, 300, 330));
            newStage.getScene().getStylesheets().add("com/ignasm/stockwatch/stockStylesheet.css");
            newStage.setOnHiding(e -> updateUI());
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openRemoveStockWindow(StockPurchaseEntry purchaseEntry) {
        Parent root;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RemoveStock.fxml"));
            RemoveStockController controller = new RemoveStockController(purchaseEntry);
            loader.setController(controller);

            Stage newStage = new Stage();
            root = loader.load();

            newStage.setScene(new Scene(root, 300, 330));
            newStage.getScene().getStylesheets().add("com/ignasm/stockwatch/stockStylesheet.css");
            newStage.setOnHiding(e -> updateUI());
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupButtons(JFXButton... buttons) {
        for (JFXButton button : buttons) {
            button.getStyleClass().add("button-raised");
        }
    }

    private void setupCss() {
        mainBox.getStyleClass().add("main-window");
        menuBox.getStyleClass().add("button-box");
        tableBox.getStyleClass().add("data-card");
        filterBox.getStyleClass().add("table-filter-box");
        tableLabel.getStyleClass().add("table-label");
        dataTable.getStyleClass().add("data-table");
        filterButton.getStyleClass().add("filter-button");
        vProfitsBox.getStyleClass().add("data-card");
        vProfitsBox.getStyleClass().add("profits-card");
        datePickerStart.setDefaultColor(Color.web("#2196F3"));
        datePickerEnd.setDefaultColor(Color.web("#2196F3"));
        profitLabel.getStyleClass().add("profits-label");
        profitText.getStyleClass().add("profits-text");
    }

    private void setupColumns() {
        symbolColumn.setResizable(false);
        symbolColumn.getStyleClass().add("symbol-col");
        symbolColumn.prefWidthProperty().bind(dataTable.widthProperty().multiply(0.15));

        companyColumn.setResizable(false);
        companyColumn.getStyleClass().add("company-col");
        companyColumn.prefWidthProperty().bind(dataTable.widthProperty().multiply(0.25));

        quantityColumn.setResizable(false);
        quantityColumn.getStyleClass().add("quantity-col");
        quantityColumn.prefWidthProperty().bind(dataTable.widthProperty().multiply(0.075));

        priceColumn.setResizable(false);
        priceColumn.getStyleClass().add("price-col");
        priceColumn.prefWidthProperty().bind(dataTable.widthProperty().multiply(0.1625));

        changeColumn.setResizable(false);
        changeColumn.getStyleClass().add("price-col");
        changeColumn.prefWidthProperty().bind(dataTable.widthProperty().multiply(0.1625));

        removeColumn.setResizable(false);
        removeColumn.getStyleClass().add("remove-col");
        removeColumn.prefWidthProperty().bind(dataTable.widthProperty().multiply(0.2));
    }

    private void prepareTable() {
        dataTable.setItems(activityEntries);
        symbolColumn.setCellValueFactory(new PropertyValueFactory<>("symbolProperty"));
        companyColumn.setCellValueFactory(new PropertyValueFactory<>("companyProperty"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("shareChangeProperty"));
        priceColumn.setCellValueFactory(c -> {
            Double price;
            StockPriceEntry stockPriceEntry = StockDataManager.getLatestStockPrice(c.getValue().getStockEntry());
            price = stockPriceEntry != null ? stockPriceEntry.getPrice() : 0;
            return new ReadOnlyObjectWrapper<>(price);
        });

        changeColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(StockDataManager.getLatestPriceDifference(c.getValue().getStockEntry())));

        removeColumn.setCellValueFactory(c -> {
            JFXButton button = new JFXButton("Panaikinti");
            button.getStyleClass().add("remove-button");
            button.setOnAction(e -> openRemoveStockWindow(c.getValue()));
            return new ReadOnlyObjectWrapper<>(button);
        });
    }

    private void updateData() {
        activityEntries.clear();
        activityEntries.addAll(StockDataManager.getSavedStockActivityEntries());
    }

    private void updateProfit() {
        String profits = StockDataManager.getProfit();
        System.out.println("Profits: " + profits);
        profitText.getStyleClass().remove("negative-profit");
        profitText.getStyleClass().remove("positive-profit");

        if (profits.startsWith("-")) {
            profitText.getStyleClass().add("negative-profit");
        } else {
            profitText.getStyleClass().add("positive-profit");
        }
        profits = profits.substring(0, profits.indexOf(".") + 4);
        profitText.setText(profits);
    }

    private void updateSavedStockPrices() {
        StockDataManager.updateStockPrices();
    }

    private void updateUI() {
        updateData();
        updateProfit();
    }
}
