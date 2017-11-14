package com.ignasm.stockwatch.data;

public class SimpleStock {
    private final String currency;
    private final String price;
    private final String companyName;

    SimpleStock(String companyName, String currency, String price) {
        this.companyName = companyName;
        this.currency = currency;
        this.price = price;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPrice() {
        return price;
    }
}
