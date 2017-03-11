package myproject;


class Word extends Token {
    private String lexeme;

    Word(String type, String lexeme) {
        super(type, lexeme);
        this.lexeme = lexeme;
    }

    @Override
    public String toString() {
        return super.toString() + ", lexeme : " + lexeme;
    }
}
