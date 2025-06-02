package com.example.Lib;

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

    public static JSONObject getJson(String endpoint) throws IOException, URISyntaxException {
        return new JSONObject(getText(endpoint));
    }

    public static JSONObject postJson(String endpoint, String json) throws IOException, URISyntaxException {
        URL url = new URI(endpoint).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        writeOutputBuffer(connection, json);
        return new JSONObject(receiveBufferString(endpoint, connection));
    }
    public static JSONObject putJson(String endpoint, String json) throws IOException, URISyntaxException {
        URL url = new URI(endpoint).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        writeOutputBuffer(connection, json);
        return new JSONObject(receiveBufferString(endpoint, connection));
    }

    // Write the JSON into the output stream to send as the request body
    // https://www.baeldung.com/httpurlconnection-post
    private static void writeOutputBuffer(HttpURLConnection connection, String json) throws IOException {
        connection.setRequestProperty("Content-Type", "application/json"); // Set the request body type to JSON
        connection.setDoOutput(true); // Allow writing to the request body
        try (OutputStream outputStream = connection.getOutputStream()) {
            byte[] input = json.getBytes(StandardCharsets.UTF_8); // Convert the string to bytes
            outputStream.write(input, 0, input.length); // Write the bytes to the request body
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
}
