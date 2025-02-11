package com.craftinginterpreters.lox;

import com.craftinginterpreters.LoxCallable;
import com.craftinginterpreters.lox.Expr.Binary;
import com.craftinginterpreters.lox.Expr.Grouping;
import com.craftinginterpreters.lox.Expr.Unary;

import java.util.ArrayList;
import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    Environment globals = new Environment();
    private Environment environment = globals;
    static boolean breakStatement = false;

    Interpreter() {
        // define global scope
        globals.define("clock", new LoxCallable() {
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
    }

    /**
     * Entry point for interpreting a list of statements.
     * This method iterates through and executes each statement in order.
     */
    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    /**
     * Executes a statement by invoking its visitor method.
     */
    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    /**
     * Evaluates an expression by invoking its visitor method.
     */
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    // ===========================
    // EXPRESSIONS (visit methods)
    // ===========================

    /** Handles literal values (numbers, strings, booleans, etc.). */
    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    /** Retrieves the value of a variable from the environment. */
    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    /** Evaluates a grouping expression (e.g., `(3 + 2)`). */
    @Override
    public Object visitGroupingExpr(Grouping expr) {
        return evaluate(expr.expression);
    }

    /** Handles logical expressions (AND, OR). */
    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left; // Short-circuit if left is truthy
        } else {
            if (!isTruthy(left)) return left; // Short-circuit if left is falsey
        }

        return evaluate(expr.right);
    }

    /** Handles assignment expressions (e.g., `x = 5`). */
    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    /** Evaluates a unary expression (`-x`, `!x`). */
    @Override
    public Object visitUnaryExpr(Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG, NOT:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double) right;
        }

        // Unreachable.
        return null;
    }

    /** Evaluates a function call expression, e.g. my_function(a, b); */
    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        // check and ensure we got a callable function before casting
        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expr.paren, "Can only call functions and classes.");
        }
        // cast
        LoxCallable function = (LoxCallable)callee;

        // after cast, make sure arity, or the # of args passed is as expected. Throw a runtime error if not (Python style)
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " + function.arity() + " arguments, but got " + arguments.size() + ".");
        }

        return function.call(this, arguments);
    }

    /** Evaluates a binary expression (`+, -, *, /, comparisons`). */
    @Override
    public Object visitBinaryExpr(Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                checkDivisionByZero(expr.operator, (Double) right);
                return (double) left / (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case PLUS:
                if (left instanceof String || right instanceof String) {
                    return stringify(left) + stringify(right);
                }
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers, or two strings.");
        }

        // Unreachable.
        return null;
    }

    // ===========================
    // STATEMENTS (visit methods)
    // ===========================

    /** Executes a block of statements within a new environment scope. */
    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    /** Evaluates an expression statement (`print(5)`, `x + 2;`). */
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        LoxFunction function = new LoxFunction(stmt);
        environment.define(stmt.name.lexeme, function); // ✅ Store the function in the environment
        return null;
    }


    /** Executes an if statement. */
    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    /** Declares and initializes a variable (`var x = 5;`). */
    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, value);
        return null;
    }

    /** Prints an expression (`print "Hello";`). */
    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    /** Executes a while loop (`while (condition) { body }`). */
    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            try {
                execute(stmt.body);
            } catch (BreakException e) {
                break; // Stop the loop if a break is encountered
            }
        }
        return null;
    }

    /** Handles the `break` statement inside loops. */
    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        throw new BreakException();
    }

    static class BreakException extends RuntimeException {}

    // ===========================
    // HELPER METHODS
    // ===========================

    /** Executes a list of statements within a new environment scope. */
    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt statement : statements) {
                execute(statement);

                // Stop immediately if a break statement is encountered.
                if (Interpreter.breakStatement) {
                    return;
                }
            }
        } finally {
            this.environment = previous;
        }
    }

    /** Checks that an operand is a number, throwing a RuntimeError otherwise. */
    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    /** Checks that both operands are numbers, throwing a RuntimeError otherwise. */
    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    /** Throws an error if attempting to divide by zero. */
    private void checkDivisionByZero(Token operator, Double right) {
        if (right != 0) return;
        throw new RuntimeError(operator, "Division by zero.");
    }

    /** Determines if a value is truthy (follows Ruby-like semantics). */
    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    /** Determines if two values are equal. */
    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    /** Converts an object to a Lox-compatible string representation. */
    private String stringify(Object object) {
        if (object == null) return "nil";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }
}
