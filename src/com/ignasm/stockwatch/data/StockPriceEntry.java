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
    private String currency;

    public StockPriceEntry(StockEntry stockEntry, double price, String currency, String date) {
        this.stockEntry = stockEntry;
        this.price = price;
        this.currency = currency;
        this.date = date;
    }

    public StockEntry getStockEntry() {
        return stockEntry;
    }

    public double getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDate() {
        return date;
    }
}
