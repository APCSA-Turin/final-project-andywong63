package com.example;

import com.example.Lib.JSONTools;
import com.example.Lib.PokeAPITools;
import com.example.Lib.Utils;
import com.example.Lib.WebRequests;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Pokemon {
    private String internalName;
    private boolean dataFetched = false;
    private String name;
    private String type;
    private String subType;
    private ArrayList<PokemonMove> possibleMoves;
    private PokemonMove[] learntMoves;
    private LevelRate levelRate;
    private int level;
    private int xp;

    // Stats
    private int currentHp;
    private int hpStat;
    private int attackStat;
    private int defenseStat;
    private int specialStat;
    private int speedStat;

    // Base stats (stats directly from API, not actual current Pokemon stats)
    private int baseHp;
    private int baseAttack;
    private int baseDefense;
    private int baseSpecial;
    private int baseSpeed;

    // IV (Indivdual values, https://bulbapedia.bulbagarden.net/wiki/Individual_values)
    private int hpIv;
    private int attackIv;
    private int defenseIv;
    private int specialIv;
    private int speedIv;

    // EV (Effort values, https://bulbapedia.bulbagarden.net/wiki/Effort_values)
    private int hpEv;
    private int attackEv;
    private int defenseEv;
    private int specialEv;
    private int speedEv;


    private boolean wild;
    private int baseXpYield;

    public Pokemon(String name, int level, boolean wild) {
        this.internalName = name;
        this.level = level;
        this.wild = wild;
        possibleMoves = new ArrayList<>();
        learntMoves = new PokemonMove[4];

        // Generate IV values
        // https://bulbapedia.bulbagarden.net/wiki/Individual_values#Generation_I_and_II
        // Attack, defense, special, and speed IVs are random numbers from 0-15
        attackIv = (int) (Math.random() * 16);
        defenseIv = (int) (Math.random() * 16);
        specialIv = (int) (Math.random() * 16);
        speedIv = (int) (Math.random() * 16);
        // HP IV is calculated using the last binary digit of each of the previous IVs
        String attackIvBin = Utils.intToBinary(attackIv, 4);
        String defenseIvBin = Utils.intToBinary(defenseIv, 4);
        String specialIvBin = Utils.intToBinary(specialIv, 4);
        String speedIvBin = Utils.intToBinary(speedIv, 4);
        String digit1 = attackIvBin.substring(3);
        String digit2 = defenseIvBin.substring(3);
        String digit3 = specialIvBin.substring(3);
        String digit4 = speedIvBin.substring(3);
        hpIv = Utils.binaryToInt(digit1 + digit2 + digit3 + digit4);
    }

    public Pokemon(String name, int level, boolean wild, boolean autoFetch) throws IOException, URISyntaxException {
        this(name, level, wild);
        if (autoFetch) fetchData();
    }

    // No-args constructor for Jackson deserializer
    public Pokemon() {
    }

    // GETTERS AND SETTERS
    // Required for Jackson serializing/deserializing (converting from class to JSON and vice versa)
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
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getSubType() {
        return subType;
    }
    public void setSubType(String subType) {
        this.subType = subType;
    }
    public ArrayList<PokemonMove> getPossibleMoves() {
        return possibleMoves;
    }
    public void setPossibleMoves(ArrayList<PokemonMove> possibleMoves) {
        this.possibleMoves = possibleMoves;
    }
    public PokemonMove[] getLearntMoves() {
        return learntMoves;
    }
    public void setLearntMoves(PokemonMove[] learntMoves) {
        this.learntMoves = learntMoves;
    }
    public LevelRate getLevelRate() {
        return levelRate;
    }
    public void setLevelRate(LevelRate levelRate) {
        this.levelRate = levelRate;
    }
    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }
    public int getXp() {
        return xp;
    }
    public void setXp(int xp) {
        this.xp = xp;
    }
    // Stats
    public int getCurrentHp() {
        return currentHp;
    }
    public void setCurrentHp(int currentHp) {
        this.currentHp = currentHp;
    }
    public int getHpStat() {
        return hpStat;
    }
    public void setHpStat(int hpStat) {
        this.hpStat = hpStat;
    }
    public int getAttackStat() {
        return attackStat;
    }
    public void setAttackStat(int attackStat) {
        this.attackStat = attackStat;
    }
    public int getDefenseStat() {
        return defenseStat;
    }
    public void setDefenseStat(int defenseStat) {
        this.defenseStat = defenseStat;
    }
    public int getSpecialStat() {
        return specialStat;
    }
    public void setSpecialStat(int specialStat) {
        this.specialStat = specialStat;
    }
    public int getSpeedStat() {
        return speedStat;
    }
    public void setSpeedStat(int speedStat) {
        this.speedStat = speedStat;
    }
    // Base Stats
    public int getBaseHp() {
        return baseHp;
    }
    public void setBaseHp(int baseHp) {
        this.baseHp = baseHp;
    }
    public int getBaseAttack() {
        return baseAttack;
    }
    public void setBaseAttack(int baseAttack) {
        this.baseAttack = baseAttack;
    }
    public int getBaseDefense() {
        return baseDefense;
    }
    public void setBaseDefense(int baseDefense) {
        this.baseDefense = baseDefense;
    }
    public int getBaseSpecial() {
        return baseSpecial;
    }
    public void setBaseSpecial(int baseSpecial) {
        this.baseSpecial = baseSpecial;
    }
    public int getBaseSpeed() {
        return baseSpeed;
    }
    public void setBaseSpeed(int baseSpeed) {
        this.baseSpeed = baseSpeed;
    }
    // IV
    public int getHpIv() {
        return hpIv;
    }
    public void setHpIv(int hpIv) {
        this.hpIv = hpIv;
    }
    public int getAttackIv() {
        return attackIv;
    }
    public void setAttackIv(int attackIv) {
        this.attackIv = attackIv;
    }
    public int getDefenseIv() {
        return defenseIv;
    }
    public void setDefenseIv(int defenseIv) {
        this.defenseIv = defenseIv;
    }
    public int getSpecialIv() {
        return specialIv;
    }
    public void setSpecialIv(int specialIv) {
        this.specialIv = specialIv;
    }
    public int getSpeedIv() {
        return speedIv;
    }
    public void setSpeedIv(int speedIv) {
        this.speedIv = speedIv;
    }
    // EV
    public int getHpEv() {
        return hpEv;
    }
    public void setHpEv(int hpEv) {
        this.hpEv = hpEv;
    }
    public int getAttackEv() {
        return attackEv;
    }
    public void setAttackEv(int attackEv) {
        this.attackEv = attackEv;
    }
    public int getDefenseEv() {
        return defenseEv;
    }
    public void setDefenseEv(int defenseEv) {
        this.defenseEv = defenseEv;
    }
    public int getSpecialEv() {
        return specialEv;
    }
    public void setSpecialEv(int specialEv) {
        this.specialEv = specialEv;
    }
    public int getSpeedEv() {
        return speedEv;
    }
    public void setSpeedEv(int speedEv) {
        this.speedEv = speedEv;
    }
    // Extra
    public boolean isWild() {
        return wild;
    }
    public void setWild(boolean wild) {
        this.wild = wild;
    }
    public int getBaseXpYield() {
        return baseXpYield;
    }
    public void setBaseXpYield(int baseXpYield) {
        this.baseXpYield = baseXpYield;
    }
    // END GETTERS/SETTERS

    public String hpFraction() {
        return currentHp + "/" + hpStat;
    }

    public void takeDamage(int damage) {
        currentHp -= damage;
        if (currentHp < 0) currentHp = 0;
    }

    public void fetchData() throws IOException, URISyntaxException {
        JSONObject pokemonData = WebRequests.getJson(Constants.POKEAPI_BASE + "/pokemon/" + internalName);
        JSONObject speciesData = WebRequests.getJson(Constants.POKEAPI_BASE + "/pokemon-species/" + internalName);

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
                    int levelLearned = versionObj.getInt("level_learned_at");
                    PokemonMove pokemonMove = new PokemonMove(JSONTools.getString(moveObj, "move.name"), levelLearned, true);
                    int index = 0;
                    while (index < possibleMoves.size() && levelLearned > possibleMoves.get(index).getLevelLearned()) {
                        index++;
                    }
                    possibleMoves.add(index, pokemonMove);
                }
            }
        }

        levelRate = new LevelRate(JSONTools.getString(speciesData, "growth_rate.name"));
        xp = levelRate.getTotalXp(level);

        for (Object stat : pokemonData.getJSONArray("stats")) {
            if (!(stat instanceof JSONObject)) continue;

            JSONObject statObject = (JSONObject) stat;
            int baseStat = statObject.getInt("base_stat");
            switch (JSONTools.getString(statObject, "stat.name")) {
                case "hp":
                    baseHp = baseStat;
                    break;
                case "attack":
                    baseAttack = baseStat;
                    break;
                case "defense":
                    baseDefense = baseStat;
                    break;
                case "special-attack":
                    baseSpecial = baseStat;
                    break;
                case "speed":
                    baseSpeed = baseStat;
            }
        }

        calculateStats();
        currentHp = hpStat;
        dataFetched = true;
    }

    public boolean learnMove(PokemonMove move, int index) {
        if (!possibleMoves.contains(move)) return false;
        learntMoves[index] = move;
        return true;
    }
    public boolean learnMove(PokemonMove move) {
        for (int i = 0; i < 4; i++) {
            if (learntMoves[i] == null) return learnMove(move, i);
        }
        return false;
    }
    public boolean forgetMove(PokemonMove move) {
        for (int i = 0; i < 4; i++) {
            if (learntMoves[i].getInternalName().equals(move.getInternalName())) {
                learntMoves[i] = null;
                return true;
            }
        }
        return false;
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

        builder.append("\n\nStats:");
        builder.append("\nHP: ").append(hpStat).append(" (").append(hpIv).append(" IV, ").append(hpEv).append(" EV)");
        builder.append("\nAttack: ").append(attackStat).append(" (").append(attackIv).append(" IV, ").append(attackEv).append(" EV)");
        builder.append("\nDefense: ").append(defenseStat).append(" (").append(defenseIv).append(" IV, ").append(defenseEv).append(" EV)");
        builder.append("\nSpecial: ").append(specialStat).append(" (").append(specialIv).append(" IV, ").append(specialEv).append(" EV)");
        builder.append("\nSpeed: ").append(speedStat).append(" (").append(speedIv).append(" IV, ").append(speedEv).append(" EV)");

        return builder.toString();
    }


    private void calculateStats() {
        // Formula: https://bulbapedia.bulbagarden.net/wiki/Stat#Generations_I_and_II
        // DV = IV
        // STATEXP = EV
        hpStat = statParentheses(baseHp, hpIv, hpEv) + level + 10;
        attackStat = statParentheses(baseAttack, attackIv, attackEv) + 5;
        defenseStat = statParentheses(baseDefense, defenseIv, defenseEv) + 5;
        specialStat = statParentheses(baseSpecial, specialIv, specialEv) + 5;
        speedStat = statParentheses(baseSpeed, speedIv, speedEv) + 5;
    }
    private int statParentheses(int base, int iv, int ev) {
        // Calculate the parentheses (shared part in both HP formula and other stats formula)
        return (int) (((base + iv) * 2 + (Math.sqrt(ev) / 4)) * level) / 100;
    }


    // Gain XP and update EVs from defeating a pokemon
    public void defeatPokemon(Pokemon otherPokemon) {
        int xpGained = getXpGained(otherPokemon);

        int oldXp = xp;
        int oldLevel = level;

        xp += xpGained;
        System.out.println("Gained " + xpGained + " xp (" + oldXp + " -> " + xp + ")");
        level = levelRate.getLevel(xp);
        if (level != oldLevel) {
            System.out.println("Leveled up from " + oldLevel + " to " + level);
        }
        int oldMaxHp = hpStat;
        calculateStats();
        int maxHpChange = hpStat - oldMaxHp;
        currentHp += maxHpChange;

        // Add EVs
        hpEv += otherPokemon.baseHp;
        attackEv += otherPokemon.baseAttack;
        defenseEv += otherPokemon.baseDefense;
        specialEv += otherPokemon.baseSpecial;
        speedEv += otherPokemon.baseSpeed;
    }

    // https://bulbapedia.bulbagarden.net/wiki/Experience#Gain_formula
    private int getXpGained(Pokemon otherPokemon) {
        if (!dataFetched) throw new IllegalStateException("Pokemon data for " + internalName + " has not been fetched yet. Call fetchData() first before calling this method.");
        if (!otherPokemon.dataFetched) throw new IllegalStateException("Pokemon data for " + otherPokemon.internalName + " has not been fetched yet. Call fetchData() first before calling this method.");

        double b = otherPokemon.baseXpYield;
        double L = otherPokemon.level;
        double s = 1; // Total amount of Pokemon in battle, TODO: Implement
        double a = 1.5;
        if (otherPokemon.wild) a = 1;

        return (int) (((b * L) / 7) * (1 / s) * a);
    }

    // Formula: https://bulbapedia.bulbagarden.net/wiki/Damage#Generation_I
    public int calculateDamageTaken(PokemonMove move, Pokemon attackingPokemon) {
        if (move.getMoveCategory().equals("status")) return 0; // Status moves don't deal damage

        double critical = 1; // TODO: Calculate if damage is critical
        double attackStat; // Variable A in formula
        double defenseStat; // Variable D in formula
        if (move.getMoveCategory().equals("physical")) {
            attackStat = attackingPokemon.attackStat;
            defenseStat = this.defenseStat;
        } else {
            attackStat = attackingPokemon.specialStat;
            defenseStat = specialStat;
        }
        if (attackStat > 255 || defenseStat > 255) {
            attackStat = (int) (attackStat / 4);
            defenseStat = (int) (defenseStat / 4);
        }
        int power = move.getPower();
        double sameTypeAttackBonus = 1; // Variable STAB in formula
        if (move.getType().equals(type) || move.getType().equals(subType)) {
            sameTypeAttackBonus = 1.5;
        }
        // TODO: Implement type effectiveness
        int typeEffectiveness1 = 1;
        int typeEffectiveness2 = 1;

        double damage = ((((((2 * level * critical) / 5) + 2) * power * (attackStat / defenseStat)) / 50) + 2)
                * sameTypeAttackBonus
                * typeEffectiveness1
                * typeEffectiveness2;

        if ((int) damage <= 1) return (int) damage;

        double random = ((int) (Math.random() * 39) + 217) / 255.0;
        return (int) (damage * random);
    }
}