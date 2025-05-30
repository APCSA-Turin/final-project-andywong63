package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.javalin.websocket.WsConnectContext;

import java.util.Map;

import static com.example.Constants.objectMapper;


public class OnlineGame {
    private User player1;
    private User player2;
    private String uuid;
    private boolean player1Connected;
    private boolean player2Connected;
    private WsConnectContext player1Ctx;
    private WsConnectContext player2Ctx;

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

    private void checkBothConnected(WsConnectContext ctx) throws JsonProcessingException {
        if (player1Connected && player2Connected) {
            broadcastToAll(objectMapper.writeValueAsString(Map.of(
                    "type", "START_GAME"
            )));
        }
    }


    // Broadcast a message to all clients
    private void broadcastToAll(String message) {
        player1Ctx.send(message);
        player2Ctx.send(message);
    }
}
