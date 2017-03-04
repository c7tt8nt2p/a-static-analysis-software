package com.list;


public class MyStatement {
    private String statementSymbol;
    private String statementName;

    public MyStatement(String statementSymbol, String statementName) {
        this.statementSymbol = statementSymbol;
        this.statementName = statementName;
    }

    public String getStatementSymbol() {
        return statementSymbol;
    }

    public String getStatementName() {
        return statementName;
    }
}
