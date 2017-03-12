package myproject;

public enum STATEMENT {
    IF("if", "STATEMENT_IF_STATEMENT"), ELSE("else", "STATEMENT_ELSE_STATEMENT"),
    FOR("for", "STATEMENT_FOR_STATEMENT"), FOREACH("foreach", "STATEMENT_FOREACH_STATEMENT"),
    IN("in", "STATEMENT_IN_STATEMENT"), WHILE("while", "STATEMENT_WHILE_STATEMENT"),
    DO("do", "STATEMENT_DO_STATEMENT"), SWITCH("switch", "STATEMENT_SWITCH_STATEMENT"),
    CASE("case", "STATEMENT_CASE_STATEMENT"), DEFAULT("default", "STATEMENT_DEFAULT_STATEMENT");

    private String statementLexeme;
    private String statementName;

    STATEMENT(String statementLexeme, String statementName) {
        this.statementLexeme = statementLexeme;
        this.statementName = statementName;
    }


    public String getStatementLexeme() {
        return statementLexeme;
    }

    public String getStatementName() {
        return statementName;
    }

}
