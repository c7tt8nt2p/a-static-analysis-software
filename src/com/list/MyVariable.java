package com.list;

public class MyVariable {
    private String varType;
    private String varName;
    private String methodScope;

    public MyVariable(String varType, String varName) {
        this.varType = varType;
        this.varName = varName;
        this.methodScope = "CLASS";
    }

    public MyVariable(String varType, String varName, String methodScope) {
        this.varType = varType;
        this.varName = varName;
        this.methodScope = methodScope;
    }

    public String getVarType() {
        return varType;
    }

    public String getVarName() {
        return varName;
    }

    public String getMethodScope() {
        return methodScope;
    }
}
