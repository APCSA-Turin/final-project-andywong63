package com.example;

import com.example.Lib.Utils;
import com.example.Lib.WebRequests;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.example.Constants.objectMapper;

public class App {
    private static Pokemon pokemon;
    private static User user;
    private static String serverApiBase = Constants.SERVER_API_BASE;
    private static String wsApiBase = Constants.WS_API_BASE;
    private static ClientBattle clientBattle;

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        boolean debug = isDebug();
        Scanner scan = new Scanner(System.in);

        Utils.clearScreen();

        user = null;

        while (user == null) {
            Utils.clearScreen();
            System.out.println(Utils.ansiText("Final Project: Pokemon Simulation", "1;4"));
            System.out.println(" [1] Sign In");
            System.out.println(" [2] Register");
            System.out.println();
            System.out.println(" [R] Run server");
            System.out.println(" [U] Change server IP");
            System.out.println("\n");
            String choice = Utils.ask(scan, "Choose an option:", "#").toUpperCase();
            switch (choice) {
                case "1":
                    String username = Utils.ask(scan, "Please enter your username:", ">");
                    try {
                        String data = WebRequests.getText(serverApiBase + "/users/" + username);
                        user = objectMapper.readValue(data, User.class);
                    } catch (FileNotFoundException e) {
                        System.out.println("Could not find user");
                    }
                    break;
                case "2":
                    String registerUsername = Utils.ask(scan, "Please enter the username to register:", ">");
                    WebRequests.postJson(serverApiBase + "/users", new JSONObject(Map.of(
                            "username", registerUsername
                    )).toString());
                    System.out.println("Successfully registered with username '" + registerUsername + "'");
                    break;

                case "R":
                    Server.init();
                    Server.start();
                    break;
                case "U":
                    String base = Utils.ask(scan, "Please enter the server IP (without the http://):", ">");
                    serverApiBase = "http://" + base;
                    wsApiBase = "ws://" + base;

                    System.out.println("Successfully changed server IP");
                    break;
                default:
                    System.out.println("Invalid input");
            }
            if (user == null) {
                System.out.print(Utils.ansiText("Press enter to continue", "3"));
                scan.nextLine();
            }
        }

        if (user.getPokemons().isEmpty()) {
            String pokemonName = Utils.ask(scan, "Enter a Pokemon name: ", ">");
            pokemon = new Pokemon(pokemonName, 5, false);
            System.out.println("Fetching Pokemon data...");
            pokemon.fetchData();
            System.out.println("Fetched data for " + pokemon.getName());

            user.addPokemon(pokemon);
            String pokemonJson = objectMapper.writeValueAsString(pokemon);
            WebRequests.postJson(serverApiBase + "/users/" + user.getUsername() + "/pokemons", pokemonJson);
        } else {
            pokemon = user.getPokemons().getFirst();
        }

