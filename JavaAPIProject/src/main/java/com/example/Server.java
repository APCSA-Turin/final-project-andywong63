package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Server {
    private static Javalin app;
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static HashMap<String, User> usersDatabase = new HashMap<>();
    private final static HashMap<String, Battle> gamesDatabase = new HashMap<>();

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
            int index = user.getPokemons().size() - 1;
            ctx.status(201).header("Location", "/user/" + ctx.pathParam("username") + "/pokemons/" + index).json(pokemon);
        });

//        app.post("/games", ctx -> {
//            String uuid = UUID.randomUUID().toString();
//            gamesDatabase.put()
//        });
//
//        app.ws("/ws/game/{uuid}", wsConfig -> {
//            wsConfig.
//        });
    }

    public static void start() {
        app.start(4321);
    }
}
