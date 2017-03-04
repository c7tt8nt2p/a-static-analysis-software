package com.list;

import java.util.LinkedList;

public class MyMethod {
    private String accessModifier;
    private String staticMethod;
    private String returnType;
    private String methodName;
    private LinkedList<String> parameterType = new LinkedList<>();
    private LinkedList<String> parameterName = new LinkedList<>();

    private int startIndex, endIndex;

    public MyMethod(String accessModifier, String staticMethod, String returnType, String methodName,
                    LinkedList<String> parameterType, LinkedList<String> parameterName,
                    int startIndex, int endIndex) {
        this.accessModifier = accessModifier;
        this.staticMethod = staticMethod;
        this.returnType = returnType;
        this.methodName = methodName;
        this.parameterType.addAll(parameterType);
        this.parameterName.addAll(parameterName);
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public String getAccessModifier() {
        return accessModifier;
    }

    public String getStaticMethod() {
        return staticMethod;
    }

    public String getReturnType() {
        return returnType;
    }

    public String getMethodName() {
        return methodName;
    }

    public LinkedList<String> getParameterType() {
        return parameterType;
    }

    public LinkedList<String> getParameterName() {
        return parameterName;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public int getNumberOfParameter() {
        return (parameterType.size() == parameterName.size()) ? parameterType.size() : -1;
    }

    //****
}
