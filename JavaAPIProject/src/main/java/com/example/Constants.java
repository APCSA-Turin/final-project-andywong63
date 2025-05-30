package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Constants {
    public static final String POKEAPI_BASE = "https://pokeapi.co/api/v2";
    public static final String GENERATION = "red-blue";
    public static final String SERVER_API_BASE = "http://localhost:4321";
    public static final String WS_API_BASE = "ws://localhost:4321";
    public static final ObjectMapper objectMapper = new ObjectMapper();

    private Constants() {}
}
