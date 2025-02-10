package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

public class Enviornment {
    private final Map<String, Object> values = new HashMap<>();
    final Enviornment enclosing;

    Enviornment() {
        enclosing = null;
    }

    Enviornment(Enviornment enclosing) {
        this.enclosing = enclosing;
    }

    // define a new variable
    void define(String name, Object value) {
        values.put(name, value);
    }

    // get an existing variable value
    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }
        if (enclosing != null) return enclosing.get(name);
        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }
}
