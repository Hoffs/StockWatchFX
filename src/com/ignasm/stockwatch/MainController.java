package com.ignasm.stockwatch;

import com.ignasm.stockwatch.data.StockDataManager;
import com.ignasm.stockwatch.data.StockPriceEntry;
import com.ignasm.stockwatch.data.StockPurchaseEntry;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.ReadOnlyObjectWrapper;
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

    private ObservableList<StockPurchaseEntry> activityEntries = FXCollections.observableArrayList();

    // private JFXSnackbar snackbar; //         snackbar.enqueue();
    private Thread stockUpdateThread;

    @FXML
    public void initialize() {
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
        StockDataManager.getStockPurchaseEntries();
    }

    private void setupLogoImage() {
        logoImage.setImage(new Image(getClass().getResourceAsStream("resources/logo120.gif")));
        logoImage.setFitHeight(120.0);

        logoImage.setFitWidth(120.0);
    }

    private void openHelpWindow() {
        Parent root;
        try {
            Stage newStage = new Stage();
            root = FXMLLoader.load(getClass().getResource("resources/HelpWindow.fxml"));
            newStage.setScene(new Scene(root, 400, 540));
            newStage.getScene().getStylesheets().add("com/ignasm/stockwatch/resources/stylesheet.css");
            newStage.getIcons().add(new Image(getClass().getResourceAsStream("resources/logo40.png")));
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
            root = FXMLLoader.load(getClass().getResource("resources/AddStock.fxml"));
            newStage.setScene(new Scene(root, 300, 330));
            newStage.getScene().getStylesheets().add("com/ignasm/stockwatch/resources/stockStylesheet.css");
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/RemoveStock.fxml"));
            RemoveStockController controller = new RemoveStockController(purchaseEntry);
            loader.setController(controller);

            Stage newStage = new Stage();
            root = loader.load();

            newStage.setScene(new Scene(root, 300, 330));
            newStage.getScene().getStylesheets().add("com/ignasm/stockwatch/resources/stockStylesheet.css");
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
        if (datePickerStart.getValue() != null && datePickerEnd.getValue() != null) {
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
        return stockFilterField.getText().isEmpty() || name.toLowerCase().contains(stockFilterField.getText().toLowerCase());
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
