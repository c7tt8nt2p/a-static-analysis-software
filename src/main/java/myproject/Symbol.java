package myproject;

public enum Symbol {

    OPERATOR_BITWISE_AND("&"), OPERATOR_LOGICAL_AND("&&"),
    OPERATOR_BITWISE_OR("|"), OPERATOR_LOGICAL_OR("||"),
    OPERATOR_BITWISE_XOR("^"), OPERATOR_BITWISE_NOT("~"),
    OPERATOR_ASSIGNMENT("="), OPERATOR_EQUAL("=="), OPERATOR_NOT_EQUAL("!="),
    OPERATOR_NOT("!"),
    OPERATOR_LT("<"), OPERATOR_LT_EQUAL("<="), OPERATOR_SHIFT_LEFT("<<"),
    OPERATOR_GT(">"), OPERATOR_GT_EQUAL(">="), OPERATOR_SHIFT_RIGHT(">>"),
    SLASH("/"), COMMENT("//"), MULTI_COMMENT("/*...*/"),
    LB("{"), RB("}"), LP("("), RP(")"), LSB("["), RSB("]"), SEMICOLON(";"), COLON(":"),
    OPERATOR_ADDITION("+"), OPERATOR_ADDITION_EQUAL("+="), OPERATOR_INCREMENT("++"),
    OPERATOR_SUBTRACTION("-"), OPERATOR_SUBTRACTION_EQUAL("-="), OPERATOR_DECREMENT("--"),
    OPERATOR_MULTIPLICATION("*"), OPERATOR_MULTIPLICATION_EQUAL("*="),
    OPERATOR_DIVISION("/"), OPERATOR_DIVISION_EQUAL("/="),
    OPERATOR_MODULUS("%"),   OPERATOR_MODULUS_EQUAL("%="),
    DOT("."), COMMA(","), STRING("\""), CHAR("'");

    private String symbolCharacter;

    Symbol(String symbolCharacter) {
        this.symbolCharacter = symbolCharacter;
    }

    public String getSymbolCharacter() {
        return symbolCharacter;
    }

}

