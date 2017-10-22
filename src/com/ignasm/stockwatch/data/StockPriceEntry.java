package com.ignasm.stockwatch.data;

/**
 * Ignas Maslinskas
 * 20153209
 * PRIf-15/1
 */
public class StockPriceEntry {
    private StockEntry stockEntry;
    private double price;
    private String date; // Maybe change to actual date?

    public StockPriceEntry(StockEntry stockEntry, double price, String date) {
        this.stockEntry = stockEntry;
        this.price = price;
        this.date = date;
    }

    public StockEntry getStockEntry() {
        return stockEntry;
    }

    public double getPrice() {
        return price;
    }

    public String getDate() {
        return date;
    }
}
