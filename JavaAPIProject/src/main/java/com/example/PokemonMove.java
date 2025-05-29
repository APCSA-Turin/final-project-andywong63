package com.example;

import com.example.Lib.JSONTools;
import com.example.Lib.PokeAPITools;
import com.example.Lib.WebRequests;
import org.json.JSONObject;

import java.io.IOException;

public class PokemonMove {
    private String internalName;
    private boolean dataFetched = false;
    private String name;
    private String effectDescription;
    private int accuracy;
    private String type;
    private int power;
    private int levelLearned;

    private String moveCategory; // "physical", "special", or "status"

    public PokemonMove(String name, int levelLearned) {
        this.internalName = name;
        this.levelLearned = levelLearned;
    }

    public PokemonMove(String name, int levelLearned, boolean autoFetch) throws IOException {
        this(name, levelLearned);
        if (autoFetch) fetchData();
    }

    // No-args constructor for Jackson deserializer
    public PokemonMove() {
    }

    public void fetchData() throws IOException {
        JSONObject moveData = WebRequests.getJson(Constants.POKEAPI_BASE + "/move/" + internalName);

        this.name = PokeAPITools.getEnglishString(moveData.getJSONArray("names"), "name");
        this.effectDescription = PokeAPITools.getEnglishString(moveData.getJSONArray("effect_entries"), "effect");
        this.accuracy = moveData.optInt("accuracy");
        this.power = moveData.optInt("power");
        this.type = JSONTools.getString(moveData, "type.name");
        this.moveCategory = JSONTools.getString(moveData, "damage_class.name");

        dataFetched = true;
    }

    // GETTERS AND SETTERS
    public String getInternalName() {
        return internalName;
    }
    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }
    public boolean isDataFetched() {
        return dataFetched;
    }
    public void setDataFetched(boolean dataFetched) {
        this.dataFetched = dataFetched;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEffectDescription() {
        return effectDescription;
    }
    public void setEffectDescription(String effectDescription) {
        this.effectDescription = effectDescription;
    }
    public int getLevelLearned() {
        return levelLearned;
    }
    public void setLevelLearned(int levelLearned) {
        this.levelLearned = levelLearned;
    }
    public int getAccuracy() {
        return accuracy;
    }
    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public int getPower() {
        return power;
    }
    public void setPower(int power) {
        this.power = power;
    }
    public String getMoveCategory() {
        return moveCategory;
    }
    public void setMoveCategory(String moveCategory) {
        this.moveCategory = moveCategory;
    }

    @Override
    public String toString() {
        if (!dataFetched) throw new IllegalStateException("Move data for " + internalName + " has not been fetched yet. Call fetchData() first before calling this method.");

        return name +
                " (PWR: " + power + ")" +
                "\nType: " + type +
                "\nLearned at level: " + levelLearned +
                "\n" + effectDescription;
    }
}
