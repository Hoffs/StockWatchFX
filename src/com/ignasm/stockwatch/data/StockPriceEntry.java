package com.ignasm.stockwatch.data;

/**
 * Ignas Maslinskas
 * 20153209
 * PRIf-15/1
 */
public class StockPriceEntry {
    private Stock stock;
    private double price;
    private String date; // TODO: CHNAGE TO ACTUAL DATE

    public StockPriceEntry(Stock stock, double price, String date) {
        this.stock = stock;
        this.price = price;
        this.date = date;
    }

    public Stock getStock() {
        return stock;
    }

    public double getPrice() {
        return price;
    }

    public String getDate() {
        return date;
    }
}
