package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.craftinginterpreters.lox.Lox.*;
import static com.craftinginterpreters.lox.TokenType.*;

public class Scanner {
    private final String source; // String of source code
    private final ArrayList<Token> tokens = new ArrayList<>(); // Array of tokens we'll build from source code

    Scanner(String source) {
        this.source = source;
    }

    ArrayList<Token> scanTokens() {
        // Consume tokens until isAtEnd is true
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        // Add EOF token once isAtEnd is true
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private int start = 0;
    private int current = 0;
    private int line = 1;

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            // Single character consumption
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '/':
                if (match('/')) {
                    comment();
                }
                else if (match('*')) {
                    blockComment();
                }
                else {
                    addToken(SLASH); break;
                }
                break;
            case '*': addToken(STAR); break;
            // Ignored stuff
            case ' ':
            case '\r':
            case '\t':
                break;
                // Igored, but increments the line counter
            case '\n':
                line++;
                break;
            // Conditional singles
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG); // !=
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL); // ==
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER); // >=
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS); // <=
                // Literals
            case '"': string(); break;
            default:
                if (isDigit(c)) {
                    number();
                }
                else if (isAlpha(c)) {
                    identifier();
                }
                else {
                    error(line, "Unexpected character");
                }
                break;
        }
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return (source.charAt(current));
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    // Boolean checks

    private boolean isDigit(char c) {
        return Character.isDigit(c);
    }

    private boolean isAlpha(char c) {
        return Character.isAlphabetic(c);
    }

    private boolean isAlphaNumeric(char c) {
        return isDigit(c) || isAlpha(c);
    }

    // Complex lexeme handlers
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        if (isAtEnd()) {
            error(line, "Unterminated string literal.");
            return;
        }
        advance();
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private void number() {
        while (isDigit(peek())) advance();
        // Look for the decimal
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }
        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private void comment() {
        while (peek() != '\n' && !isAtEnd()) advance();
    }

    private void blockComment() {
        while(!isAtEnd()) {
            advance();
            if (peek() == '*' && peekNext() == '/') {
                advance(); // consume the *
                advance(); // consume the /
                break;  // break out of MLC loop
            }
        }
    }

    // Keywords

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

}
