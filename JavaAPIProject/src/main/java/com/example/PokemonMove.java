package com.example;

import org.json.JSONObject;

import java.io.IOException;

public class PokemonMove {
    private final String internalName;
    private String name;
    private int accuracy;
    private String type;
    private int power;

    public PokemonMove(String name, int levelLearned) {
        this.internalName = name;
    }

    public PokemonMove(String name, int levelLearned, boolean autoFetch) throws IOException {
        this(name, levelLearned);
        if (autoFetch) fetchData();
    }

    public void fetchData() throws IOException {
        JSONObject moveData = WebRequests.getJson(Constants.API_BASE + "/move/" + internalName);

        this.name = PokeAPITools.getEnglishString(moveData.getJSONArray("names"), "name");
        this.accuracy = moveData.optInt("accuracy");
        this.power = moveData.optInt("power");
        this.type = JSONTools.getString(moveData, "type.name");
    }

    public String getName() {
        return name;
    }
    public int getAccuracy() {
        return accuracy;
    }
    public String getType() {
        return type;
    }
    public int getPower() {
        return power;
    }
}
