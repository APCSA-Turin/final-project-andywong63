package com.example;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Utils {
    public static void slowPrint(String str, int speed, Scanner scan) throws InterruptedException, IOException {
        for (int i = 0; i < str.length(); i++) {
            System.out.print(str.substring(i, i + 1));
            TimeUnit.MILLISECONDS.sleep(speed); // https://stackoverflow.com/a/24104427
            if (System.in.available() > 0) {
                scan.nextLine();
                System.out.print("\033[A"); // https://stackoverflow.com/a/11474509, go back to previous line to finish message
                System.out.print(str);
                break;
            }
        }
    }

    public static void slowPrintln(String str, int speed, Scanner scan) throws InterruptedException, IOException {
        slowPrint(str, speed, scan);
        System.out.println();
    }

    public static void slowPrintlnPause(String str, int speed, Scanner scan) throws InterruptedException, IOException {
        slowPrint(str, speed, scan);
        System.out.print(" ▼");
        scan.nextLine();
    }

    // https://stackoverflow.com/a/32295974
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
