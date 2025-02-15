package com.craftinginterpreters.lox;

import java.util.List;

/**
 * Represents a user-defined function in Lox.
 * Implements the LoxCallable interface, making it callable like native functions.
 */
public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration; // Stores the function declaration (name, parameters, body).
    private final Environment closure;

    /**
     * Constructs a new LoxFunction from a function declaration statement.
     *
     * @param declaration The function declaration containing parameters and body.
     */
    LoxFunction(Stmt.Function declaration, Environment closure) {
        this.closure = closure;
        this.declaration = declaration;
    }

    /**
     * Executes the function when called.
     *
     * @param interpreter The interpreter executing this function.
     * @param arguments The arguments passed to the function call.
     * @return The result of the function
     */
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        // Create a new environment for the function call.
        // It is nested inside the global environment to allow variable access.
        Environment environment = new Environment(closure);
        // Bind each function parameter to its corresponding argument.
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        // Execute the function body within this new environment.
        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }

        // Currently, Lox functions always return null, since return statements aren't implemented yet.
        return null;
    }

    /**
     * Returns the number of parameters this function expects.
     *
     * @return The arity (parameter count) of the function.
     */
    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}
