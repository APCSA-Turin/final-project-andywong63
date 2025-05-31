package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Map;

public class BackendBattle {
    private OnlineGame game;
    private Pokemon player1Pokemon;
    private Pokemon player2Pokemon;
    private PokemonMove player1Move;
    private PokemonMove player2Move;

    public BackendBattle(Pokemon player1Pokemon, Pokemon player2Pokemon, OnlineGame game) {
        this.game = game;
        this.player1Pokemon = player1Pokemon;
        this.player2Pokemon = player2Pokemon;
        player1Move = null;
        player2Move = null;
    }

    public void p1UseMove(PokemonMove move) throws JsonProcessingException {
        player1Move = move;
        bothUseMove();
    }
    public void p2UseMove(PokemonMove move) throws JsonProcessingException {
        player2Move = move;
        bothUseMove();
    }

    public void bothUseMove() throws JsonProcessingException {
        if (player1Move == null || player2Move == null) return;

        // Find first pokemon to use the move
        int firstPlayer;
        if (player1Move.getPriority() > player2Move.getPriority()) { // Check move priority (higher = first)
            firstPlayer = 1;
        } else if (player2Move.getPriority() > player1Move.getPriority()) {
            firstPlayer = 2;
        } else if (player1Pokemon.getSpeedStat() > player2Pokemon.getSpeedStat()) { // Same move priority, check the speed stat
            firstPlayer = 1;
        } else if (player2Pokemon.getSpeedStat() > player1Pokemon.getSpeedStat()) {
            firstPlayer = 2;
        } else {
            // Both pokemons used move with same priority and have same speed, randomly choose which one to move first
            firstPlayer = (int) (Math.random() * 2) + 1;
        }

        int p1DamageTaken = 0;
        int p2DamageTaken = 0;
        if (firstPlayer == 1) {
            // Player 1 pokemon move first
            p2DamageTaken = player2Pokemon.calculateDamageTaken(player1Move, player1Pokemon);
            player2Pokemon.takeDamage(p2DamageTaken);
            if (player2Pokemon.getCurrentHp() > 0) { // Player 2 pokemon didn't faint, use player 2's move
                p1DamageTaken = player1Pokemon.calculateDamageTaken(player2Move, player2Pokemon);
                player1Pokemon.takeDamage(p1DamageTaken);
            }
        } else {
            // Player 2 pokemon move first
            p1DamageTaken = player1Pokemon.calculateDamageTaken(player2Move, player2Pokemon);
            player1Pokemon.takeDamage(p1DamageTaken);
            if (player1Pokemon.getCurrentHp() > 0) { // Player 1 pokemon didn't faint, use player 1's move
                p2DamageTaken = player2Pokemon.calculateDamageTaken(player1Move, player1Pokemon);
                player2Pokemon.takeDamage(p2DamageTaken);
            }
        }

        game.broadcastToAll(Map.of(
                "type", "MOVES_USED",
                "movedFirst", firstPlayer,
                "player1", Map.of(
                        "damage", p1DamageTaken,
                        "hp", player1Pokemon.getCurrentHp(),
                        "moveUsed", player1Move
                ),
                "player2", Map.of(
                        "damage", p2DamageTaken,
                        "hp", player2Pokemon.getCurrentHp(),
                        "moveUsed", player2Move
                )
        ));

        player1Move = null;
        player2Move = null;
    }
}
