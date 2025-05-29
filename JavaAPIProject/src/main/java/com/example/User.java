package com.example;

import java.util.ArrayList;

public class User {
    private String username;
    private ArrayList<Pokemon> pokemons;

    public User(String username) {
        this.username = username;
        pokemons = new ArrayList<>();
    }

    // No-args constructor for Jackson deserializer
    public User() {
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public ArrayList<Pokemon> getPokemons() {
        return pokemons;
    }
    public void setPokemons(ArrayList<Pokemon> pokemons) {
        this.pokemons = pokemons;
    }

    public void addPokemon(Pokemon pokemon) {
        pokemons.add(pokemon);
    }

}
