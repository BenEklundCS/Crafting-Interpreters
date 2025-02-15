package com.craftinginterpreters.lox;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Lox {
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    static boolean debug = false;

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
        if (debug) {
            debugMode(source);
        }
        else {
            normalMode(source);
        }
    }

    private static void normalMode(String source) {
        // scan source code to get tokens
        Scanner scanner = new Scanner(source);
        ArrayList<Token> tokens = scanner.scanTokens();
        // parse tokens into AST
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        if (hadError) return;
        // scan for variables and look for issues
        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);
        // check for errors
        if (hadError) return;
        // interpret AST with tree walk interpreter
        interpreter.interpret(statements);
    }
    private static void debugMode(String source) {
        // Scanner class converts a String of source code into an ArrayList of Tokens using the scanTokens method
        System.out.println("\n::SOURCE CODE::");
        System.out.println(source);
        Scanner scanner = new Scanner(source);
        ArrayList<Token> tokens = scanner.scanTokens();
        // Can also use printTheTokens() to show all tokens in run if needed
        System.out.println("\n::SCANNED TOKENS::");
        printTheTokens(tokens);
        // Parser class converts an ArrayList of tokens to a parsed expression using recursive descent parsing
        Parser parser = new Parser(tokens);
        List<Stmt> stmtList = parser.parse();
        if (hadError) return;
        System.out.println("\n::INTERPRETED EXPRESSION::");
        interpreter.interpret(stmtList);
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
