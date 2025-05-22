package com.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Stores how fast a Pokemon levels up
 */
public class LevelRate {
    /**
     * Array of total XP required for each level
     */
    private final int[] xpRequired;

    /**
     *
     * @param identifier The ID of the level/growth rate, can be numerical (ex: 4) or string (ex: "medium-slow")
     */
    public LevelRate(String identifier) throws IOException {
        xpRequired = new int[100];
        JSONObject response = WebRequests.getJson("https://pokeapi.co/api/v2/growth-rate/" + identifier);
        JSONArray levels = response.getJSONArray("levels");
        for (int i = 0; i < levels.length(); i++) {
            xpRequired[i] = levels.getJSONObject(i).getInt("experience");
        }
    }

    /**
     * Get the current level at a given XP
     * @param xp Current XP
     * @return The current level number
     */
    public int getLevel(int xp) {
        for (int i = 0; i < xpRequired.length; i++) {
            if (xp < xpRequired[i]) return i;
        }
        return 100;
    }

    /**
     * Get the total XP required to reach a certain level
     * @param level The level of the Pokemon
     * @return The total XP required
     */
    public int getTotalXp(int level) {
        return xpRequired[level - 1];
    }
}
