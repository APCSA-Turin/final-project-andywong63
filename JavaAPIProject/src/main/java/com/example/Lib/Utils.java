package com.example.Lib;

import java.io.*;
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
        while (System.in.available() == 0) { // Make arrow blink
            // https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797#cursor-controls
            TimeUnit.MILLISECONDS.sleep(750);
            if (System.in.available() > 0) break;
            System.out.print("\033[1D ");
            TimeUnit.MILLISECONDS.sleep(750);
            if (System.in.available() > 0) break;
            System.out.print("\033[1D▼");
        }
        scan.nextLine();
    }

    // https://stackoverflow.com/a/32295974
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Creates a colored progress bar string based on a value compared to the max value
     * <ul>
     * <li>If percent is over 50%, bar is green</li>
     * <li>If percent is between 20% and 50%, bar is yellow</li>
     * <li>If percent is less than 20%, bar is red</li>
     * </ul>
     *
     * @param currentVal The current value
     * @param maxVal The max value
     * @param size The amount of characters in the progress bar
     * @return The progress bar string
     */
    public static String progressBar(double currentVal, double maxVal, int size) {
        double percent = currentVal / maxVal;
        int numActive = (int) (percent * size);
        StringBuilder result = new StringBuilder();
        if (percent > 0.5) {
            result.append("\033[32m"); // Green
        } else if (percent >= 0.2) {
            result.append("\033[33m"); // Yellow
        } else {
            result.append("\033[31m"); // Red
        }
        for (int i = 0; i < numActive; i++) {
            result.append("▬");
        }
        result.append("\033[90m"); // Gray
        for (double i = numActive; i < size; i++) {
            result.append("▬");
        }
        result.append("\033[0m"); // Reset color
        return result.toString();
    }


    /**
     * Uses Scanner to ask the user for a string input, with styling
     * @param scan The Scanner to use
     * @param ask The question shown
     * @param prefix The prefix before the user input area
     * @return The received input
     */
    public static String ask(Scanner scan, String ask, String prefix) {
        // If question is blank, don't add the extra line break
        if (ask != null && !ask.isBlank()) ask += "\n";
        else ask = "";
        System.out.print(ask + prefix + " \033[37m");
        String result = scan.nextLine();
        System.out.print("\033[0m");
        return result;
    }

    /**
     * Uses Scanner to ask the user for a numerical input, with styling, retrying if invalid
     * @param scan The Scanner to use
     * @param ask The question shown
     * @return The received input
     */
    public static int askInt(Scanner scan, String ask) {
        while (true) {
            try {
                return Integer.parseInt(ask(scan, ask, "#"));
            } catch (NumberFormatException e) { // Input was not a number
                System.out.println("That is not a number.");
            }
        }
    }


    public static String intToBinary(int num, int digits) { // https://stackoverflow.com/a/2406441
        StringBuilder binary = new StringBuilder(Integer.toBinaryString(num));
        while (binary.length() < digits) { // Pad beginning of binary with 0 if not enough digits
            binary.insert(0, "0");
        }
        return binary.toString();
    }
    public static int binaryToInt(String binary) {  // https://stackoverflow.com/a/10179008
        return Integer.parseInt(binary, 2);
    }

    /**
     * Style a text portion with ANSI<br>
     * Uses codes from <a href="https://en.wikipedia.org/wiki/ANSI_escape_code#Select_Graphic_Rendition_parameters">https://en.wikipedia.org/wiki/ANSI_escape_code</a>
     * @param text The text to style
     * @param ansi The ANSI numerical codes, separated by semicolons
     * @return The printable text
     */
    public static String ansiText(String text, String ansi) {
        return "\033[" + ansi + "m" + text + "\033[0m";
    }


    // https://www.baeldung.com/java-write-to-file
    public static void saveToFile(String filePath, String contents) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
        writer.write(contents);
        writer.close();
    }

    // https://beginnersbook.com/2014/01/how-to-read-file-in-java-using-bufferedreader/
    public static String readFromFile(String filePath) throws IOException {
        StringBuilder result = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line = reader.readLine();
            while (line != null) {
                result.append(line);
                result.append("\n");
                line = reader.readLine();
            }

            return result.toString();
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}
