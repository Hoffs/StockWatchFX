package com.ignasm.stockwatch.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class YahooFinanceWrapper {
    private static final String crumbUrl = "https://query1.finance.yahoo.com/v1/test/getcrumb";

    public static SimpleStock getSimpleStock(String symbol) throws IOException {
        Map<String, String> data = getData(symbol);
        return new SimpleStock(
                data.get("company"),
                data.get("currency"),
                data.get("price")
        );
    }

    private static String getContents(String url) throws IOException {
        URL apiUrl = new URL(url);
        HttpURLConnection urlConnection = (HttpURLConnection) apiUrl.openConnection();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        StringBuilder resultData = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            resultData.append(line);
        }
        return resultData.toString();
    }

    private static Map<String, String> getData(String symbol) throws IOException {
        Map<String, String> data = new HashMap<>();
        String contents = getContents(getYahooURL(symbol));
        JsonObject jsonObject = new JsonParser().parse(contents).getAsJsonObject().getAsJsonObject("quoteResponse").getAsJsonArray("result").get(0).getAsJsonObject();
        try {
            data.put("currency", jsonObject.get("currency").getAsString());
            data.put("company", jsonObject.get("shortName").getAsString());
            data.put("price", jsonObject.get("regularMarketPrice").getAsJsonObject().get("raw").getAsString());
        } catch (NullPointerException ignored) {
        }
        return data;
    }

    private static String getYahooURL(String symbol) {
        return "https://query1.finance.yahoo.com/v7/finance/quote?formatted=true&crumb=" +
                getCrumb() +
                "&lang=en-US&region=US&symbols=" +
                symbol +
                "&fields=shortName%2CmarketCap%2CregularMarketPrice";
    }

    private static String getCrumb() {
        try {
            return requestCrumb();
        } catch (IOException e) {
            // e.printStackTrace();
            System.out.println("Couldn't get CRUMB");
        }
        return "";
    }

    private static String requestCrumb() throws IOException {
        // GET CRUMB
        URL url = new URL(crumbUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        BufferedReader urlReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String crumb = urlReader.readLine();
        urlReader.close();
        return crumb;
    }
}
