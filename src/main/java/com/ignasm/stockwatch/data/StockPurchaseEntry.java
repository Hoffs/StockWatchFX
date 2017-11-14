package com.ignasm.stockwatch.data;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Ignas Maslinskas
 * 20153209
 * PRIf-15/1
 */
public class StockPurchaseEntry {
    private StockEntry stockEntry;
    private int id; // id of entry
    private int shareChange; // Amounts of stockEntry that got traded (+ if bought shares, - if sold shares)
    private double netChange; // The price equivelent of the stockEntry trade. (+ if stockEntry was sold (ideally), - if stockEntry was bought)
    private double unitPrice;
    private String currency;

    private final SimpleIntegerProperty shareChangeProperty;
    private final SimpleDoubleProperty netChangeProperty;
    private final SimpleDoubleProperty unitPriceProperty;

    public StockPurchaseEntry(int id, int shareAmount, double unitPrice, String currency, double net, StockEntry stockEntry) {
        this.id = id;
        this.unitPrice = unitPrice;
        this.currency = currency;
        this.stockEntry = stockEntry;
        shareChange = shareAmount;
        netChange = net;

        shareChangeProperty = new SimpleIntegerProperty(shareChange);
        netChangeProperty = new SimpleDoubleProperty(netChange);
        unitPriceProperty = new SimpleDoubleProperty(this.unitPrice);
    }

    public StockEntry getStockEntry() {
        return stockEntry;
    }

    public int getId() {
        return id;
    }

    public int getShareChange() {
        return shareChange;
    }

    public double getNetChange() {
        return netChange;
    }

    public void setStockEntry(StockEntry stockEntry) {
        this.stockEntry = stockEntry;
    }

    public int getShareChangeProperty() {
        return shareChangeProperty.get();
    }
    public double getNetChangeProperty() {
        return netChangeProperty.get();
    }

    public double getUnitPriceProperty() {
        return unitPriceProperty.get();
    }

    public String getCompanyProperty() {
        return stockEntry.getCompanyProperty();
    }

    public String getSymbolProperty() {
        return stockEntry.getSymbolProperty();
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public String getCurrency() {
        return currency;
    }
}