        while (true) {
            Utils.clearScreen();

            System.out.println("Pokemon Simulation");
            System.out.println(" [1] Show current stats");
            System.out.println(" [2] Learn a move");
            System.out.println(" [3] Fight online battle");
            System.out.println(" [4] Join battle");
            System.out.println(" [5] Heal Pokemon");
            System.out.println();
            if (debug) System.out.println(" [D] Show debugger");
            System.out.println(" [0] Quit");

            String choice = Utils.ask(scan, "Choose an option:", "#");
            if (choice.equals("0")) {
                // Stop server on exit
                if (Server.isServerStarted()) Server.stop();
                // Stop websocket client on exit
                if (clientBattle != null && clientBattle.isWsOpen()) clientBattle.closeWs();
                break;
            }
            boolean enterToContinue = false;
            switch (choice) {
                case "1":
                    System.out.println(pokemon);
                    enterToContinue = true;
                    break;
                case "2":
                    learnMove(scan);
                    break;
                case "3":
                    onlineBattle(scan);
                    enterToContinue = true;
                    break;
                case "4":
                    joinBattle(scan);
                    enterToContinue = true;
                    break;
                case "5":
                    healPokemon();
                    break;

                case "D":
                    System.out.println("Debugger finished");
                    break;
                default:
                    System.out.println("Invalid input");
            }
            if (enterToContinue) {
                System.out.print(Utils.ansiText("Press enter to continue", "3"));
                scan.nextLine();
            }
        }
    }

    private static void learnMove(Scanner scan) throws IOException, InterruptedException, URISyntaxException {
        ArrayList<PokemonMove> learntMoves = new ArrayList<>(Arrays.asList(pokemon.getLearntMoves()));
        ArrayList<PokemonMove> possibleMoves = pokemon.getPossibleMoves();

        ArrayList<PokemonMove> moveChoices = new ArrayList<>();
        for (PokemonMove move : possibleMoves) {
            if (move.getLevelLearned() > pokemon.getLevel()) continue; // Only show moves that this pokemon has a high enough level for
            boolean alreadyLearned = false;
            for (PokemonMove learntMove : learntMoves) { // Check if pokemon already knows this move
                if (learntMove != null && learntMove.getName().equals(move.getName())) {
                    // Pokemon already knows this move
                    alreadyLearned = true;
                    break;
                }
            }
            if (alreadyLearned) continue;
            moveChoices.add(move);
        }
        if (moveChoices.isEmpty()) {
            Utils.slowPrintlnPause(pokemon.getName() + " does not have any moves to learn.", 35, scan);
            return;
        }

        PokemonMove move = chooseMove(moveChoices, "Choose a number to learn the move, or 0 to cancel:", scan);
        if (move == null) return;

        boolean learnSuccess = pokemon.learnMove(move);
        if (learnSuccess) {
            Utils.slowPrintlnPause(pokemon.getName() + " learned " + move.getName() + "!", 50, scan);
        } else {
            // Pokemon already knows 4 moves, ask user to replace old move
            String message = pokemon.getName() + " is trying to learn " + move.getName() + ", but it already knows 4 moves.";
            Utils.slowPrintln(message, 40, scan);
            TimeUnit.MILLISECONDS.sleep(500);
            message = "Forget an old move to learn " + move.getName() + "?";
            Utils.slowPrintln(message, 40, scan);
            String input = Utils.ask(scan, "", "[Y/N] ");
            if (input.equals("Y")) {
                PokemonMove forgetMove = chooseMove(learntMoves, "Choose a number to forget the move, or 0 to cancel:", scan);
                if (forgetMove != null) {
                    pokemon.forgetMove(forgetMove);
                    pokemon.learnMove(move);
                    Utils.slowPrintln(pokemon.getName() + " forgot " + forgetMove.getName() + "...", 50, scan);
                    TimeUnit.MILLISECONDS.sleep(200);
                    Utils.slowPrintlnPause("and learned " + move.getName() + "!", 50, scan);
                    learnSuccess = true;
                }
            }
        }

        if (learnSuccess) {
            // Save to multiplayer API
            updateServer(pokemon, 0);
        } else {
            Utils.slowPrintlnPause(pokemon.getName() + " did not learn " + move.getName() + ".", 50, scan);
        }
    }

    // Create a new game and join the game
    private static void onlineBattle(Scanner scan) {
        String opponentUsername = Utils.ask(scan, "Enter the username of the player you want to fight against:", ">");
        if (opponentUsername.equals(user.getUsername())) {
            System.out.println("You can't battle yourself!");
            return;
        }
        try {
            JSONObject gameJson = WebRequests.postJson(serverApiBase + "/games", new JSONObject(Map.of(
                    "player1", user.getUsername(),
                    "player2", opponentUsername
            )).toString());
            String uuid = gameJson.getString("uuid");
            System.out.println("Game created with UUID " + uuid);

            clientBattle = new ClientBattle(wsApiBase, uuid, user, scan);
            clientBattle.connectToBattle();
            // Update objects from server (server has the most updated instances)
            user = clientBattle.getClientPlayer();
            pokemon = user.getPokemons().getFirst();
            // Since pokemon may have gained xp (from defeating other pokemon), update values in multiplayer API
            updateServer(pokemon, 0);
        } catch (IOException e) {
            System.out.println("Player not found");
        } catch (URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void joinBattle(Scanner scan) throws IOException, URISyntaxException, InterruptedException {
        String uuid = Utils.ask(scan, "Enter the UUID of the game to join:", ">");
        clientBattle = new ClientBattle(wsApiBase, uuid, user, scan);
        clientBattle.connectToBattle();
        // Update objects from server (server has the most updated instances)
        user = clientBattle.getClientPlayer();
        pokemon = user.getPokemons().getFirst();
        // Since pokemon may have gained xp (from defeating other pokemon), update values in multiplayer API
        updateServer(pokemon, 0);
    }

    private static void healPokemon() throws IOException, URISyntaxException {
        pokemon.setCurrentHp(pokemon.getHpStat());

        // Save to multiplayer API
        updateServer(pokemon, 0);
    }


    // Show menu to choose a move
    // Can be used for learning a new move or forgetting an old move
    private static PokemonMove chooseMove(ArrayList<PokemonMove> moves, String ask, Scanner scan) throws IOException, InterruptedException {
        while (true) {
            Utils.clearScreen();

            for (int i = 0; i < moves.size(); i++) {
                System.out.println(" [" + (i + 1) + "] " + moves.get(i));
                System.out.println("\n\n");
            }

            int choice = Utils.askInt(scan, ask);
            if (choice == 0) {
                return null;
            } else if (choice < 1) {
                Utils.slowPrintlnPause("That number is too small!", 30, scan);
            } else if (choice > moves.size()) {
                Utils.slowPrintlnPause("That number is too large!", 30, scan);
            } else {
                return moves.get(choice - 1);
            }
        }
    }

    // Update pokemon in server with new values
    private static void updateServer(Pokemon pokemon, int index) throws IOException, URISyntaxException {
        String pokemonJson = objectMapper.writeValueAsString(pokemon);
        WebRequests.putJson(serverApiBase + "/users/" + user.getUsername() + "/pokemons/" + index, pokemonJson);
    }

    // https://stackoverflow.com/a/28754689
    // Checks if program is launched in debug mode in IDEA
    private static boolean isDebug() {
        return ManagementFactory.
                getRuntimeMXBean().
                getInputArguments().toString().contains("jdwp");
    }
}