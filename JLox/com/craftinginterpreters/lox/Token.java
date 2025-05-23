package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.TokenType;

public class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type; // eg. STRING
        this.lexeme = lexeme; // eg. "Hello world!"
        this.literal = literal; // eg. Hello world!
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal; // STRING "Hello world!" Hello world! // TYPE LEXEME LITERAL
    }
}
