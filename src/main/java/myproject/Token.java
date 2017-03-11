package myproject;



class Token {
    private String type;
    private String lexeme;
    Token(String type, String lexeme) {
        this.type = type;
        this.lexeme = lexeme;
    }

    String getType() {
        return type;
    }

    String getLexeme() {
        return lexeme;
    }

    @Override
    public String toString() {
        return "Type : " + type;
    }
}
