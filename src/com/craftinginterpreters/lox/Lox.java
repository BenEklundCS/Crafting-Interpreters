package com.craftinginterpreters.lox;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Lox {

    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        }
        else if (args.length == 1) {
            runFile(args[0]);
        }
        else {
            runPrompt();
        }
    }
    
    // Scan from input file
        // If Lox is launched with a command line argument, it tries to run the file at that given path
        // Lox is a scripting language, so it executes directly from the source file
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        // indicate error has occured on-exit
        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    // Scan from cmd line
        // If lox is launched without any command line arguments, it enters a REPL mode
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        // Interactive loop for REPL
        while(true) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    // Run the line
        // Common method for running a string of source code
    private static void run(String source) {
        // Scanner class converts a String of source code into an ArrayList of Tokens using the scanTokens method
        Scanner scanner = new Scanner(source);
        ArrayList<Token> tokens = scanner.scanTokens();
        // Can also use printTheTokens() to show all tokens in run if needed
        //printTheTokens(tokens);
        // Parser class converts an ArrayList of tokens to a parsed expression using recursive descent parsing
        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();
        if (hadError) return;
        interpreter.interpret(expression);
        //System.out.println(new ASTPrinter().print(expression));
    }

    @Deprecated
    static void printTheTokens(ArrayList<Token> tokens) {
        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        }
        else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}
