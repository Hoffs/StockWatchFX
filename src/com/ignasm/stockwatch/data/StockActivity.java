package com.ignasm.stockwatch.data;

/**
 * Ignas Maslinskas
 * 20153209
 * PRIf-15/1
 */
public class StockActivity {
    Stock stock;
    int id; // id of entry
    double shareChange; // Amounts of stock that got traded (+ if bought shares, - if sold shares)
    double netChange; // The price equivelent of the stock trade. (+ if stock was sold (ideally), - if stock was bought)

    public StockActivity(int id, double share, double net, Stock stock) {
        this.id = id;
        shareChange = share;
        netChange = net;
        this.stock = stock;
    }

    public Stock getStock() {
        return stock;
    }

    public int getId() {
        return id;
    }

    public double getShareChange() {
        return shareChange;
    }

    public double getNetChange() {
        return netChange;
    }
}
