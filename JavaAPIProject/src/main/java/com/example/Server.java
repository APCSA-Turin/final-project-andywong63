package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.example.Constants.objectMapper;

public class Server {
    private static Javalin app;
    private final static HashMap<String, User> usersDatabase = new HashMap<>();
    private final static HashMap<String, OnlineGame> gamesDatabase = new HashMap<>();
    private static boolean serverStarted = false;

    public static void main(String[] args) throws IOException {
        init();
        start();

        // Initialize user database with players (so I don't need to keep constantly registering)
        User user1 = new User("DeltAndy");
        Pokemon pokemon1 = new Pokemon("squirtle", 4, false, true);
        pokemon1.learnMove(pokemon1.getPossibleMoves().getFirst());
        pokemon1.learnMove(pokemon1.getPossibleMoves().get(1));
        user1.addPokemon(pokemon1);
        User user2 = new User("test");
        Pokemon pokemon2 = new Pokemon("charmander", 4, false, true);
        pokemon2.learnMove(pokemon2.getPossibleMoves().getFirst());
        pokemon2.learnMove(pokemon2.getPossibleMoves().get(1));
        user2.addPokemon(pokemon2);
        usersDatabase.put("DeltAndy", user1);
        usersDatabase.put("test", user2);
    }

    public static void init() {
        app = Javalin.create()
                .get("/", ctx -> ctx.result("Hello World"));

        app.post("/users", ctx -> {
            if (!ctx.isJson()) {
                ctx.status(400).json(Map.of(
                        "error", "Request must be in JSON"
                ));
                return;
            }
            JSONObject json = new JSONObject(ctx.body());
            String username = json.optString("username");
            if (username.isEmpty()) {
                ctx.status(400).json(Map.of(
                        "error", "No value for 'username' in body"
                ));
                return;
            }
            if (usersDatabase.containsKey(username)) {
                // Username already taken
                ctx.status(409).json(Map.of(
                        "error", "Username already taken"
                ));
                return;
            }
            User user = new User(username);
            usersDatabase.put(username, user);
            ctx.status(201).header("Location", "/user/" + username).json(user);
        });

        app.get("/users/{username}", ctx -> {
            User user = usersDatabase.get(ctx.pathParam("username"));
            if (user == null) {
                ctx.status(404).json(Map.of(
                        "error", "User not found"
                ));
                return;
            }
            ctx.status(200).json(user);
        });

        app.get("/users/{username}/pokemons", ctx -> {
            User user = usersDatabase.get(ctx.pathParam("username"));
            if (user == null) {
                ctx.status(404).json(Map.of(
                        "error", "User not found"
                ));
                return;
            }
            ctx.status(200).json(user.getPokemons());
        });
        app.post("/users/{username}/pokemons", ctx -> {
            User user = usersDatabase.get(ctx.pathParam("username"));
            if (user == null) {
                ctx.status(404).json(Map.of(
                        "error", "User not found"
                ));
                return;
            }
            Pokemon pokemon = ctx.bodyAsClass(Pokemon.class);
            user.addPokemon(pokemon);
            ctx.status(201).json(pokemon);
        });
        app.get("/users/{username}/pokemons/{index}", ctx -> {
            User user = usersDatabase.get(ctx.pathParam("username"));
            if (user == null) {
                ctx.status(404).json(Map.of(
                        "error", "User not found"
                ));
                return;
            }
            try {
                int index = Integer.parseInt(ctx.pathParam("index"));
                if (index >= 0 && index < user.getPokemons().size()) {
                    Pokemon pokemon = user.getPokemons().get(index);
                    ctx.status(200).json(pokemon);
                } else {
                    ctx.status(404).json(Map.of(
                            "error", "Index out of bounds"
                    ));
                }
            } catch (NumberFormatException e) {
                ctx.status(400).json(Map.of(
                        "error", "Index is not a number"
                ));
            }
        });
        app.put("/users/{username}/pokemons/{index}", ctx -> {
            User user = usersDatabase.get(ctx.pathParam("username"));
            if (user == null) {
                ctx.status(404).json(Map.of(
                        "error", "User not found"
                ));
                return;
            }
            try {
                int index = Integer.parseInt(ctx.pathParam("index"));
                if (index >= 0 && index < user.getPokemons().size()) {
                    // Replace pokemon at index with one in request body
                    Pokemon pokemon = ctx.bodyAsClass(Pokemon.class);
                    user.setPokemon(index, pokemon);
                    ctx.status(201).json(pokemon);
                } else {
                    ctx.status(404).json(Map.of(
                            "error", "Index out of bounds"
                    ));
                }
            } catch (NumberFormatException e) {
                ctx.status(400).json(Map.of(
                        "error", "Index is not a number"
                ));
            }
        });

        app.post("/games", ctx -> {
            if (!ctx.isJson()) {
                ctx.status(400).json(Map.of(
                        "error", "Request must be in JSON"
                ));
                return;
            }
            JSONObject json = new JSONObject(ctx.body());
            String player1Name = json.optString("player1");
            String player2Name = json.optString("player2");
            User user1 = usersDatabase.get(player1Name);
            User user2 = usersDatabase.get(player2Name);
            if (user1 == null) {
                ctx.status(400).json(Map.of(
                        "error", "Player 1 not found"
                ));
                return;
            }
            if (user2 == null) {
                ctx.status(400).json(Map.of(
                        "error", "Player 2 not found"
                ));
                return;
            }

            String uuid = UUID.randomUUID().toString();
            OnlineGame game = new OnlineGame(user1, user2, uuid);
            gamesDatabase.put(uuid, game);

            ctx.status(201).header("Location", "/games/" + uuid).json(game);
        });

        app.get("/games/{uuid}", ctx -> {
            OnlineGame game = gamesDatabase.get(ctx.pathParam("uuid"));
            if (game == null) {
                ctx.status(404).json(Map.of(
                        "error", "Game not found"
                ));
                return;
            }
            ctx.status(200).json(game);
        });

        // /games/{uuid}/ws?player={username}
        app.ws("/games/{uuid}/ws", ws -> {
            ws.onConnect(ctx -> {
                ctx.session.setIdleTimeout(Duration.ofMinutes(5)); // Only close connection after 5 minutes

                OnlineGame game = gamesDatabase.get(ctx.pathParam("uuid"));
                if (game == null) {
                    // Send game not found message and close websocket
                    ctx.send(objectMapper.writeValueAsString(Map.of(
                            "type", "CONNECTION_ERROR",
                            "message", "Game not found"
                    )));
                    ctx.session.close(1008, "Invalid game UUID");
                    return;
                }
                String username = ctx.queryParam("player");
                User player = usersDatabase.get(username);
                if (username == null || player == null) {
                    // Send player not found message and close websocket
                    ctx.send(objectMapper.writeValueAsString(Map.of(
                            "type", "CONNECTION_ERROR",
                            "message", "Player not found"
                    )));
                    ctx.session.close(1008, "Invalid username");
                    return;
                }

                if (username.equals(game.getPlayer1().getUsername())) {
                    game.connectPlayer1(ctx);
                    game.connectWs(ws);
                } else if (username.equals(game.getPlayer2().getUsername())) {
                    game.connectPlayer2(ctx);
                } else {
                    // Player is not in current game
                    ctx.send(objectMapper.writeValueAsString(Map.of(
                            "type", "CONNECTION_ERROR",
                            "message", "Player is not in this game"
                    )));
                    ctx.session.close(1008, "Invalid player");
                }
            });
        });
    }

    public static void start() {
        app.start(4321);
        serverStarted = true;
    }
    public static void stop() {
        app.stop();
        serverStarted = false;
    }

    public static boolean isServerStarted() {
        return serverStarted;
    }
}
