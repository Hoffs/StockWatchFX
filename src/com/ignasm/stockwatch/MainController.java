package com.ignasm.stockwatch;

import com.ignasm.stockwatch.data.StockDataManager;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSnackbar;
import com.sun.istack.internal.NotNull;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class MainController {
    public VBox mainBox;
    public HBox menuBox;
    public HBox profitsBox;

    public VBox tableBox;
    public HBox filterBox;
    public TableView dataTable;
    public TableColumn symbolColumn;
    public TableColumn companyColumn;
    public TableColumn priceColumn;
    public TableColumn quantityColumn;
    public TableColumn removeColumn;

    public JFXButton addStockButton;
    public JFXButton helpButton;

    private JFXSnackbar snackbar; //         snackbar.enqueue();

    @FXML
    public void initialize() {

        snackbar = new JFXSnackbar(mainBox);
        mainBox.getStyleClass().add("main-window");
        menuBox.getStyleClass().add("button-box");
        tableBox.getStyleClass().add("data-card");
        filterBox.getStyleClass().add("table-filter-box");
        dataTable.getStyleClass().add("data-table");
        setupButtons(helpButton, addStockButton);
        setupColumns();

        StockDataManager.getStockActivityEntries();
    }

    private void setupButtons(JFXButton... buttons) {
        for (JFXButton button : buttons) {
            button.getStyleClass().add("button-raised");
        }
    }

    private void setupColumns() {
        symbolColumn.setResizable(false);
        symbolColumn.prefWidthProperty().bind(dataTable.widthProperty().multiply(0.15));
        companyColumn.setResizable(false);
        companyColumn.prefWidthProperty().bind(dataTable.widthProperty().multiply(0.25));
        quantityColumn.setResizable(false);
        quantityColumn.prefWidthProperty().bind(dataTable.widthProperty().multiply(0.2));
        priceColumn.setResizable(false);
        priceColumn.prefWidthProperty().bind(dataTable.widthProperty().multiply(0.2));
        removeColumn.setResizable(false);
        removeColumn.prefWidthProperty().bind(dataTable.widthProperty().multiply(0.2));
    }
}
