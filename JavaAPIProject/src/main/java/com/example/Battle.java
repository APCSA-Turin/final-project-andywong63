package com.example;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Battle {
    private Pokemon player1Pokemon;
    private Pokemon player2Pokemon;

    private boolean battleFinished = false;

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
        while (!battleFinished) {
            Utils.clearScreen();
            System.out.println("You -> " + player1Pokemon.getName() + " (Lvl " + player1Pokemon.getLevel() + ")");
            System.out.println(" - HP: " + player1Pokemon.hpFraction());
            System.out.println("Player 2 -> " + player2Pokemon.getName() + " (Lvl " + player2Pokemon.getLevel() + ")");
            System.out.println(" - HP: " + player2Pokemon.hpFraction());
            System.out.println();
            System.out.println(" [1] Instakill (temporary)"); // TODO: Implement moves
            System.out.println();
            System.out.println("Choose your move:");
            System.out.print("# ");
            String input = scan.nextLine();
            if (input.equals("1")) {
                useMove(scan);
            }
        }
    }

    private void useMove(Scanner scan) throws IOException, InterruptedException {
        Utils.slowPrintln(player1Pokemon.getName() + " used Instakill!", 50, scan);
        System.out.print(player2Pokemon.getName() + ": " + player2Pokemon.hpFraction());
        player2Pokemon.takeDamage(999999);
        System.out.println(" -> " + player2Pokemon.hpFraction());

        checkWin(scan);
    }

    private void checkWin(Scanner scan) throws IOException, InterruptedException {
        if (player1Pokemon.getCurrentHp() == 0) {
            Utils.slowPrintln(player1Pokemon.getName() + " has fainted.", 50, scan);
        } else if (player2Pokemon.getCurrentHp() == 0) {
            Utils.slowPrintln(player2Pokemon.getName() + " has fainted.", 50, scan);
            player1Pokemon.defeatPokemon(player2Pokemon);
        } else return;
        battleFinished = true;
    }
}
