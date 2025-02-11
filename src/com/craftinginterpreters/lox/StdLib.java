package com.craftinginterpreters.lox;

import java.util.List;

public class StdLib {
    public static void define(Environment environment) {
        // define clock function
        environment.define("clock", new LoxCallable() {
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000.0f;
            }

            @Override
            public int arity() {
                return 0;
            }
            @Override
            public String toString() {
                return "<native fn>";
            }
        });
        environment.define("print", new LoxCallable() {
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                System.out.println(arguments.get(0));
                return null;
            }

            @Override
            public int arity() {
                return 0;
            }
            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }
}
