package interpreters.arithmetic;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

class Arithmetic {

    public static void main(String[] args) {
        System.out.println("Welcome to my Arithmetic Scanner!");
        try {
        runPrompt();
        } catch (IOException e) { error(e.getMessage()); }
    }

    public static void run(String source) {
        ArithmeticScanner scanner = new ArithmeticScanner(source);
        ArrayList<Token> tokens = scanner.scanner();
        System.out.println(tokens);
        //log("Hello world!");
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        // Interactive loop for REPL
        while(true) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
        }
    }

    public static void error(String errorMsg) {
        System.err.println(errorMsg);
    }

    public static void log(String message) {
        try (FileWriter log = new FileWriter("arithmetic.log")){
            for (char c : message.toCharArray()) {
                log.write(c);
            }
            log.close();
        } catch (IOException e) { error(e.getMessage()); return; }
    }
} 