package com.example.Lib;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class WebRequests {
    // Cache responses for previously fetched API requests
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
        return bufferString(endpoint, connection);
    }

    public static String postJson(String endpoint, String json) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        // https://www.baeldung.com/httpurlconnection-post
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        try (OutputStream outputStream = connection.getOutputStream()) {
            byte[] input = json.getBytes(StandardCharsets.UTF_8);
            outputStream.write(input, 0, input.length);
        }
        return bufferString(endpoint, connection);
    }

    // Get the string from the buffer stream
    private static String bufferString(String endpoint, HttpURLConnection connection) throws IOException {
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
    public static JSONArray getArray(String endpoint) throws IOException {
        return new JSONArray(getText(endpoint));
    }
}
