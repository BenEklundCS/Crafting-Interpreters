class Token {
    private TokenType token;
    private Object value;

    Token(TokenType token, Object value) {
        this.token = token;
        this.value = value;
    }
    
    public String toString() {
        return String.format("Token(%s, %s)", token, value);
    }
}