package com.example.Lib;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class WebRequests {
    // Cache responses for previously fetched API requests
    private final static HashMap<String, String> cache = new HashMap<>();

    public static String getText(String endpoint) throws IOException, URISyntaxException {
        return getText(endpoint, true);
    }

    public static String getText(String endpoint, boolean useCache) throws IOException, URISyntaxException {
        String cachedResp;
        if (useCache && (cachedResp = cache.get(endpoint)) != null) {
            return cachedResp;
        }
        URL url = new URI(endpoint).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        return receiveBufferString(endpoint, connection);
    }

    public static JSONObject postJson(String endpoint, String json) throws IOException, URISyntaxException {
        URL url = new URI(endpoint).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        // https://www.baeldung.com/httpurlconnection-post
        writeOutputBuffer(endpoint, json, connection);
        return new JSONObject(receiveBufferString(endpoint, connection));
    }
    public static JSONObject putJson(String endpoint, String json) throws IOException, URISyntaxException {
        URL url = new URI(endpoint).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        // https://www.baeldung.com/httpurlconnection-post
        writeOutputBuffer(endpoint, json, connection);
        return new JSONObject(receiveBufferString(endpoint, connection));
    }

    // Write the JSON into the output stream to send as the request body
    private static void writeOutputBuffer(String endpoint, String json, HttpURLConnection connection) throws IOException {
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        try (OutputStream outputStream = connection.getOutputStream()) {
            byte[] input = json.getBytes(StandardCharsets.UTF_8);
            outputStream.write(input, 0, input.length);
        }
    }

    // Get the string from the buffer stream
    private static String receiveBufferString(String endpoint, HttpURLConnection connection) throws IOException {
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


    public static JSONObject getJson(String endpoint) throws IOException, URISyntaxException {
        return new JSONObject(getText(endpoint));
    }
    public static JSONArray getArray(String endpoint) throws IOException, URISyntaxException {
        return new JSONArray(getText(endpoint));
    }
}
