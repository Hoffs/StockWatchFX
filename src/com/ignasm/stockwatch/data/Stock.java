package com.ignasm.stockwatch.data;

/**
 * Ignas Maslinskas
 * 20153209
 * PRIf-15/1
 */
public class Stock {
    private int id;
    private String company;
    private String symbol;

    public Stock(int id, String company, String symbol) {
        this.id = id;
        this.company = company;
        this.symbol = symbol;
    }
}
