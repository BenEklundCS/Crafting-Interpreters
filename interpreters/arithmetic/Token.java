class Token {
    private TokenType token;
    private String value;

    Token(TokenType token, String value) {
        this.token = token;
        this.value = value;
    }
    
    public String toString() {
        return String.format("Token(%s, %s)", token, value);
    }
}