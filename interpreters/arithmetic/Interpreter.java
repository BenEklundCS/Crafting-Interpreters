import java.util.ArrayList;

class Interpreter {
    private String source;
    private int start = 0;
    private int current = 0;
    private String num = "";

    Interpreter(String source) {
        this.source = source;
    }

    public ArrayList<Token> scanner() {
        ArrayList<Token> tokens = new ArrayList<Token>();

        while(!isAtEnd()) {
            char c = getNext();
            // Handle Integers (very broken)
            if (isInt(c)) {
                while (isInt(c) && !isAtEnd()) {
                    num += c;
                    c = getNext(); 
                    // This is an issue bc I'm reading ...2...+...2...4 and the first 2 is a num,
                    // and then I immediately call getNext resulting in the + getting read, and my int is dropped
                    // Troubleshoot this through struggle - no googling this one.
                }
                tokens.add(new Token(TokenType.INTEGER, c+""));
                num = "";
            }
            // Handle all other tokens lol
            switch(c) {
                case '+': tokens.add(new Token(TokenType.PLUS, "+")); break;
                default:
                    Arithmetic.error("Token not recognized");
            }
        }
        return tokens;
    }

    private char getNext() {
        return (source.charAt(current++));
    }

    private boolean isAtEnd() {
        if (current == source.length()) {
            return true;
        }
        return false;
    }

    private boolean isInt(char c) {
        return Character.isDigit(c);
    }
}