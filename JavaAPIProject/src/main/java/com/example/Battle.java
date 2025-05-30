package com.example;

import com.example.Lib.Utils;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Battle {
    private Pokemon player1Pokemon;
    private Pokemon player2Pokemon;

    public Battle(Pokemon player1Pokemon, Pokemon player2Pokemon) {
        this.player1Pokemon = player1Pokemon;
        this.player2Pokemon = player2Pokemon;
    }
    
    public void startBattle(Scanner scan) throws InterruptedException, IOException {
        Utils.slowPrintln("Tips:", 50, scan);
        Utils.slowPrintln("When you see ▼ at the end of a message, press enter to continue", 40, scan);
        Utils.slowPrintlnPause("Press enter to skip message animations", 50, scan);
        System.out.println();

        Utils.slowPrintlnPause("Player 2 wants to battle!", 50, scan);
        TimeUnit.MILLISECONDS.sleep(100);
        Utils.slowPrintln("Player 2 sent out " + player2Pokemon.getName() + "!", 50, scan);
        TimeUnit.MILLISECONDS.sleep(500);
        Utils.slowPrintln("You sent out " + player1Pokemon.getName() + "!", 50, scan);
        TimeUnit.MILLISECONDS.sleep(1000);
        showMenu(scan);
    }

    private void showMenu(Scanner scan) throws IOException, InterruptedException {
        PokemonMove[] moves = player1Pokemon.getLearntMoves();
        while (true) {
            Utils.clearScreen();

            String p1Bar = Utils.progressBar(player1Pokemon.getCurrentHp(), player1Pokemon.getHpStat(), 30);
            String p2Bar = Utils.progressBar(player2Pokemon.getCurrentHp(), player2Pokemon.getHpStat(), 30);

            System.out.println("You -> " + player1Pokemon.getName() + " (Lvl " + player1Pokemon.getLevel() + ")");
            System.out.println(" - HP: " + p1Bar + " (" + player1Pokemon.hpFraction() + ")");
            System.out.println("Player 2 -> " + player2Pokemon.getName() + " (Lvl " + player2Pokemon.getLevel() + ")");
            System.out.println(" - HP: " + p2Bar + " (" + player2Pokemon.hpFraction() + ")");
            System.out.println();

            if (checkWin(scan)) {
                break;
            }

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
                useMove(scan, choice);
            } else {
                Utils.slowPrintlnPause("That is not a valid choice.", 30, scan);
            }
        }
    }

    private void useMove(Scanner scan, int moveIndex) throws IOException, InterruptedException {
        PokemonMove move = player1Pokemon.getLearntMoves()[moveIndex];

        Utils.slowPrintln(player1Pokemon.getName() + " used " + move.getName() + "!", 50, scan);
        TimeUnit.MILLISECONDS.sleep(300);

        int damage = player2Pokemon.calculateDamageTaken(move, player1Pokemon);
        player2Pokemon.takeDamage(damage);

        TimeUnit.MILLISECONDS.sleep(300);
    }

    private boolean checkWin(Scanner scan) throws IOException, InterruptedException {
        if (player1Pokemon.getCurrentHp() == 0) {
            Utils.slowPrintln(player1Pokemon.getName() + " has fainted.", 50, scan);
        } else if (player2Pokemon.getCurrentHp() == 0) {
            Utils.slowPrintln(player2Pokemon.getName() + " has fainted.", 50, scan);
            player1Pokemon.defeatPokemon(player2Pokemon);

        } else return false;
        return true;
    }
}
