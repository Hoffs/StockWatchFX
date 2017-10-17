package com.ignasm.stockwatch.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

/**
 * Ignas Maslinskas
 * 20153209
 * PRIf-15/1
 */
public class StockDataManager {

    public static StockActivity[] getStockActivityEntries() {
        StockActivity[] data = new StockActivity[0];
        String query = "SELECT stock.id s_id, stock.symbol, stock.company_name, stock_activity.id sa_id, stock_activity.share_change, stock_activity.net_change, stock_activity.date FROM stock_activity JOIN stock ON stock.id = stock_activity.stock";
        try {
            ResultSet resultSet = DatabaseUtility.executeQueryStatement(query);
            LinkedList<StockActivity> activityList = new LinkedList<>();
            while(resultSet.next()) {
                Stock stock = new Stock(resultSet.getInt("s_id"), resultSet.getString("company_name"), resultSet.getString("symbol"));
                activityList.add(new StockActivity(resultSet.getInt("sa_id"), resultSet.getDouble("share_change"), resultSet.getDouble("net_change"), stock));
            }
            data = activityList.toArray(new StockActivity[activityList.size()]);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static Stock[] getCachedStocks() {
        Stock[] data = new Stock[0];
        String query = "SELECT stock.id, stock.symbol, stock.company_name";
        try {
            ResultSet resultSet = DatabaseUtility.executeQueryStatement(query);
            LinkedList<Stock> stockList = new LinkedList<>();
            while (resultSet.next()) {
                stockList.add(new Stock(resultSet.getInt("id"), resultSet.getString("company_name"), resultSet.getString("symbol")));
            }
            data = stockList.toArray(new Stock[stockList.size()]);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static StockPriceEntry[] getCachedStockPrices() {
        StockPriceEntry[] data = new StockPriceEntry[0];
        String query = "SELECT stock_price.price, stock_price.date, stock.id, stock.company_name, stock.symbol FROM stock_price";
        try {
            ResultSet resultSet = DatabaseUtility.executeQueryStatement(query);
            LinkedList<StockPriceEntry> entries = new LinkedList<>();
            while (resultSet.next()) {
                Stock stock = new Stock(resultSet.getInt("id"), resultSet.getString("company_name"), resultSet.getString("symbol"));
                entries.add(new StockPriceEntry(stock, resultSet.getDouble("price"), resultSet.getString("date")));
            }
            data = entries.toArray(new StockPriceEntry[entries.size()]);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    // TODO: Implement these
    
    public static void insertNewStock(Stock stock) {}

    public static void insertNewStockActivity(StockActivity stockActivity) {}

    public static void insertNewStockPriceEntry(StockPriceEntry stockPriceEntry) {}
}
