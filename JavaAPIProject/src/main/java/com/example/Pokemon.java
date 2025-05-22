package com.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class Pokemon {
    private final String internalName;
    private boolean dataFetched = false;
    private String name;
    private String type;
    private String subType;
    private ArrayList<PokemonMove> possibleMoves;
    private boolean wild;
    private LevelRate levelRate;
    private int level;
    private int xp;
    private int maxHp;
    private int currentHp;

    private int baseXpYield;

    public Pokemon(String name, int level, boolean wild) {
        this.internalName = name;
        this.level = level;
        this.wild = wild;
        possibleMoves = new ArrayList<>();
    }

    public Pokemon(String name, int level, boolean wild, boolean autoFetch) throws IOException {
        this(name, level, wild);
        if (autoFetch) fetchData();
    }

    public String getName() {
        return name;
    }
    public int getLevel() {
        return level;
    }
    public int getCurrentHp() {
        return currentHp;
    }
    public int getMaxHp() {
        return maxHp;
    }

    public String hpFraction() {
        return currentHp + "/" + maxHp;
    }

    public void takeDamage(int damage) {
        currentHp -= damage;
        if (currentHp < 0) currentHp = 0;
    }

    public void fetchData() throws IOException {
        JSONObject pokemonData = WebRequests.getJson(Constants.API_BASE + "/pokemon/" + internalName);
        JSONObject speciesData = WebRequests.getJson(Constants.API_BASE + "/pokemon-species/" + internalName);

        type = JSONTools.getString(pokemonData, "types.0.type.name");
        subType = JSONTools.getString(pokemonData, "types.1.type.name");

        baseXpYield = pokemonData.getInt("base_experience");

        this.name = PokeAPITools.getEnglishString(speciesData.getJSONArray("names"), "name");

        JSONArray apiMoves = pokemonData.getJSONArray("moves");
        for (Object move : apiMoves) {
            if (!(move instanceof JSONObject)) continue;
            JSONObject moveObj = (JSONObject) move;

            JSONArray moveVersions = moveObj.getJSONArray("version_group_details");
            for (Object version : moveVersions) {
                if (!(version instanceof JSONObject)) continue;
                JSONObject versionObj = (JSONObject) version;
                if (!JSONTools.getString(versionObj, "version_group.name").equals(Constants.GENERATION)) continue;

                if (JSONTools.getString(versionObj, "move_learn_method.name").equals("level-up")) {
                    PokemonMove pokemonMove = new PokemonMove(JSONTools.getString(moveObj, "move.name"), versionObj.getInt("level_learned_at"), true);
                    possibleMoves.add(pokemonMove);
                }
            }
        }

        levelRate = new LevelRate(JSONTools.getString(speciesData, "growth_rate.name"));
        xp = levelRate.getTotalXp(level);

        maxHp = PokeAPITools.getIntIf(pokemonData.getJSONArray("stats"), "base_stat", "stat.name", "hp");
        currentHp = maxHp;

        dataFetched = true;
    }

    @Override
    public String toString() {
        if (!dataFetched) throw new IllegalStateException("Pokemon data for " + internalName + " has not been fetched yet. Call fetchData() first before calling this method.");

        StringBuilder builder = new StringBuilder();
        builder.append(name);
        builder.append(" (Level ").append(level).append(", ").append(xp).append(" XP)");
        builder.append("\nType: ").append(type);
        if (subType != null) {
            builder.append("\nSubtype: ").append(subType);
        }
        return builder.toString();
    }


    // TODO: Switch to separate Battle class
    // https://bulbapedia.bulbagarden.net/wiki/Experience#Gain_formula
    public void defeatPokemon(Pokemon otherPokemon) {
        if (!dataFetched) throw new IllegalStateException("Pokemon data for " + internalName + " has not been fetched yet. Call fetchData() first before calling this method.");
        if (!otherPokemon.dataFetched) throw new IllegalStateException("Pokemon data for " + otherPokemon.internalName + " has not been fetched yet. Call fetchData() first before calling this method.");

        double b = otherPokemon.baseXpYield;
        double L = otherPokemon.level;
        double s = 1; // TODO: Implement
        double a = 1.5;
        if (otherPokemon.wild) a = 1;

        double xpGained = ((b * L) / 7) * (1 / s) * a;
        System.out.println("Gained " + xpGained + " xp (" + xp + " -> " + (xp += (int) xpGained) + ")");
        level = levelRate.getLevel(xp);
    }
}