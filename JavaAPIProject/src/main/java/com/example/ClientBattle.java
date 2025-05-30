package com.example;

import com.example.Lib.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ClientBattle {
    // CountDownLatch is to pause code execution until battle finishes
    private CountDownLatch battleStartedLatch = new CountDownLatch(1);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private String wsBase;
    private String uuid;
    private User currentPlayer;
    private User opponentPlayer;
    private Pokemon currentPlayerPokemon;
    private Pokemon opponentPlayerPokemon;
    private Scanner scan;

    private WebSocketClient wsClient;

    public ClientBattle(String wsBase, String uuid, User player, Scanner scan) {
        this.wsBase = wsBase;
        this.uuid = uuid;
        currentPlayer = player;
        this.scan = scan;
    }
    public ClientBattle(String uuid, User player, Scanner scan) {
        this(Constants.WS_API_BASE, uuid, player, scan);
    }

    public void connectWs() throws URISyntaxException {
        wsClient = new WebSocketClient(new URI(wsBase + "/games/" + uuid + "/ws?player=" + currentPlayer.getUsername())) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                System.out.println("Websocket opened on client");
            }

            @Override
            public void onMessage(String s) {
                try {
                    ClientBattle.this.onMessage(new JSONObject(s)); // Call the onMessage method outside WebSocketClient
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                System.out.println("Websocket closed on client");
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        };
        wsClient.connect();
    }

    public void onMessage(JSONObject message) throws IOException, InterruptedException {
        switch (message.getString("type")) {
            case "GAME_STATE":
                JSONObject game = message.getJSONObject("game");
                User player1 = objectMapper.readValue(game.getJSONObject("player1").toString(), User.class);
                User player2 = objectMapper.readValue(game.getJSONObject("player2").toString(), User.class);
                if (currentPlayer.getUsername().equals(player1.getUsername())) {
                    // Client player is player 1
                    currentPlayer = player1;
                    opponentPlayer = player2;
                } else {
                    // Client player is player 2
                    currentPlayer = player2;
                    opponentPlayer = player1;
                }
                currentPlayerPokemon = currentPlayer.getPokemons().getFirst();
                opponentPlayerPokemon = opponentPlayer.getPokemons().getFirst();
                break;
            case "START_GAME":
                System.out.println("Start game");
                startBattle();
                break;

            default:
                System.out.println("Websocket received unknown message: " + message);
        }
    }

    public void connectToBattle() throws InterruptedException, IOException, URISyntaxException {
        connectWs();
        System.out.println("Currently waiting for opponent...");
        battleStartedLatch.await();
    }

    public void startBattle() throws IOException, InterruptedException {
        Utils.slowPrintln("Tips:", 50, scan);
        Utils.slowPrintln("When you see ▼ at the end of a message, press enter to continue", 40, scan);
        Utils.slowPrintlnPause("Press enter to skip message animations", 50, scan);
        System.out.println();

        Utils.slowPrintlnPause(opponentPlayer.getUsername() + " wants to battle!", 50, scan);
        TimeUnit.MILLISECONDS.sleep(75);
        Utils.slowPrintln(opponentPlayer.getUsername() + " sent out " + opponentPlayerPokemon.getName() + "!", 50, scan);
        TimeUnit.MILLISECONDS.sleep(250);
        Utils.slowPrintln("You sent out " + currentPlayerPokemon.getName() + "!", 50, scan);
        TimeUnit.MILLISECONDS.sleep(500);

        battleStartedLatch.countDown();
    }
}
