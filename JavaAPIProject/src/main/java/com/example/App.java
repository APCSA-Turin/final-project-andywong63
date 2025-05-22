package com.example;

import java.io.IOException;
import java.util.Scanner;

public class App {
    public static void main(String[] args) throws IOException, InterruptedException {
        Utils.clearScreen();

        boolean debug = isDebug();
        Scanner scan = new Scanner(System.in);
        System.out.print("Enter a Pokemon name: ");
        String pokemonName = scan.nextLine();
        Pokemon pokemon = new Pokemon(pokemonName, 4, false);
        System.out.println("Fetching Pokemon data...");
        pokemon.fetchData();
        System.out.println("Fetched data for " + pokemon.getName());

        while (true) {
            System.out.println("Pokemon Simulation");
            System.out.println(" [1] Show current stats");
            System.out.println(" [2] Fight against self");
            System.out.println(" [3] Fight against another Pokemon");
            System.out.println();
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
                    pokemon.defeatPokemon(pokemon);
                    break;
                case "3":
                    battle(scan, pokemon, pokemon);
                    break;

                case "D":
                    System.out.println("Debugger finished");
            }
            System.out.print("Press enter to continue");
            scan.nextLine();
            System.out.println("\n\n");
            Utils.clearScreen();
        }
    }

    private static void battle(Scanner scan, Pokemon pokemon1, Pokemon pokemon2) throws InterruptedException, IOException {
        Pokemon opponent = new Pokemon("pikachu", 4, false, true);
        Battle battle = new Battle(pokemon1, opponent);
        battle.startBattle(scan);
    }

    // https://stackoverflow.com/a/28754689
    // Checks if program is launched in debug mode in IDEA
    private static boolean isDebug() {
        return java.lang.management.ManagementFactory.
                getRuntimeMXBean().
                getInputArguments().toString().contains("jdwp");
    }
}