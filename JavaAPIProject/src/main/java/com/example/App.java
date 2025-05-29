package com.example;

import com.example.Lib.JSONTools;
import com.example.Lib.Utils;
import com.example.Lib.WebRequests;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class App {
    private static Pokemon pokemon;
    private static String serverUrl = Constants.SERVER_API_BASE;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws IOException, InterruptedException {
        boolean debug = isDebug();
        Scanner scan = new Scanner(System.in);

        Utils.clearScreen();

        User user = null;

        while (user == null) {
            Utils.clearScreen();
            System.out.println("Login Screen");
            System.out.println(" [1] Sign In");
            System.out.println(" [2] Register");
            System.out.println();
            System.out.println(" [R] Run server");
            System.out.println(" [U] Change server URL");
            System.out.println("\nChoose a number:");
            System.out.print("# ");
            String choice = scan.nextLine().toUpperCase();
            switch (choice) {
                case "1":
                    System.out.print("Please enter your username:\n> ");
                    String username = scan.nextLine();
                    String data = WebRequests.getText(serverUrl + "/users/" + username);
                    user = objectMapper.readValue(data, User.class);
                    break;
                case "2":
                    System.out.print("Please enter the username to register:\n> ");
                    String registerUsername = scan.nextLine();
                    WebRequests.postJson(serverUrl + "/users", new JSONObject(Map.of(
                            "username", registerUsername
                    )).toString());
                    System.out.println("Successfully registered with username '" + registerUsername + "'");
                    System.out.print("Press enter to continue");
                    scan.nextLine();
                    break;

                case "R":
                    Server.init();
                    Server.start();
                    break;
                case "U":
                    System.out.print("Please enter the server URL:\n> ");
                    serverUrl = scan.nextLine();
                    break;
                default:
                    System.out.println("Invalid input");
                    System.out.print("Press enter to continue");
                    scan.nextLine();
            }
        }

        if (user.getPokemons().isEmpty()) {
            System.out.print("Enter a Pokemon name: ");
            String pokemonName = scan.nextLine();
            pokemon = new Pokemon(pokemonName, 4, false);
            System.out.println("Fetching Pokemon data...");
            pokemon.fetchData();
            System.out.println("Fetched data for " + pokemon.getName());

            String pokemonJson = objectMapper.writeValueAsString(pokemon);
            WebRequests.postJson(serverUrl + "/users/" + user.getUsername() + "/pokemons", pokemonJson);
        } else {
            pokemon = user.getPokemons().get(0);
        }

        while (true) {
            System.out.println("Pokemon Simulation");
            System.out.println(" [1] Show current stats");
            System.out.println(" [2] Learn a move");
            System.out.println(" [3] Fight against another Pokemon");
            System.out.println();
            System.out.println(" [P] Test P2P");
            if (debug) System.out.println(" [D] Show debugger");
            System.out.println(" [0] Quit");

            System.out.println("\nChoose a number:");
            System.out.print("# ");
            String choice = scan.nextLine().toUpperCase();
            if (choice.equals("0")) break;
            switch (choice) {
                case "1":
                    System.out.println(pokemon);
                    break;
                case "2":
                    learnMove(scan);
                    break;
                case "3":
                    battle(scan, pokemon);
                    break;

                case "P":
                    Javalin app = Javalin.create()
                            .get("/", context -> context.result("Hello world"))
                            .start(4321);
                    scan.nextLine();
                    app.stop();
                    break;
                case "D":
                    System.out.println("Debugger finished");
                    break;
                default:
                    System.out.println("Invalid input");
            }
            System.out.print("Press enter to continue");
            scan.nextLine();
            System.out.println("\n\n");
            Utils.clearScreen();
        }
    }

    private static void learnMove(Scanner scan) throws IOException, InterruptedException {
        List<PokemonMove> learntMoves = Arrays.asList(pokemon.getLearntMoves());
        ArrayList<PokemonMove> possibleMoves = pokemon.getPossibleMoves();

        ArrayList<PokemonMove> moveChoices = new ArrayList<>();
        for (PokemonMove move : possibleMoves) {
            if (move.getLevelLearned() > pokemon.getLevel() || learntMoves.contains(move)) continue;
            moveChoices.add(move);
        }
        for (int i = 0; i < moveChoices.size(); i++) {
            System.out.println(" [" + (i + 1) + "] " + moveChoices.get(i));
            System.out.println("\n\n");
        }

        int choice = Utils.askInt(scan, "Choose a number to learn the move:");
        if (choice < 1) {
            System.out.println("That number is too small!");
            return;
        } else if (choice > moveChoices.size()) {
            System.out.println("That number is too large!");
            return;
        }
        PokemonMove move = moveChoices.get(choice - 1);

        boolean learnSuccess = pokemon.learnMove(move);
        if (learnSuccess) {
            Utils.slowPrintlnPause(pokemon.getName() + " learned " + move.getName() + "!", 50, scan);
        } else {
            String message = pokemon.getName() + " is trying to learn " + move.getName() + ", but it already knows 4 moves.";
            Utils.slowPrintln(message, 40, scan);
            TimeUnit.MILLISECONDS.sleep(500);
            message = "Forget an old move to learn " + move.getName() + "?";
            Utils.slowPrintln(message, 40, scan);
            System.out.print("[Y/N] ");
            String input = scan.nextLine();
            if (input.equals("Y")) {
                // TODO: Implement forgetting old move
            } else {
                Utils.slowPrintlnPause(pokemon.getName() + " did not learn " + move.getName() + ".", 50, scan);
            }
        }
    }

    private static void battle(Scanner scan, Pokemon pokemon1) throws InterruptedException, IOException {
        Pokemon opponent = new Pokemon("pikachu", 4, false, true);
        Battle battle = new Battle(pokemon1, opponent);
        battle.startBattle(scan);
    }

    // https://stackoverflow.com/a/28754689
    // Checks if program is launched in debug mode in IDEA
    private static boolean isDebug() {
        return ManagementFactory.
                getRuntimeMXBean().
                getInputArguments().toString().contains("jdwp");
    }
}