package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsContext;
import org.json.JSONObject;

import java.util.Map;

import static com.example.Constants.objectMapper;


public class OnlineGame {
    private User player1;
    private User player2;
    private Pokemon player1Pokemon;
    private Pokemon player2Pokemon;
    private String uuid;
    private WsConfig ws;
    private boolean player1Connected;
    private boolean player2Connected;
    private WsContext player1Ctx;
    private WsContext player2Ctx;
    private BackendBattle battle;

    public OnlineGame(User player1, User player2, String uuid) {
        this.player1 = player1;
        this.player2 = player2;
        this.uuid = uuid;
    }

    public User getPlayer1() {
        return player1;
    }
    public User getPlayer2() {
        return player2;
    }
    public String getUuid() {
        return uuid;
    }

    public boolean isPlayer1Connected() {
        return player1Connected;
    }
    public boolean isPlayer2Connected() {
        return player2Connected;
    }

    // Get the player number of the WsContext
    // Returns 1 or 2, or 0 if none
    public int getCtxPlayer(WsContext ctx) {
        if (player1.getUsername().equals(ctx.queryParam("player"))) return 1;
        else if (player2.getUsername().equals(ctx.queryParam("player"))) return 2;
        else return 0;
    }


    public void connectWs(WsConfig ws) {
        this.ws = ws;
    }

    public void connectPlayer1(WsConnectContext ctx) throws JsonProcessingException {
        ctx.send(objectMapper.writeValueAsString(Map.of(
                "type", "GAME_STATE",
                "game", this
        )));
        player1Connected = true;
        player1Ctx = ctx;
        checkBothConnected(ctx);
    }
    public void connectPlayer2(WsConnectContext ctx) throws JsonProcessingException {
        ctx.send(objectMapper.writeValueAsString(Map.of(
                "type", "GAME_STATE",
                "game", this
        )));
        player2Connected = true;
        player2Ctx = ctx;
        checkBothConnected(ctx);
    }
    public void disconnectPlayer1() {
        player1Connected = false;
        player1Ctx = null;
    }
    public void disconnectPlayer2() {
        player2Connected = false;
        player2Ctx = null;
    }

    private void checkBothConnected(WsConnectContext ctx) throws JsonProcessingException {
        if (player1Connected && player2Connected) {
            broadcastToAll(Map.of(
                    "type", "START_GAME"
            ));
            player1Pokemon = player1.getPokemons().getFirst();
            player2Pokemon = player2.getPokemons().getFirst();
            battle = new BackendBattle(player1Pokemon, player2Pokemon, this);
            wsListen();
        }
    }


    private void wsListen() {
        ws.onMessage(ctx -> {
            JSONObject message = new JSONObject(ctx.message());
            System.out.println("Received message from " + ctx.queryParam("player") + ": " + message);
            switch (message.getString("type")) {
                case "USE_MOVE":
                    if (getCtxPlayer(ctx) == 1) {
                        // Player 1 sent USE_MOVE message
                        PokemonMove move = player1Pokemon.getLearntMoves()[message.getInt("move")];
                        battle.p1UseMove(move);
                    } else {
                        // Player 2 sent USE_MOVE message
                        PokemonMove move = player2Pokemon.getLearntMoves()[message.getInt("move")];
                        battle.p2UseMove(move);
                    }
                    break;
            }
        });
    }

    // Broadcast a message to all clients
    public void broadcastToAll(String message) {
        System.out.println("Broadcasting to all clients: " + message);
        player1Ctx.send(message);
        player2Ctx.send(message);
    }
    public void broadcastToAll(Map<String, Object> map) throws JsonProcessingException {
        broadcastToAll(objectMapper.writeValueAsString(map));
    }


    private void sendError(WsContext ctx, String message) throws JsonProcessingException {
        ctx.send(objectMapper.writeValueAsString(Map.of(
                "type", "ERROR",
                "message", message
        )));
    }
}
