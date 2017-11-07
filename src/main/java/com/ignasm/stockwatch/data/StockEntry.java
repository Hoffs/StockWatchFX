package com.ignasm.stockwatch.data;

import javafx.beans.property.SimpleStringProperty;

/**
 * Ignas Maslinskas
 * 20153209
 * PRIf-15/1
 */
public class StockEntry {
    private int id;
    private String company;
    private String symbol;

    private final SimpleStringProperty companyProperty;
    private final SimpleStringProperty symbolProperty;

    public StockEntry(int id, String company, String symbol) {
        this.id = id;
        this.company = company;
        this.symbol = symbol;

        companyProperty = new SimpleStringProperty(company);
        symbolProperty = new SimpleStringProperty(symbol);
    }

    public int getId() {
        return id;
    }

    public String getCompany() {
        return company;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCompanyProperty() {
        return companyProperty.get();
    }

    public String getSymbolProperty() {
        return symbolProperty.get();
    }
}
