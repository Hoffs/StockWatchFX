package com.ignasm.stockwatch.data;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.LinkedList;

/**
 * Ignas Maslinskas
 * 20153209
 * PRIf-15/1
 */
public class StockDataManager {

    public static StockPurchaseEntry[] getSavedStockActivityEntries() {
        StockPurchaseEntry[] data = new StockPurchaseEntry[0];
        String query = "SELECT stock.id s_id, stock.symbol, stock.company_name, MAX(stock_purchases.id) sa_id, SUM(stock_purchases.share_change) share_change, SUM(stock_purchases.net_change) net_change, stock_purchases.price, MAX(stock_purchases.date) date " +
                "FROM stock_purchases " +
                "JOIN stock ON stock.id = stock_purchases.stock " +
                "GROUP BY stock.id";
        try {
            ResultSet resultSet = DatabaseUtility.executeQueryStatement(query);
            LinkedList<StockPurchaseEntry> activityList = new LinkedList<>();
            while(resultSet.next()) {
                StockEntry stockEntry = new StockEntry(resultSet.getInt("s_id"), resultSet.getString("company_name"), resultSet.getString("symbol"));
                if (resultSet.getDouble("share_change") > 0.0 || resultSet.getDouble("share_change") == -1.0) {
                    activityList.add(
                            new StockPurchaseEntry(
                                    resultSet.getInt("sa_id"),
                                    resultSet.getInt("share_change"),
                                    resultSet.getDouble("price"),
                                    resultSet.getDouble("net_change"),
                                    stockEntry
                            )
                    );
                }
            }
            data = activityList.toArray(new StockPurchaseEntry[activityList.size()]);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static double getLastPurchasePrice(StockEntry stock) {
        String query = String.format("SELECT stock_purchases.price FROM stock_purchases WHERE stock_purchases.stock = %d ORDER BY stock_purchases.date DESC LIMIT 1", stock.getId());
        try {
            ResultSet resultSet = DatabaseUtility.executeQueryStatement(query);
            if (resultSet.next()) {
                return resultSet.getDouble("price");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public static double getLatestPriceDifference(StockEntry stock) {
        StockPriceEntry priceEntry = getLatestStockPrice(stock);
        double latestPrice = 0.0;
        if (priceEntry != null) {
            latestPrice = priceEntry.getPrice();
        }
        double latestPurchasePrice = getLastPurchasePrice(stock);
        return (latestPrice - latestPurchasePrice);
    }

    public static String getProfit() {
        String query = "SELECT SUM(stock_purchases.net_change) profit FROM stock_purchases";
        try {
            ResultSet resultSet = DatabaseUtility.executeQueryStatement(query);
            if (resultSet.next()) {
                return new DecimalFormat("#0.00000").format(resultSet.getDouble("profit")).replace(',', '.');
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "0.0";
    }

    public static StockPriceEntry getLatestStockPrice(StockEntry stock) {
        String query = String.format("SELECT stock_price.price, stock_price.date FROM stock_price WHERE stock = %d ORDER BY DATE DESC LIMIT 1", stock.getId());
        try {
            ResultSet resultSet = DatabaseUtility.executeQueryStatement(query);
            if (resultSet.next()) {
                return new StockPriceEntry(stock, resultSet.getDouble("price"), resultSet.getString("date"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static StockEntry[] getSavedStocks() {
        StockEntry[] data = new StockEntry[0];
        String query = "SELECT stock.id, stock.symbol, stock.company_name FROM stock";
        try {
            ResultSet resultSet = DatabaseUtility.executeQueryStatement(query);
            LinkedList<StockEntry> stockEntryList = new LinkedList<>();
            while (resultSet.next()) {
                stockEntryList.add(new StockEntry(resultSet.getInt("id"), resultSet.getString("company_name"), resultSet.getString("symbol")));
            }
            data = stockEntryList.toArray(new StockEntry[stockEntryList.size()]);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static StockPriceEntry[] getSavedStockPrices() {
        StockPriceEntry[] data = new StockPriceEntry[0];
        String query = "SELECT stock_price.price, stock_price.date, stock.id, stock.company_name, stock.symbol FROM stock_price";
        try {
            ResultSet resultSet = DatabaseUtility.executeQueryStatement(query);
            LinkedList<StockPriceEntry> entries = new LinkedList<>();
            while (resultSet.next()) {
                StockEntry stockEntry = new StockEntry(resultSet.getInt("id"), resultSet.getString("company_name"), resultSet.getString("symbol"));
                entries.add(new StockPriceEntry(stockEntry, resultSet.getDouble("price"), resultSet.getString("date")));
            }
            data = entries.toArray(new StockPriceEntry[entries.size()]);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static StockEntry getStockEntryBySymbol(String symbol) {
        String query = String.format("SELECT stock.id, stock.company_name, stock.symbol FROM stock WHERE symbol = '%s'", symbol);
        try {
            ResultSet resultSet = DatabaseUtility.executeQueryStatement(query);
            if (resultSet.next()) {
                return new StockEntry(
                        resultSet.getInt("id"),
                        resultSet.getString("company_name"),
                        resultSet.getString("symbol")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static void insertStockEntry(StockEntry stockEntry) {
        String query = String.format(
                "INSERT INTO stock(company_name, symbol) SELECT '%s', '%s' WHERE NOT EXISTS (SELECT 1 FROM stock WHERE symbol = '%s')",
                stockEntry.getCompany(),
                stockEntry.getSymbol(),
                stockEntry.getSymbol()
        );

        try {
            DatabaseUtility.executeUpdateStatement(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertStockPurchaseEntry(StockPurchaseEntry stockPurchaseEntry) {
        String query = String.format(
                "INSERT INTO stock_purchases(stock, share_change, net_change, price, date) " +
                        "SELECT %d, %s, %s, %s, '%s'",
                stockPurchaseEntry.getStockEntry().getId(),
                String.valueOf(stockPurchaseEntry.getShareChange()),
                String.valueOf(stockPurchaseEntry.getNetChange()),
                String.valueOf(stockPurchaseEntry.getUnitPrice()),
                String.valueOf(LocalDateTime.now())
        );

        try {
            DatabaseUtility.executeUpdateStatement(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertStockPriceEntry(StockPriceEntry stockPriceEntry) {
        String query = String.format("INSERT INTO stock_price(stock, price, date) " +
                "SELECT %d, %s, '%s'",
                stockPriceEntry.getStockEntry().getId(),
                String.valueOf(stockPriceEntry.getPrice()),
                stockPriceEntry.getDate()
        );
        try {
            DatabaseUtility.executeUpdateStatement(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateStockPrices() {
        StockEntry[] stockEntries = getSavedStocks();
        for (StockEntry entry : stockEntries) {
            try {
                Stock stock = YahooFinance.get(entry.getSymbol());
                insertStockPriceEntry(new StockPriceEntry(
                        entry,
                        Double.parseDouble(stock.getQuote().getPrice().toString()),
                        LocalDateTime.now().toString()
                ));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
