package com.ignasm.stockwatch.data;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Ignas Maslinskas
 * 20153209
 * PRIf-15/1
 */
public class StockDataManager {

    public static StockPurchaseEntry[] getStockPurchaseEntries() {
        StockPurchaseEntry[] data = new StockPurchaseEntry[0];
        String query = "SELECT stock.id s_id, stock.symbol, stock.company_name, MAX(stock_purchases.id) sa_id, SUM(stock_purchases.share_change) share_change, SUM(stock_purchases.net_change) net_change, stock_purchases.price, MAX(stock_purchases.date) date, stock_purchases.currency " +
                "FROM stock_purchases " +
                "JOIN stock ON stock.id = stock_purchases.stock " +
                "GROUP BY stock.id";
        try {
            data = getStockPurchaseEntries(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static StockPurchaseEntry[] getStockPurchaseEntries(String dateFrom, String dateTo) {
        StockPurchaseEntry[] data = new StockPurchaseEntry[0];
        String query = "SELECT stock.id s_id, stock.symbol, stock.company_name, MAX(stock_purchases.id) sa_id, SUM(stock_purchases.share_change) share_change, SUM(stock_purchases.net_change) net_change, stock_purchases.price, MAX(stock_purchases.date) date, stock_purchases.currency " +
                "FROM stock_purchases " +
                "JOIN stock ON stock.id = stock_purchases.stock " +
                String.format("WHERE DATE(stock_purchases.date) >= '%s' AND DATE(stock_purchases.date) <= '%s' ", dateFrom, dateTo) +
                "GROUP BY stock.id ";
        try {
            data = getStockPurchaseEntries(query);
            HashMap<StockPurchaseEntry, Double> purchaseProfitMap = new HashMap<>();
            for (StockPurchaseEntry entry : data) {
                String key = getLatestPriceDifference(entry.getStockEntry());
                Double keyDouble = (key.startsWith("+")) ? Double.valueOf(key.replace("+", "")) : Double.valueOf(key);
                purchaseProfitMap.put(entry, keyDouble);
            }

            return purchaseProfitMap.entrySet()
                    .stream()
                    .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                    .limit(5)
                    .map(Map.Entry::getKey)
                    .toArray(StockPurchaseEntry[]::new);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    private static StockPurchaseEntry[] getStockPurchaseEntries(String query) throws SQLException {
        ResultSet resultSet = DatabaseUtility.executeQueryStatement(query);
        LinkedList<StockPurchaseEntry> activityList = new LinkedList<>();
        while(resultSet.next()) {
            StockEntry stockEntry = new StockEntry(resultSet.getInt("s_id"), resultSet.getString("company_name"), resultSet.getString("symbol"));
            if (resultSet.getDouble("share_change") > 0.0) {
                activityList.add(
                        new StockPurchaseEntry(
                                resultSet.getInt("sa_id"),
                                resultSet.getInt("share_change"),
                                resultSet.getDouble("price"),
                                resultSet.getString("currency"),
                                resultSet.getDouble("net_change"),
                                stockEntry
                        )
                );
            }
        }
        return activityList.toArray(new StockPurchaseEntry[activityList.size()]);
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

    public static String getLatestPriceDifference(StockEntry stock) {
        StockPriceEntry priceEntry = getLatestStockPrice(stock);
        double latestPrice = 0.0;
        if (priceEntry != null) {
            latestPrice = priceEntry.getPrice();
        }
        double latestPurchasePrice = getLastPurchasePrice(stock);
        String formattedString = new DecimalFormat("#0.000").format((latestPrice - latestPurchasePrice)).replace(',', '.');
        if (!formattedString.startsWith("-")) formattedString = "+".concat(formattedString);
        return formattedString;
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
        String query = String.format("SELECT stock_price.price, stock_price.currency, stock_price.date FROM stock_price WHERE stock = %d ORDER BY DATE DESC LIMIT 1", stock.getId());
        try {
            ResultSet resultSet = DatabaseUtility.executeQueryStatement(query);
            if (resultSet.next()) {
                return new StockPriceEntry(stock, resultSet.getDouble("price"), resultSet.getString("currency"), resultSet.getString("date"));
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
        String query = "SELECT stock_price.price, stock_price.date, stock_price.currency, stock.id, stock.company_name, stock.symbol FROM stock_price";
        try {
            ResultSet resultSet = DatabaseUtility.executeQueryStatement(query);
            LinkedList<StockPriceEntry> entries = new LinkedList<>();
            while (resultSet.next()) {
                StockEntry stockEntry = new StockEntry(resultSet.getInt("id"), resultSet.getString("company_name"), resultSet.getString("symbol"));
                entries.add(new StockPriceEntry(stockEntry, resultSet.getDouble("price"), resultSet.getString("currency"), resultSet.getString("date")));
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
                "INSERT INTO stock_purchases(stock, share_change, net_change, price, currency, date) " +
                        "SELECT %d, %s, %s, %s, '%s', '%s'",
                stockPurchaseEntry.getStockEntry().getId(),
                String.valueOf(stockPurchaseEntry.getShareChange()),
                String.valueOf(stockPurchaseEntry.getNetChange()),
                String.valueOf(stockPurchaseEntry.getUnitPrice()),
                stockPurchaseEntry.getCurrency(),
                String.valueOf(LocalDateTime.now())
        );

        try {
            DatabaseUtility.executeUpdateStatement(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertStockPriceEntry(StockPriceEntry stockPriceEntry) {
        String query = String.format("INSERT INTO stock_price(stock, price, currency, date) " +
                "SELECT %d, %s, '%s', '%s'",
                stockPriceEntry.getStockEntry().getId(),
                String.valueOf(stockPriceEntry.getPrice()),
                stockPriceEntry.getCurrency(),
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
            updateStockPrice(entry);
        }
    }

    private static void updateStockPrice(StockEntry entry) {
        try {
            Stock stock = YahooFinance.get(entry.getSymbol());
            System.out.println("hist = " + stock.getHistory().get(0));
            insertStockPriceEntry(new StockPriceEntry(
                    entry,
                    Double.parseDouble(stock.getQuote().getPrice().toString()),
                    stock.getCurrency(),
                    LocalDateTime.now().toString()
            ));
        } catch (IOException e) {
            System.out.println("Couldn't get stock information. Using fallback...");
            // e.printStackTrace();
            updateStockPriceFallback(entry);
        }
    }

    private static void updateStockPriceFallback(StockEntry entry) {
        try {
            SimpleStock stock = YahooFinanceWrapper.getSimpleStock(entry.getSymbol());
            insertStockPriceEntry(new StockPriceEntry(
                    entry,
                    Double.parseDouble(stock.getPrice()),
                    stock.getCurrency(),
                    LocalDateTime.now().toString()
            ));
            System.out.println("Fallback completed!");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Couldn't get stock information in fallback.");
        }
    }
}
