package interpreters.arithmetic;

enum TokenType {
    NUMBER,
    PLUS, MINUS, MULTIPLY, DIVIDE,
    LPAREN, RPAREN, EQUAL,
    EOF, LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE
}