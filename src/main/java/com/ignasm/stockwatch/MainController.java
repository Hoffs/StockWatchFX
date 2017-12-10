package com.ignasm.stockwatch;

import com.ignasm.stockwatch.data.StockDataManager;
import com.ignasm.stockwatch.data.StockPriceEntry;
import com.ignasm.stockwatch.data.StockPurchaseEntry;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.DecimalFormat;

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
    public JFXTextField stockFilterField;
    public JFXDatePicker datePickerStart;
    public JFXDatePicker datePickerEnd;
    public JFXButton filterButton;

    public TableView<StockPurchaseEntry> dataTable;
    public TableColumn<StockPurchaseEntry, String> symbolColumn;
    public TableColumn<StockPurchaseEntry, String> companyColumn;
    public TableColumn<StockPurchaseEntry, Double> quantityColumn;
    public TableColumn<StockPurchaseEntry, String> priceColumn;
    public TableColumn<StockPurchaseEntry, String> changeColumn;
    public TableColumn<StockPurchaseEntry, Node> removeColumn;

    public JFXButton refreshButton;
    public JFXButton addStockButton;
    public JFXButton helpButton;
    public ImageView logoImage;
    public Pane mainPane;
    public JFXButton resetButton;

    private ObservableList<StockPurchaseEntry> activityEntries = FXCollections.observableArrayList();

    // private JFXSnackbar snackbar; //         snackbar.enqueue();
    private Thread stockUpdateThread;

    @FXML
    public void initialize() {
        mainBox.prefWidthProperty().bind(mainPane.widthProperty());
        mainBox.prefHeightProperty().bind(mainPane.heightProperty());

        setupValidation();
        // datePickerStart.
        // snackbar = new JFXSnackbar(mainBox);
        setupLogoImage();
        setupCss();
        setupColumns();
        updateSavedStockPrices();
        updateProfit();

        prepareTable();
        updateData();

        refreshButton.setRipplerFill(addStockButton.getRipplerFill());

        addStockButton.setOnAction(e -> openAddStockWindow());
        filterButton.setOnAction(e -> updateUI());
        refreshButton.setOnAction(e -> updateSavedStockPrices());
        helpButton.setOnAction(e -> openHelpWindow());
        resetButton.setOnAction(e -> clearFilters());
        StockDataManager.getStockPurchaseEntries();
    }

    private void clearFilters() {
        datePickerStart.setValue(null);
        datePickerEnd.setValue(null);
        stockFilterField.setText(null);
        updateData();
    }

    private void setupValidation() {
        datePickerEnd.focusedProperty().addListener(this::checkListener);
        datePickerStart.focusedProperty().addListener(this::checkListener);
        filterButton.focusedProperty().addListener(this::checkListener);
        stockFilterField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (stockFilterField.getText() != null && stockFilterField.getText().length() > 40) {
                stockFilterField.setText(stockFilterField.getText(0, 40));
            }
        });
    }

    private void checkListener(ObservableValue<? extends Boolean> observable, Boolean oldVal, Boolean newVal) {
        checkValidationErrors();
    }

    private void checkValidationErrors() {
        ErrorManager.clearErrorNodes(mainPane);

        if (datePickerEnd.getValue() != null && datePickerStart.getValue() == null) {
            ErrorManager.addError("Neįvesta pradžios data!", datePickerStart, mainPane);
        }

        if (datePickerStart.getValue() != null && datePickerEnd.getValue() == null) {
            ErrorManager.addError("Neįvesta pabaigos data!", datePickerEnd, mainPane);
        }

        if (datePickerEnd.getValue() != null && datePickerStart.getValue() != null && datePickerStart.getValue().isAfter(datePickerEnd.getValue())) {
            ErrorManager.addError("Pradžios data negali būti vėlesnė už pabaigos!", datePickerStart, mainPane);
        }
    }

    // private double

    private void setupLogoImage() {
        logoImage.setImage(new Image(getClass().getClassLoader().getResourceAsStream("logo120.gif")));
        logoImage.setFitHeight(120.0);

        logoImage.setFitWidth(120.0);
    }

    private void openHelpWindow() {
        Parent root;
        try {
            Stage newStage = new Stage();
            root = FXMLLoader.load(getClass().getClassLoader().getResource("HelpWindow.fxml"));
            newStage.setScene(new Scene(root, 400, 540));
            newStage.getScene().getStylesheets().add("stylesheet.css");
            newStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("logo40.png")));
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openAddStockWindow() {
        Parent root;
        try {
            Stage newStage = new Stage();
            root = FXMLLoader.load(getClass().getClassLoader().getResource("AddStock.fxml"));
            newStage.setScene(new Scene(root, 300, 370));
            newStage.getScene().getStylesheets().add("stockStylesheet.css");
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
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("RemoveStock.fxml"));
            RemoveStockController controller = new RemoveStockController(purchaseEntry);
            loader.setController(controller);

            Stage newStage = new Stage();
            root = loader.load();

            newStage.setScene(new Scene(root, 300, 310));
            newStage.getScene().getStylesheets().add("stockStylesheet.css");
            newStage.setOnHiding(e -> updateUI());
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupCss() {
        datePickerStart.setDefaultColor(Color.web("#2196F3"));
        datePickerEnd.setDefaultColor(Color.web("#2196F3"));
        stockFilterField.setFocusColor(Color.web("#2196F3"));
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
            Double price = StockDataManager.getLastPurchasePrice(c.getValue().getStockEntry());
            return new ReadOnlyObjectWrapper<>(new DecimalFormat("#0.000").format(price).replace(',', '.').concat(" " + c.getValue().getCurrency()));
        });

        changeColumn.setCellValueFactory(c -> {
            StockPriceEntry latestStockPrice = StockDataManager.getLatestStockPrice(c.getValue().getStockEntry());
            String latestPrice = (latestStockPrice != null) ? String.valueOf(new DecimalFormat("#0.000").format(latestStockPrice.getPrice()).replace(',', '.')) : "0.000";
            String changeString = String.format("%s (%s)",
                    latestPrice,
                    StockDataManager.getLatestPriceDifference(c.getValue().getStockEntry())
            );
            return new ReadOnlyObjectWrapper<>(changeString + " " + c.getValue().getCurrency());
        });
        changeColumn.setCellFactory((TableColumn<StockPurchaseEntry, String> param) -> new TableCell<StockPurchaseEntry, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                this.setText(item);
                this.getStyleClass().remove("negative-profit");
                this.getStyleClass().remove("positive-profit");
                if (item != null && item.contains("-")) {
                    this.getStyleClass().add("negative-profit");
                } else if (item != null && item.contains("+")) {
                    this.getStyleClass().add("positive-profit");
                }
            }
        });

        changeColumn.setComparator((a, b) -> {
            String aString = a.split(" ")[1].replace("(", "").replace(")", "").replace("+", "");
            String bString = b.split(" ")[1].replace("(", "").replace(")", "").replace("+", "");

            return Double.compare(Double.parseDouble(bString), Double.parseDouble(aString));
        });
        removeColumn.setCellValueFactory(c -> {
            JFXButton button = new JFXButton("Panaikinti");
            button.getStyleClass().add("remove-button");
            button.setOnAction(e -> openRemoveStockWindow(c.getValue()));
            return new ReadOnlyObjectWrapper<>(button);
        });
    }

    private void updateData() {
        activityEntries.clear();
        StockPurchaseEntry[] purchaseEntries;
        if (datePickerStart.getValue() != null && datePickerEnd.getValue() != null && datePickerEnd.getValue().isAfter(datePickerStart.getValue())) {
            purchaseEntries = StockDataManager.getStockPurchaseEntries(datePickerStart.getValue().toString(), datePickerEnd.getValue().toString());
        } else {
            purchaseEntries = StockDataManager.getStockPurchaseEntries();
        }
        for (StockPurchaseEntry entry : purchaseEntries) {
            if (symbolCompanyFilter(entry.getStockEntry().getSymbol()) || symbolCompanyFilter(entry.getStockEntry().getCompany())) {
                activityEntries.add(entry);
            }
        }
    }

    private boolean symbolCompanyFilter(String name) {
        return stockFilterField.getText() == null || (stockFilterField.getText() != null
                && (name.toLowerCase().contains(stockFilterField.getText().toLowerCase()))
                || stockFilterField.getText().isEmpty());
    }

    private void updateProfit() {
        String profits = StockDataManager.getProfit();
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
        if (stockUpdateThread == null || !stockUpdateThread.isAlive()) {
            stockUpdateThread = new Thread(new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    StockDataManager.updateStockPrices();
                    updateUI();
                    return null;
                }
            });
            stockUpdateThread.start();
        } else {
            System.out.println("Update already running...");
        }
    }

    private void updateUI() {
        updateData();
        updateProfit();
    }
}
