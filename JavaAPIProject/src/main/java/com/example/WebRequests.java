package com.example;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class WebRequests {
    private final static HashMap<String, String> cache = new HashMap<>();

    public static String getText(String endpoint) throws IOException {
        return getText(endpoint, true);
    }

    public static String getText(String endpoint, boolean useCache) throws IOException {
        String cachedResp;
        if (useCache && (cachedResp = cache.get(endpoint)) != null) {
            return cachedResp;
        }
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        BufferedReader buffer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String bufferLine;
        StringBuilder res = new StringBuilder();
        while ((bufferLine = buffer.readLine()) != null) {
            res.append(bufferLine);
        }
        buffer.close();
        connection.disconnect();
        cache.put(endpoint, res.toString());
        return res.toString();
    }

    public static JSONObject getJson(String endpoint) throws IOException {
        return new JSONObject(getText(endpoint));
    }
}
