package com.ignasm.stockwatch;

import com.ignasm.stockwatch.data.StockDataManager;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXSnackbar;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

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
        setupButtons(helpButton, addStockButton);
        setupCss();
        setupColumns();

        StockDataManager.getSavedStockActivityEntries();
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
