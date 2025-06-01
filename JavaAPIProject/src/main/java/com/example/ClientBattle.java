package com.example;

import com.example.Lib.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.example.Constants.objectMapper;

public class ClientBattle {
    // CountDownLatch is to pause code execution until battle finishes
    private CountDownLatch battleStartedLatch = new CountDownLatch(1); // Don't continue code execution in App.java
    private CountDownLatch moveUsedLatch = new CountDownLatch(1); // Don't show move menu until both players used move
    private String wsBase;
    private String uuid;
    private User clientPlayer;
    private User opponentPlayer;
    private Pokemon clientPlayerPokemon;
    private Pokemon opponentPlayerPokemon;
    private int clientPlayerNum;
    private Scanner scan;
    private boolean pokemonDefeated;

    private WebSocketClient wsClient;

    public ClientBattle(String wsBase, String uuid, User player, Scanner scan) {
        this.wsBase = wsBase;
        this.uuid = uuid;
        clientPlayer = player;
        this.scan = scan;
    }
    public ClientBattle(String uuid, User player, Scanner scan) {
        this(Constants.WS_API_BASE, uuid, player, scan);
    }

    public User getClientPlayer() {
        return clientPlayer;
    }

    public void connectWs() throws URISyntaxException, JsonProcessingException, InterruptedException {
        wsClient = new WebSocketClient(new URI(wsBase + "/games/" + uuid + "/ws?player=" + clientPlayer.getUsername())) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
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
            public void onClose(int code, String reason, boolean b) {
//                System.out.println("Connect closed with code " + code + ", reason = " + reason);
//                if (code != 1000) {
//                    // Connection closed due to error, print error in console
//                    System.out.println("An error occurred in the WebSocket connection: " + reason + " (code + " + code + ")");
//                }
                battleStartedLatch.countDown(); // Continue rest of code execution in App.java
            }

            @Override
            public void onError(Exception e) {
                System.out.println("WebSocket error:");
                e.printStackTrace();
            }
        };
        wsClient.connect();
    }

    public void closeWs() {
        wsClient.close();
    }

    public boolean isWsOpen() {
        return wsClient != null && wsClient.isOpen();
    }

    public void onMessage(JSONObject message) throws IOException, InterruptedException {
        switch (message.getString("type")) {
            case "GAME_STATE":
                JSONObject game = message.getJSONObject("game");
                User player1 = objectMapper.readValue(game.getJSONObject("player1").toString(), User.class);
                User player2 = objectMapper.readValue(game.getJSONObject("player2").toString(), User.class);
                if (clientPlayer.getUsername().equals(player1.getUsername())) {
                    // Client player is player 1
                    clientPlayer = player1;
                    opponentPlayer = player2;
                    clientPlayerNum = 1;
                } else {
                    // Client player is player 2
                    clientPlayer = player2;
                    opponentPlayer = player1;
                    clientPlayerNum = 2;
                }
                clientPlayerPokemon = clientPlayer.getPokemons().getFirst();
                opponentPlayerPokemon = opponentPlayer.getPokemons().getFirst();
                break;
            case "START_GAME":
                System.out.println("Opponent has connected!");
                startBattle();
                break;
            case "MOVES_USED":
                movesUsed(message);
                moveUsedLatch.countDown();
                break;

            case "CONNECTION_ERROR":
                System.err.println("An error occurred during connection to WebSocket: " + message.getString("message"));
                break;
            case "DEBUG":
                System.out.println("Received debug message from server");
                break;
            default:
                System.out.println("Websocket received unknown message: " + message);
        }
    }

    // Returns the User object of the player from the server
    public void connectToBattle() throws InterruptedException, IOException, URISyntaxException {
        connectWs();
        System.out.println("Currently waiting for opponent...");
        battleStartedLatch.await(); // Pause code execution (so it doesn't go back to menu in App.java)
    }

    public void startBattle() throws IOException, InterruptedException {
        pokemonDefeated = false;
        new Thread(() -> { // Create a new thread for the battle to prevent blocking new messages from websocket
            try {
                Utils.slowPrintln("Tips:", 50, scan);
                Utils.slowPrintln("When you see ▼ at the end of a message, press enter to continue", 40, scan);
                Utils.slowPrintlnPause("Press enter to skip message animations", 50, scan);
                System.out.println();

                Utils.slowPrintlnPause(opponentPlayer.getUsername() + " wants to battle!", 50, scan);
                TimeUnit.MILLISECONDS.sleep(75);
                Utils.slowPrintln(opponentPlayer.getUsername() + " sent out " + opponentPlayerPokemon.getName() + "!", 50, scan);
                TimeUnit.MILLISECONDS.sleep(250);
                Utils.slowPrintln("You sent out " + clientPlayerPokemon.getName() + "!", 50, scan);
                TimeUnit.MILLISECONDS.sleep(500);

                showMenu(); // Main game loop

                // Game ended, close WebSocket
                wsClient.close(1000, "Game finished");
                battleStartedLatch.countDown(); // Continue rest of code execution
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }


    private void showMenu() throws IOException, InterruptedException {
        PokemonMove[] moves = clientPlayerPokemon.getLearntMoves();
        while (!pokemonDefeated) {
            Utils.clearScreen();
            printPokemonInfo();

            int moveNum = 0;
            for (int i = 0; i < moves.length; i++) {
                PokemonMove move = moves[i];
                if (move == null) continue;
                System.out.println(" [" + (i + 1) + "] " + move.getName());
                moveNum++;
            }
            System.out.println();
            int choice = Utils.askInt(scan, "Choose your move:") - 1;
            if (choice >= 0 && choice < moveNum) {
                useMove(choice);
            } else {
                Utils.slowPrintlnPause("That is not a valid choice.", 30, scan);
            }
        }
    }

    private void printPokemonInfo() {
        String p1Bar = Utils.progressBar(clientPlayerPokemon.getCurrentHp(), clientPlayerPokemon.getHpStat(), 30);
        String p2Bar = Utils.progressBar(opponentPlayerPokemon.getCurrentHp(), opponentPlayerPokemon.getHpStat(), 30);

        System.out.println("You -> " + clientPlayerPokemon.getName() + " (Lvl " + clientPlayerPokemon.getLevel() + ")");
        System.out.println(" - HP: " + p1Bar + " (" + clientPlayerPokemon.hpFraction() + ")");
        System.out.println(opponentPlayer.getUsername() + " -> " + opponentPlayerPokemon.getName() + " (Lvl " + opponentPlayerPokemon.getLevel() + ")");
        System.out.println(" - HP: " + p2Bar);
        System.out.println();
    }

    private void useMove(int moveIndex) throws JsonProcessingException, InterruptedException {
        // Send a message to websocket server with move to use
        wsClient.send(objectMapper.writeValueAsString(Map.of(
                "type", "USE_MOVE",
                "move", moveIndex
        )));
        System.out.println("Waiting for opponent's move...");
        moveUsedLatch.await(); // Don't continue back to menu

        moveUsedLatch = new CountDownLatch(1); // Create new CountDownLatch so it can be used again
    }

    private void movesUsed(JSONObject message) throws IOException, InterruptedException {
        JSONObject clientState;
        JSONObject opponentState;
        PokemonMove clientMove;
        PokemonMove opponentMove;
        boolean clientMovedFirst = message.getInt("movedFirst") == clientPlayerNum;
        if (clientPlayerNum == 1) {
            clientState = message.getJSONObject("player1");
            opponentState = message.getJSONObject("player2");
        } else {
            clientState = message.getJSONObject("player2");
            opponentState = message.getJSONObject("player1");
        }
        clientMove = objectMapper.readValue(clientState.getJSONObject("moveUsed").toString(), PokemonMove.class);
        opponentMove = objectMapper.readValue(opponentState.getJSONObject("moveUsed").toString(), PokemonMove.class);

        if (clientMovedFirst) {
            moveCycle(clientPlayerPokemon, clientState, clientMove, opponentPlayerPokemon, opponentState, opponentMove);
        } else {
            moveCycle(opponentPlayerPokemon, opponentState, opponentMove, clientPlayerPokemon, clientState, clientMove);
        }
        TimeUnit.MILLISECONDS.sleep(300);
    }

    private void moveCycle(Pokemon firstPokemon, JSONObject firstState, PokemonMove firstMove, Pokemon secondPokemon, JSONObject secondState, PokemonMove secondMove) throws InterruptedException, IOException {
        Utils.clearScreen();
        printPokemonInfo();
        Utils.slowPrintln(firstPokemon.getName() + " used " + firstMove.getName() + "!", 50, scan);
        secondPokemon.setCurrentHp(secondState.getInt("hp"));

        TimeUnit.MILLISECONDS.sleep(500);
        Utils.clearScreen();
        printPokemonInfo();
        TimeUnit.MILLISECONDS.sleep(700);

        if (!checkWin(scan)) { // Only make second pokemon use move if it's not fainted
            Utils.slowPrintln(secondPokemon.getName() + " used " + secondMove.getName() + "!", 50, scan);
            firstPokemon.setCurrentHp(firstState.getInt("hp"));
            TimeUnit.MILLISECONDS.sleep(500);
            Utils.clearScreen();
            printPokemonInfo();
            TimeUnit.MILLISECONDS.sleep(700);
            checkWin(scan);
        }
    }

    private boolean checkWin(Scanner scan) throws IOException, InterruptedException {
        if (clientPlayerPokemon.getCurrentHp() == 0) {
            Utils.slowPrintln(clientPlayerPokemon.getName() + " has fainted.", 50, scan);
        } else if (opponentPlayerPokemon.getCurrentHp() == 0) {
            Utils.slowPrintln(opponentPlayerPokemon.getName() + " has fainted.", 50, scan);
            clientPlayerPokemon.defeatPokemon(opponentPlayerPokemon);
        } else return false;
        TimeUnit.MILLISECONDS.sleep(200);
        pokemonDefeated = true;
        return true;
    }
}
