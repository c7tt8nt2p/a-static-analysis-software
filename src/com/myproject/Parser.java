package com.myproject;

import com.list.*;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Parser {
    private List<Token> lexemeTable = new ArrayList<>();
    private List<MyMethod> allMethod = new ArrayList<>();
    private List<MyVariable> allVariable = new ArrayList<>(); //Temp only
    private List<MyVariable> allGlobalVariable = new LinkedList<>();
    private List<MyVariable> allLocalVariable = new LinkedList<>();

    private List<MyStatement> allStatement = new ArrayList<>();
    private List<MyOperator> allOperator = new ArrayList<>();

    private List<MyPair<String, Integer>> theOperatorList = new ArrayList<>();
    private List<MyPair<String, Integer>> theOperandList = new ArrayList<>();

    // check the difference between while and do...while -> used for do...while block and gather all statements.
    private boolean doWhileFlag = false;

    private List<Node> allBlock = new ArrayList<>();
    private StringBuilder aStatement = new StringBuilder();
    private List<String> statementList = new ArrayList<>();

    private boolean ignoreBracket = true; //Ignore Bracket in array instantiation.
    //private static List<Node> parentNode = new ArrayList<>();

    private String methodScope = "NULL";

    private String filePath;

    public Parser(String filePath) {
        this.filePath = filePath;
    }

    public String doParse() {
        String aPath = filePath;
        try {
            Lexer myLex = new Lexer(aPath);
            Token tempToken = myLex.getNextToken();
            while (tempToken != null) {
                if (tempToken.getType().equalsIgnoreCase("STATEMENT_IF_STATEMENT") &&
                        lexemeTable.get(lexemeTable.size() - 1).getType()
                                .equalsIgnoreCase("STATEMENT_ELSE_STATEMENT")) {
                    //Detect else if statement.
                    lexemeTable.remove(lexemeTable.size() - 1);
                    tempToken = new Word("STATEMENT_ELSE_IF_STATEMENT", "else if");
                } else if ((tempToken.getType().equalsIgnoreCase("NUM") ||
                        tempToken.getType().equalsIgnoreCase("REAL")) &&
                        lexemeTable.get(lexemeTable.size() - 1).getType().equalsIgnoreCase("OPERATOR_SUBTRACTION")) {
                    //Detect negative number.
                    String[] negativeNum = {"OPERATOR_ASSIGNMENT", "LP", "COMMA", "RETURN", "OPERATOR_ADDITION",
                            "OPERATOR_SUBTRACTION", "OPERATOR_MULTIPLICATION", "OPERATOR_DIVISION",
                            "OPERATOR_MODULUS", "OPERATOR_INCREMENT", "OPERATOR_DECREMENT", "OPERATOR_ADDITION_EQ",
                            "OPERATOR_SUBTRACTION_EQ", "OPERATOR_MULTIPLICATION_EQ", "OPERATOR_DIVISION_EQ",
                            "OPERATOR_MODULUS_EQ", "OPERATOR_GT", "OPERATOR_LT", "OPERATOR_SHIFT_RIGHT",
                            "OPERATOR_SHIFT_LEFT", "OPERATOR_GTE", "OPERATOR_LTE", "OPERATOR_EQ"};
                    //String[] nonNegativeNum = {"IDENTIFIER/UNDEFINED", "RP", "NUM", "REAL"};
                    for (String aString : negativeNum) {
                        if (lexemeTable.get(lexemeTable.size() - 2).getType().equalsIgnoreCase(aString)) {
                            if (tempToken.getType().equalsIgnoreCase("NUM")) {
                                int tempNum = Integer.valueOf(tempToken.getLexeme()) * -1;
                                lexemeTable.remove(lexemeTable.size() - 1);
                                tempToken = new Num("NUM", tempNum);
                            } else if (tempToken.getType().equalsIgnoreCase("REAL")) {
                                double tempReal = Integer.valueOf(tempToken.getLexeme()) * -1;
                                lexemeTable.remove(lexemeTable.size() - 1);
                                tempToken = new Real("NUM", tempReal);
                            } else {
                                System.err.println("Error during detecting negative integer, lexeme : " + tempToken.getLexeme());
                            }
                            break;
                        }
                    }
                }
                lexemeTable.add(tempToken);
                tempToken = myLex.getNextToken();
            }
            for (Token key : lexemeTable) {
                System.out.println("TYPE : " + key.getType() + "  LEXEME : " + key.getLexeme() + " --> " + lexemeTable.indexOf(key));
            }
        } catch (FileNotFoundException e) {
            System.err.println("[Parser.java : 59] File not found.");
            return "Error";
            //e.printStackTrace();
        } catch (IOException e) {
            System.err.println("[Parser.java : 63] IOException.");
            return "Error";
        }

        getTotalMethod();
        getVariable();
        getLocalVariable();
        getStatement();
        getOperator();
        doHalsteads();
        prepareNode(); // must be after calling get method otherwise throw an exception.

        doPrintStuff();

        return "Done";
    }

    private void doPrintStuff() {
        System.out.println("===================== METHODS =====================");
        allMethod.forEach((i) ->
                System.out.println(i.getAccessModifier() + " " +
                        i.getStaticMethod() + " " +
                        i.getReturnType() + " " +
                        i.getMethodName() + " " +
                        i.getParameterType() + " " +
                        i.getParameterName() + " " +
                        i.getStartIndex() + " " +
                        i.getEndIndex()));
        System.out.println("=================== GLOBAL VARS ===================");
        /*allVariable.forEach((i) ->
                System.out.println(i.getVarType() + " " + i.getVarName() + " " + i.getMethodScope()));
        System.out.println("----");*/
        allGlobalVariable.forEach((i) ->
                System.out.println(i.getVarType() + " " + i.getVarName() + " " + i.getMethodScope()));
        System.out.println("==================== LOCAL VARS ===================");
        allLocalVariable.forEach((i) ->
                System.out.println(i.getVarType() + " " + i.getVarName() + " " + i.getMethodScope()));
        System.out.println("==================== OPERATORS ====================");
        allOperator.forEach((i) ->
                System.out.println(i.getOperatorSymbol() + " " + i.getOperatorName()));
        System.out.println("==================== STATEMENTS ===================");
        allStatement.forEach((i) ->
                System.out.println(i.getStatementSymbol() + " " + i.getStatementName()));
        System.out.println("====================== BLOCKS =====================");
        allBlock.forEach((i) -> System.out.println("{" + i.getMethodScope() + "} : " + i.getData()));
        System.out.println("==================== LOOPS ===================");
        System.out.println("Number of Loops : " + getLoop());
    }

    public static void main(String[] args) throws IOException {
        System.out.println("///////////////////////////DEBUGGING///////////////////////////");
        //For debugging.
        /*String filePath = "D:\\IdeaProjects\\SeniorProject_FX\\src\\com\\myproject\\resources\\EX_Code.cs";
        Lexer myLex = new Lexer(filePath);
        Token tempToken = myLex.getNextToken();
        while (tempToken != null) {
            //tableOfLexemes.put(tempToken.getLexeme(), tempToken.getType());
            lexemeTable.add(tempToken);
            //System.out.println("TYPE : " + tempToken.getType() + ", LEXEME : " + tempToken.getLexeme());
            tempToken = myLex.getNextToken();
        }
        for (Token key : lexemeTable) {
            System.out.println("TYPE : " + key.getType() + "  LEXEME : " + key.getLexeme() + " --> " + lexemeTable.indexOf(key));
        }
        getTotalMethod();
        getVariable();
        getLocalVariable();
        getStatement();
        getOperator();
        getOperand();
        prepareNode(); // must be after calling get method otherwise throw an exception.

        doPrintStuff();

        doPrintStuff();*/
    }

    //****************************************************************************************************
    //****************************************************************************************************
    private void getTotalMethod() {
        String accessModifier = "(default)";
        String staticMethod = "";
        String returnType = "";
        String methodName = "";
        LinkedList<String> parameterType = new LinkedList<>();
        LinkedList<String> parameterName = new LinkedList<>();
        /*
            <Access Specifier> <Return Type> <Method Name>(Parameter List)
            {
               Method Body
            }
         */
        int startIndex = -1;
        int endIndex;
        int leftBracket = 0;
        int rightBracket = 0;
        boolean isBalanceBracket;

        int currentState = 0;
        for (Token aLexemeTable : lexemeTable) {
            switch (currentState) {
                case 0:
                    accessModifier = "(default)";
                    staticMethod = "";
                    returnType = "";
                    methodName = "";
                    parameterType.clear();
                    parameterName.clear();

                    if (aLexemeTable.getType().equalsIgnoreCase("STATIC")) {
                        // Starts with static
                        staticMethod = "static";
                        currentState = 1;
                    } else if (aLexemeTable.getType().equalsIgnoreCase("MODIFIER")) {
                        // Starts with modifier
                        accessModifier = aLexemeTable.getLexeme();
                        currentState = 2;
                    } else if (aLexemeTable.getType().equalsIgnoreCase("VARIABLE") ||
                            (aLexemeTable.getType().equalsIgnoreCase("VOID")) ||
                            (aLexemeTable.getType().equalsIgnoreCase("IDENTIFIER/UNDEFINED"))) {
                        // Starts with return type
                        staticMethod = "(non-static)";
                        returnType = aLexemeTable.getLexeme();
                        currentState = 4;
                    } else {
                        currentState = 0;
                    }
                    break;
                case 1:
                    if (aLexemeTable.getType().equalsIgnoreCase("MODIFIER")) {
                        accessModifier = aLexemeTable.getLexeme();
                        currentState = 3;
                    } else if (aLexemeTable.getType().equalsIgnoreCase("VOID") ||
                            aLexemeTable.getType().equalsIgnoreCase("VARIABLE") ||
                            aLexemeTable.getType().equalsIgnoreCase("IDENTIFIER/UNDEFINED")) {
                        returnType = aLexemeTable.getLexeme();
                        currentState = 4;
                    } else {
                        currentState = 0;
                    }
                    break;
                case 2:
                    if (aLexemeTable.getType().equalsIgnoreCase("STATIC")) {
                        staticMethod = "static";
                        currentState = 3;
                    } else {
                        staticMethod = "(non-static)";
                        if (aLexemeTable.getType().equalsIgnoreCase("VOID") ||
                                aLexemeTable.getType().equalsIgnoreCase("VARIABLE") ||
                                aLexemeTable.getType().equalsIgnoreCase("IDENTIFIER/UNDEFINED")) {
                            returnType = aLexemeTable.getLexeme();
                            currentState = 4;
                        } else {
                            currentState = 0;
                        }
                    }
                    break;
                case 3:
                    if (aLexemeTable.getType().equalsIgnoreCase("VOID") ||
                            aLexemeTable.getType().equalsIgnoreCase("VARIABLE") ||
                            aLexemeTable.getType().equalsIgnoreCase("IDENTIFIER/UNDEFINED")) {
                        returnType = aLexemeTable.getLexeme();
                        currentState = 4;
                    } else {
                        currentState = 0;
                    }
                    break;
                case 4:
                    // Method Name
                    if (aLexemeTable.getType().equalsIgnoreCase("IDENTIFIER/UNDEFINED")) {
                        methodName = aLexemeTable.getLexeme();
                        currentState = 5;
                    } else {
                        currentState = 0;
                    }
                    break;
                case 5:
                    if (aLexemeTable.getType().equalsIgnoreCase("LP")) {
                        currentState = 6;
                    } else {
                        currentState = 0;
                    }
                    break;
                case 6:
                    // Parameter List
                    if (aLexemeTable.getType().equalsIgnoreCase("VARIABLE") ||
                            aLexemeTable.getType().equalsIgnoreCase("IDENTIFIER/UNDEFINED")) {
                        parameterType.add(aLexemeTable.getLexeme());
                        currentState = 7;
                    } else if (aLexemeTable.getType().equalsIgnoreCase("RP")) {
                        currentState = 10;
                    } else {
                        currentState = 0;
                    }
                    break;
                case 7:
                    if (aLexemeTable.getType().equalsIgnoreCase("IDENTIFIER/UNDEFINED")) {
                        parameterName.add(aLexemeTable.getLexeme());
                        currentState = 8;
                    } else if (aLexemeTable.getType().equalsIgnoreCase("LSB")) {
                        currentState = 11;
                    } else {
                        currentState = 0;
                    }
                    break;
                case 8:
                    if (aLexemeTable.getType().equalsIgnoreCase("COMMA")) {
                        currentState = 9;
                    } else if (aLexemeTable.getType().equalsIgnoreCase("RP")) {
                        currentState = 10;
                    } else {
                        currentState = 0;
                    }
                    break;
                case 9:
                    if (aLexemeTable.getType().equalsIgnoreCase("VARIABLE") ||
                            aLexemeTable.getType().equalsIgnoreCase("IDENTIFIER/UNDEFINED")) {
                        parameterType.add(aLexemeTable.getLexeme());
                        currentState = 7;
                    } else {
                        currentState = 0;
                    }
                    break;
                case 10:
                    if (aLexemeTable.getType().equalsIgnoreCase("LB")) {
                        ++leftBracket;
                        startIndex = lexemeTable.indexOf(aLexemeTable);
                        currentState = 12;
                    } else {
                        currentState = 0;
                    }
                    break;
                case 11:
                    if (aLexemeTable.getType().equalsIgnoreCase("RSB")) {
                        parameterType.set(parameterType.size() - 1, parameterType.get(parameterType.size() - 1).concat("[]"));
                        currentState = 7;
                    } else if (aLexemeTable.getType().equalsIgnoreCase("COMMA")) {
                        parameterType.set(parameterType.size() - 1, parameterType.get(parameterType.size() - 1).concat("[]"));
                        currentState = 11;
                    } else {
                        currentState = 0;
                    }
                    break;
                case 12: // find where is the end of a method
                    if (aLexemeTable.getType().equalsIgnoreCase("LB"))
                        ++leftBracket;
                    else if (aLexemeTable.getType().equalsIgnoreCase("RB"))
                        ++rightBracket;
                    isBalanceBracket = leftBracket == rightBracket;
                    if (isBalanceBracket) {
                        endIndex = lexemeTable.indexOf(aLexemeTable);
                        allMethod.add(new MyMethod(accessModifier, staticMethod, returnType, methodName, parameterType, parameterName, startIndex, endIndex));
                        currentState = 0;
                    } else {
                        currentState = 12;
                    }
                    break;
                default:
                    break;
                //System.out.println(accessModifier + " " + staticMethod + " " + returnType + " " + methodName);
            }
        }
    }

    //****************************************************************************************************
    //****************************************************************************************************
    private void getVariable() {
        int currentState = 0;
        String varType = "";
        String varName = "";
        for (Token aLexemeTable : lexemeTable) {
            //System.out.println(aLexemeTable.getLexeme());
            switch (currentState) {
                case 0:
                    varType = "";
                    varName = "";
                    if (aLexemeTable.getType().equalsIgnoreCase("VARIABLE")) {
                        if (lexemeTable.indexOf(aLexemeTable) == 0) {
                            currentState = 1;
                            varType = aLexemeTable.getLexeme();
                        } else if (lexemeTable.indexOf(aLexemeTable) != 0) {
                            // To avoid ArrayIndexOutOfBoundException = -1 !!!!!!!!!
                            // To void variable in statement such as for loop
                            if (lexemeTable.get(lexemeTable.indexOf(aLexemeTable) - 1).getType().equalsIgnoreCase("LP") ||
                                    lexemeTable.get(lexemeTable.indexOf(aLexemeTable) - 1).getType().equalsIgnoreCase("RP")) {
                                currentState = 0;
                            } else {
                                currentState = 1;
                                varType = aLexemeTable.getLexeme();
                            }
                        }
                    } else {
                        currentState = 0;
                    }
                    break;
                case 1:
                    if (aLexemeTable.getType().equalsIgnoreCase("IDENTIFIER/UNDEFINED")) {
                        currentState = 2;
                        varName = aLexemeTable.getLexeme();
                    } else if (aLexemeTable.getType().equalsIgnoreCase("LSB")) {
                        // For array
                        currentState = 6;
                    } else {
                        currentState = 0;
                    }
                    break;
                case 2:
                    if (aLexemeTable.getType().equalsIgnoreCase("COMMA")) {
                        currentState = 1;
                        allVariable.add(new MyVariable(varType, varName));
                    } else if (aLexemeTable.getType().equalsIgnoreCase("SEMICOLON")) {
                        //ACCEPT STATE
                        allVariable.add(new MyVariable(varType, varName));
                        currentState = 0;
                    } else if (aLexemeTable.getType().equalsIgnoreCase("OPERATOR_ASSIGNMENT")) {
                        currentState = 3;
                    } else {
                        currentState = 0;
                    }
                    break;
                case 3:
                    if (aLexemeTable.getType().equalsIgnoreCase("REAL") ||
                            aLexemeTable.getType().equalsIgnoreCase("NUM") ||
                            aLexemeTable.getType().equalsIgnoreCase("FUNC_CALL") || // For .xx and func call
                            aLexemeTable.getType().equalsIgnoreCase("STRING") ||
                            aLexemeTable.getType().equalsIgnoreCase("CHAR")) {
                        currentState = 4;
                    } else if (aLexemeTable.getType().equalsIgnoreCase("OPERATOR_SUBTRACTION")) {
                        currentState = 10;
                    } else if (aLexemeTable.getType().equalsIgnoreCase("IDENTIFIER/UNDEFINED") ||
                            aLexemeTable.getType().equalsIgnoreCase("VARIABLE") ||
                            aLexemeTable.getType().equalsIgnoreCase("LB") ||
                            aLexemeTable.getType().equalsIgnoreCase("LP")) {
                        currentState = 5;
                    } else {
                        currentState = 0;
                    }
                    break;
                case 4:
                    if (aLexemeTable.getType().equalsIgnoreCase("COMMA")) {
                        currentState = 1;
                        allVariable.add(new MyVariable(varType, varName));
                    } else if (aLexemeTable.getType().equalsIgnoreCase("IDENTIFIER/UNDEFINED")) {
                        currentState = 4; // for variable suffix
                        /*if (aLexemeTable.getLexeme().equalsIgnoreCase("m")) { // for decimal suffix.
                            currentState = 4;
                        }else {
                            System.out.println("OK");
                            varName = aLexemeTable.getLexeme();
                            allVariable.add(new MyVariable(varType, varName));
                            currentState = 4;
                        }*/
                    } else if (aLexemeTable.getType().equalsIgnoreCase("SEMICOLON")) {
                        //ACCEPT STATE
                        allVariable.add(new MyVariable(varType, varName));
                        currentState = 0;
                    } else {
                        currentState = 0;
                    }
                    break;
                case 5:
                    if (!aLexemeTable.getType().equalsIgnoreCase("SEMICOLON")) {
                        currentState = 5;
                    } else if (aLexemeTable.getType().equalsIgnoreCase("SEMICOLON")) {
                        //ACCEPT STATE
                        allVariable.add(new MyVariable(varType, varName));
                        currentState = 0;
                    } else {
                        currentState = 0;
                    }
                    break;
                //6-9 -> For an array
                case 6:
                    if (aLexemeTable.getType().equalsIgnoreCase("RSB")) {
                        currentState = 7;
                        varType = varType + "[]";
                    } else if (aLexemeTable.getType().equalsIgnoreCase("COMMA")) {
                        currentState = 6;
                        varType = varType + "[]";
                    } else {
                        currentState = 0;
                    }
                    break;
                case 7:
                    if (aLexemeTable.getType().equalsIgnoreCase("IDENTIFIER/UNDEFINED")) {
                        currentState = 8;
                        varName = aLexemeTable.getLexeme();
                    } else if (aLexemeTable.getType().equalsIgnoreCase("LSB")) {
                        currentState = 6;
                    } else {
                        currentState = 0;
                    }
                    break;
                case 8:
                    if (aLexemeTable.getType().equalsIgnoreCase("OPERATOR_ASSIGNMENT")) {
                        currentState = 9;
                    } else if (aLexemeTable.getType().equalsIgnoreCase("COMMA")) {
                        allVariable.add(new MyVariable(varType, varName));
                        currentState = 1;
                    } else if (aLexemeTable.getType().equalsIgnoreCase("SEMICOLON")) {
                        //ACCEPT STATE
                        allVariable.add(new MyVariable(varType, varName));
                        currentState = 0;
                    } else {
                        currentState = 0;
                    }
                    break;
                case 9:
                    if (!aLexemeTable.getType().equalsIgnoreCase("RB")) {
                        currentState = 9;
                    } else {
                        //ACCEPT STATE
                        allVariable.add(new MyVariable(varType, varName));
                        //System.out.println(varType + " " + varName);
                        currentState = 0;
                    }
                    break;
                case 10: // for -x
                    if (aLexemeTable.getType().equalsIgnoreCase("REAL") ||
                            aLexemeTable.getType().equalsIgnoreCase("NUM")) {
                        currentState = 4;
                    } else {
                        currentState = 0;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    //****************************************************************************************************
    //****************************************************************************************************
    @SuppressWarnings("ConstantConditions")
    private void getLocalVariable() {
        int currentState = 0;
        String methodName = "";
        String localVarType = "";
        String localVarName = "";
        int openBrace = 1;
        int closeBrace = 0;
        for (Token aLexemeTable : lexemeTable) {
            if (openBrace == closeBrace) {
                currentState = 0;
                openBrace = 1;
                closeBrace = 0;
            }
            if (currentState >= 8) {
                if (aLexemeTable.getType().equalsIgnoreCase("LB")) {
                    ++openBrace;
                } else if (aLexemeTable.getType().equalsIgnoreCase("RB")) {
                    ++closeBrace;
                }
            }
            switch (currentState) {
                case 0:
                    methodName = "";
                    if (aLexemeTable.getType().equalsIgnoreCase("STATIC")) {
                        // Starts with static
                        currentState = 1;
                    } else if (aLexemeTable.getType().equalsIgnoreCase("MODIFIER")) {
                        // Starts with modifier
                        currentState = 2;
                    } else if (aLexemeTable.getType().equalsIgnoreCase("VARIABLE") ||
                            (aLexemeTable.getType().equalsIgnoreCase("VOID")) ||
                            (aLexemeTable.getType().equalsIgnoreCase("IDENTIFIER/UNDEFINED"))) {
                        // Starts with return type
                        currentState = 4;
                    } else {
                        currentState = 0;
                    }

                    break;
                case 1:
                    if (aLexemeTable.getType().equalsIgnoreCase("MODIFIER")) {
                        currentState = 3;
                    } else if (aLexemeTable.getType().equalsIgnoreCase("VOID") ||
                            aLexemeTable.getType().equalsIgnoreCase("VARIABLE") ||
                            aLexemeTable.getType().equalsIgnoreCase("IDENTIFIER/UNDEFINED")) {
                        currentState = 4;
                    } else {
                        currentState = 0;
                    }
                    break;
                case 2:
                    if (aLexemeTable.getType().equalsIgnoreCase("STATIC")) {
                        currentState = 3;
                    } else {
                        if (aLexemeTable.getType().equalsIgnoreCase("VOID") ||
                                aLexemeTable.getType().equalsIgnoreCase("VARIABLE") ||
                                aLexemeTable.getType().equalsIgnoreCase("IDENTIFIER/UNDEFINED")) {
                            currentState = 4;
                        } else {
                            currentState = 0;
                        }
                    }
                    break;
                case 3:
                    if (aLexemeTable.getType().equalsIgnoreCase("VOID") ||
                            aLexemeTable.getType().equalsIgnoreCase("VARIABLE") ||
                            aLexemeTable.getType().equalsIgnoreCase("IDENTIFIER/UNDEFINED")) {
                        currentState = 4;
                    } else {
                        currentState = 0;
                    }
                    break;
                case 4:
                    // Method Name
                    if (aLexemeTable.getType().equalsIgnoreCase("IDENTIFIER/UNDEFINED")) {
                        methodName = aLexemeTable.getLexeme();
                        currentState = 5;
                    } else {
                        currentState = 0;
                    }
                    break;
                case 5:
                    if (aLexemeTable.getType().equalsIgnoreCase("LP")) {
                        currentState = 6;
                    } else {
                        currentState = 0;
                    }
                    break;
                case 6:
                    // Parameter List
                    /*if (aLexemeTable.getType().equalsIgnoreCase("VARIABLE") ||
                            aLexemeTable.getType().equalsIgnoreCase("IDENTIFIER/UNDEFINED") ||
                            aLexemeTable.getType().equalsIgnoreCase("LSB") ||
                            aLexemeTable.getType().equalsIgnoreCase("RSB") ||
                            aLexemeTable.getType().equalsIgnoreCase("COMMA")) {
                        currentState = 6;
                    } else if (aLexemeTable.getType().equalsIgnoreCase("RP")) {
                        currentState = 7;
                    } else {
                        currentState = 0;
                    }*/
                    if (!aLexemeTable.getType().equalsIgnoreCase("RP")) {
                        currentState = 6;
                    } else if (aLexemeTable.getType().equalsIgnoreCase("RP")) {
                        currentState = 7;
                    } else {
                        currentState = 0;
                    }
                    break;
                case 7:
                    if (aLexemeTable.getType().equalsIgnoreCase("LB")) {
                        // Is a method
                        currentState = 8;
                    } else {
                        currentState = 0;
                    }
                    break;
                case 8:
                    localVarType = "";
                    localVarName = "";
                    if (aLexemeTable.getType().equalsIgnoreCase("VARIABLE")) {
                        if (lexemeTable.indexOf(aLexemeTable) == 0) {
                            currentState = 9;
                            localVarType = aLexemeTable.getLexeme();
                        } else if (lexemeTable.indexOf(aLexemeTable) != 0) {
                            // To avoid ArrayIndexOutOfBoundException = -1 !!!!!!!!!
                            // To void variable in statement
                            if (lexemeTable.get(lexemeTable.indexOf(aLexemeTable) - 1).getType().equalsIgnoreCase("LP") ||
                                    lexemeTable.get(lexemeTable.indexOf(aLexemeTable) - 1).getType().equalsIgnoreCase("RP")) {
                                currentState = 8;
                            } else {
                                currentState = 9;
                                localVarType = aLexemeTable.getLexeme();
                            }
                        }
                    } else {
                        //System.out.println("OKK " + aLexemeTable.getLexeme());
                        currentState = 8;
                    }
                    break;
                case 9:
                    if (aLexemeTable.getType().equalsIgnoreCase("IDENTIFIER/UNDEFINED")) {
                        currentState = 10;
                        localVarName = aLexemeTable.getLexeme();
                    } else if (aLexemeTable.getType().equalsIgnoreCase("LSB")) {
                        // For array
                        currentState = 14;
                    } else {
                        currentState = 8;
                    }
                    break;
                case 10:
                    if (aLexemeTable.getType().equalsIgnoreCase("COMMA")) {
                        currentState = 9;
                        allLocalVariable.add(new MyVariable(localVarType, localVarName, methodName));
                    } else if (aLexemeTable.getType().equalsIgnoreCase("SEMICOLON")) {
                        //ACCEPT STATE
                        allLocalVariable.add(new MyVariable(localVarType, localVarName, methodName));
                        currentState = 8;
                    } else if (aLexemeTable.getType().equalsIgnoreCase("OPERATOR_ASSIGNMENT")) {
                        currentState = 11;
                    } else {
                        currentState = 8;
                    }
                    break;
                case 11:
                    if (aLexemeTable.getType().equalsIgnoreCase("REAL") ||
                            aLexemeTable.getType().equalsIgnoreCase("NUM") ||
                            aLexemeTable.getType().equalsIgnoreCase("FUNC_CALL") || // For .xx and func call
                            aLexemeTable.getType().equalsIgnoreCase("STRING") ||
                            aLexemeTable.getType().equalsIgnoreCase("CHAR")) {
                        currentState = 12;
                    } else if (aLexemeTable.getType().equalsIgnoreCase("OPERATOR_SUBTRACTION")) {
                        currentState = 18;
                    } else if (aLexemeTable.getType().equalsIgnoreCase("IDENTIFIER/UNDEFINED") ||
                            aLexemeTable.getType().equalsIgnoreCase("VARIABLE") ||
                            aLexemeTable.getType().equalsIgnoreCase("LB") ||
                            aLexemeTable.getType().equalsIgnoreCase("LP")) {
                        currentState = 13;
                    } else {
                        currentState = 8;
                    }
                    break;
                case 12:
                    if (aLexemeTable.getType().equalsIgnoreCase("COMMA")) {
                        currentState = 9;
                        allLocalVariable.add(new MyVariable(localVarType, localVarName, methodName));
                    } else if (aLexemeTable.getType().equalsIgnoreCase("IDENTIFIER/UNDEFINED")) {
                        currentState = 12; // for variable suffix
                    } else if (aLexemeTable.getType().equalsIgnoreCase("SEMICOLON")) {
                        //ACCEPT STATE
                        allLocalVariable.add(new MyVariable(localVarType, localVarName, methodName));
                        currentState = 8;
                    } else {
                        currentState = 8;
                    }
                    break;
                case 13:
                    if (!aLexemeTable.getType().equalsIgnoreCase("SEMICOLON")) {
                        currentState = 13;
                    } else if (aLexemeTable.getType().equalsIgnoreCase("SEMICOLON")) {
                        //ACCEPT STATE
                        allLocalVariable.add(new MyVariable(localVarType, localVarName, methodName));
                        currentState = 8;
                    } else {
                        currentState = 8;
                    }
                    break;
                //14-17 -> For an array
                case 14:
                    if (aLexemeTable.getType().equalsIgnoreCase("RSB")) {
                        currentState = 15;
                        localVarType = localVarType + "[]";
                    } else if (aLexemeTable.getType().equalsIgnoreCase("COMMA")) {
                        currentState = 14;
                        localVarType = localVarType + "[]";
                    } else {
                        currentState = 8;
                    }
                    break;
                case 15:
                    if (aLexemeTable.getType().equalsIgnoreCase("IDENTIFIER/UNDEFINED")) {
                        currentState = 16;
                        localVarName = aLexemeTable.getLexeme();
                    } else if (aLexemeTable.getType().equalsIgnoreCase("LSB")) {
                        currentState = 14;
                    } else {
                        currentState = 8;
                    }
                    break;
                case 16:
                    if (aLexemeTable.getType().equalsIgnoreCase("OPERATOR_ASSIGNMENT")) {
                        currentState = 17;
                    } else if (aLexemeTable.getType().equalsIgnoreCase("SEMICOLON")) {
                        //ACCEPT STATE
                        allLocalVariable.add(new MyVariable(localVarType, localVarName, methodName));
                        currentState = 8;
                    } else {
                        currentState = 8;
                    }
                    break;
                case 17:
                    if (!aLexemeTable.getType().equalsIgnoreCase("SEMICOLON")) {
                        currentState = 17;
                    } else {
                        //ACCEPT STATE
                        allLocalVariable.add(new MyVariable(localVarType, localVarName, methodName));
                        //System.out.println(varType + " " + varName);
                        currentState = 8;
                    }
                    break;
                case 18: // for -x
                    if (aLexemeTable.getType().equalsIgnoreCase("REAL") ||
                            aLexemeTable.getType().equalsIgnoreCase("NUM")) {
                        currentState = 12;
                    } else {
                        currentState = 8;
                    }
                    break;
                default:
                    break;
            }
        }
        allGlobalVariable.addAll(allVariable);
        //System.out.println(allGlobalVariable.toString());
        List<MyVariable> varAddressTemp = new LinkedList<>(); // Store deleted object address
        for (MyVariable aAllLocalVariable : allLocalVariable) {
            for (int j = allVariable.size() - 1; j >= 0; j--) {
                if (aAllLocalVariable.getVarType().equals(allVariable.get(j).getVarType()) &&
                        aAllLocalVariable.getVarName().equals(allVariable.get(j).getVarName())) {
                    if (!varAddressTemp.contains(allVariable.get(j))) {
                        allGlobalVariable.remove(allVariable.get(j));
                        varAddressTemp.add(allVariable.get(j)); // Prevent remove old object
                        break;
                    }
                    // Remove by object address
                   /* System.out.println("... " + allVariable.get(j) + " ..... " + allVariable.get(j).getVarType() + " ....." + allVariable.get(j).getVarName());
                    allGlobalVariable.remove(allVariable.get(j));
                    System.out.println(allGlobalVariable.toString());
                    break;*/
                }
            }
        }
        /*for (int i = 0; i < allLocalVariable.size(); i++) {
            for (int j = allVariable.size() - 1; j >= 0; j--) {
               if (allLocalVariable.get(i).getVarType().equals(allVariable.get(j).getVarType()) &&
                       allLocalVariable.get(i).getVarName().equals(allVariable.get(j).getVarName())) {
                   // Remove by object address
                   allGlobalVariable.remove(allVariable.get(j));
                   break;
               }
            }
        }*/
    }

    //****************************************************************************************************
    //****************************************************************************************************
    private void getOperator() {
        int currentState = 0;
        String operatorSymbol;
        String operatorName;
        for (Token aLexemeTable : lexemeTable) {
            switch (currentState) {
                case 0:
                    operatorName = "";
                    if (aLexemeTable.getType().contains("OPERATOR_")) {
                        try {
                            String[] parts = aLexemeTable.getType().split("_");
                            operatorSymbol = aLexemeTable.getLexeme();
                            for (int i = 1; i < parts.length; i++) {
                                if (i == 1)
                                    operatorName = operatorName + parts[i];
                                else
                                    operatorName = operatorName + "_" + parts[i];
                            }
                            if (operatorSymbol.equals("<")) {
                                currentState = 1;
                            } else {
                                allOperator.add(new MyOperator(operatorSymbol, operatorName));
                            }
                        } catch (ArrayIndexOutOfBoundsException c) {
                            System.err.println("Somethings gone wrong.");
                        }
                    } else {
                        currentState = 0;
                    }
                    break;
                case 1:
                    if (aLexemeTable.getType().equalsIgnoreCase("IDENTIFIER/UNDEFINED") ||
                            aLexemeTable.getType().equalsIgnoreCase("VARIABLE")) {
                        currentState = 2;
                    } else if (aLexemeTable.getType().equalsIgnoreCase("OPERATOR_GT")) {
                        currentState = 0;
                    } else {
                        allOperator.add(new MyOperator("<", "OPERATOR_LT"));
                        currentState = 0;
                    }
                    break;
                case 2:
                    if (aLexemeTable.getType().equalsIgnoreCase("OPERATOR_GT")) {
                        currentState = 0; // Avoid object type or wildcard <>, <int>
                    } else {
                        allOperator.add(new MyOperator("<", "OPERATOR_LT"));
                        currentState = 0;
                    }
                    break;
                default:
                    currentState = 0;
                    break;
            }
        }
    }

    private void doHalsteads() {
        /*
        *****************************************************
        n1 = the number of distinct operators.
        n2 = the number of distinct operands.
        N1 = the total number of operators.
        N2 = the total number of operands.
        *****************************************************
        */
        List<MyVariable> theVariable = new ArrayList<>();
        //Add all globalvars.

        for (MyMethod aMethod : allMethod) {
            //Reset operand when changes method scope.
            theVariable.clear();
            theVariable.addAll(allGlobalVariable);

            //Add local vars in the current scope to theVariable.
            for (MyVariable aVariable : allLocalVariable) {
                if (aVariable.getMethodScope().equalsIgnoreCase(aMethod.getMethodName()))
                    theVariable.add(aVariable);
            }

            //Add method vars in the current scope to theVariable.
            for (int i = 0; i < aMethod.getParameterType().size(); i++) {
                theVariable.add(new MyVariable(aMethod.getParameterType().get(i),
                        aMethod.getParameterName().get(i)));
            }

            //Start with method header.
            doAnalyzeMethodHeader(aMethod);

            //Follow by method body.
            for (int i = aMethod.getStartIndex(); i <= aMethod.getEndIndex(); i++) {
                if (!lexemeTable.get(i).getType().equalsIgnoreCase("COMMENT") &&
                        !lexemeTable.get(i).getType().equalsIgnoreCase("MULTI_COMMENT") &&
                        !lexemeTable.get(i).getType().equalsIgnoreCase("RB") &&
                        !lexemeTable.get(i).getType().equalsIgnoreCase("RP") &&
                        !lexemeTable.get(i).getType().equalsIgnoreCase("RSB")) {
                    //Ignore comments.
                    if (lexemeTable.get(i).getType().equalsIgnoreCase("IDENTIFIER/UNDEFINED")) {
                        //Check whether identifier equals to variable name. If so, it is operand. If not, it is operator (ex. Cosole, Writeline).
                        boolean doesExist = false;
                        for (MyVariable aVariable : theVariable) {
                            if (aVariable.getVarName().equals(lexemeTable.get(i).getLexeme())) {
                                doesExist = true;
                                doAddOperand(lexemeTable.get(i).getLexeme());
                                break;
                            }
                        }
                        if (!doesExist)
                            doAddOperator(lexemeTable.get(i).getLexeme());
                    } else if (lexemeTable.get(i).getType().equalsIgnoreCase("NUM") ||
                            lexemeTable.get(i).getType().equalsIgnoreCase("REAL") ||
                            lexemeTable.get(i).getType().equalsIgnoreCase("CHAR") ||
                            lexemeTable.get(i).getType().equalsIgnoreCase("STRING")) {
                        doAddOperand(lexemeTable.get(i).getLexeme());
                    } else if (lexemeTable.get(i).getType().equalsIgnoreCase("LB")) {
                        doAddOperator(lexemeTable.get(i).getLexeme().concat("}"));
                    } else if (lexemeTable.get(i).getType().equalsIgnoreCase("LP")) {
                        doAddOperator(lexemeTable.get(i).getLexeme().concat(")"));
                    } else if (lexemeTable.get(i).getType().equalsIgnoreCase("LSB")) {
                        doAddOperator(lexemeTable.get(i).getLexeme().concat("]"));
                    } else {
                        doAddOperator(lexemeTable.get(i).getLexeme());
                    }
                }
            }
        }
        System.out.println("********* OPERATOR *********");
        for (MyPair<String, Integer> aPair : theOperatorList) {
            System.out.println(aPair.getL() + " : " + aPair.getR());
        }
        System.out.println("********* OPERAND *********");
        for (MyPair<String, Integer> aPair : theOperandList) {
            System.out.println(aPair.getL() + " : " + aPair.getR());
        }


    }

    private void doAnalyzeMethodHeader(MyMethod aMethod) {
        //This method is for finding Halstead's complexity.
        //Analyze method header.
        if (!aMethod.getAccessModifier().equalsIgnoreCase("(default)")) {
            doAddOperator(aMethod.getAccessModifier());
        }
        if (!aMethod.getStaticMethod().equalsIgnoreCase("(non-static)")) {
            doAddOperator(aMethod.getStaticMethod());
        }
        if (!aMethod.getReturnType().equalsIgnoreCase("")) {
            doAddOperator(aMethod.getReturnType());
        }
        if (!aMethod.getMethodName().equalsIgnoreCase("")) {
            doAddOperator(aMethod.getMethodName());
        }
        if (!aMethod.getParameterType().isEmpty()) {
            for (String aParamType : aMethod.getParameterType()) {
                doAddOperator(aParamType);
            }
        }
        if (!aMethod.getParameterName().isEmpty()) {
            for (String aParamType : aMethod.getParameterName()) {
                doAddOperand(aParamType);
            }
        }
        //Add paranthesis after method header (Source Code must be compiled without and errors).
        doAddOperator("()");
    }

    private void doAddOperator(String lexeme) {
        //This method is for finding Halstead's complexity.
        //Check if operator or operand does exist?
        for (MyPair<String, Integer> aPair : theOperatorList) {
            if (aPair.getL().equals(lexeme)) {
                System.out.println("Duplicate Operator at " + lexeme);
                aPair.setR(aPair.getR() + 1);
                return;
            }
        }
        System.out.println("OPRATOR ADDED OK " + lexeme);
        theOperatorList.add(new MyPair<>(lexeme, 1));
    }

    private void doAddOperand(String lexeme) {
        //This method is for finding Halstead's complexity.
        //Check if operator or operand does exist?
        for (MyPair<String, Integer> aPair : theOperandList) {
            if (aPair.getL().equals(lexeme)) {
                System.out.println("Duplicate Operand at " + lexeme);
                aPair.setR(aPair.getR() + 1);
                return;
            }
        }
        System.out.println("..OPERAND.. ADDED OK " + lexeme);
        theOperandList.add(new MyPair<>(lexeme, 1));
    }

    //****************************************************************************************************
    //****************************************************************************************************
    private void getStatement() {
        int currentState = 0;
        String statementSymbol;
        String statementName;
        for (Token aLexemeTable : lexemeTable) {
            switch (currentState) {
                case 0:
                    statementName = "";
                    if (aLexemeTable.getType().contains("STATEMENT_")) {
                        try {
                            String[] parts = aLexemeTable.getType().split("_");
                            statementSymbol = aLexemeTable.getLexeme();
                            for (int i = 1; i < parts.length; i++) {
                                if (i == 1)
                                    statementName = statementName + parts[i];
                                else
                                    statementName = statementName + "_" + parts[i];
                            }
                            if (aLexemeTable.getType().equalsIgnoreCase("STATEMENT_ELSE_STATEMENT")) {
                                allStatement.add(new MyStatement("else", "ELSE_STATEMENT"));
                                currentState = 0;
                            } else if (aLexemeTable.getType().equalsIgnoreCase("STATEMENT_ELSE_IF_STATEMENT")) {
                                allStatement.add(new MyStatement("else if", "ELSE_IF_STATEMENT"));
                                currentState = 0;
                            } else if (aLexemeTable.getType().equalsIgnoreCase("STATEMENT_WHILE_STATEMENT")) {
                                if (!doWhileFlag) {
                                    allStatement.add(new MyStatement("while", "WHILE_STATEMENT"));
                                    currentState = 0;
                                } else {
                                    doWhileFlag = false;
                                    currentState = 0;
                                }
                            } else if (aLexemeTable.getType().equalsIgnoreCase("STATEMENT_DO_STATEMENT")) {
                                doWhileFlag = true;
                                allStatement.add(new MyStatement("do...while", "DO_WHILE_STATEMENT"));
                                currentState = 0;
                            } else {
                                allStatement.add(new MyStatement(statementSymbol, statementName));
                                currentState = 0;
                            }
                        } catch (ArrayIndexOutOfBoundsException c) {
                            System.err.println("Somethings gone wrong.");
                        }
                    } else {
                        currentState = 0;
                    }
                    break;
                default:
                    break;
            }
        }
        doWhileFlag = false; //In order to reuse again.
    }

    private void constructBlock(int startIndex, int endIndex) {
        //Get rid of old block.
        if (statementList.size() != 0)
            addNode();
        for (int i = startIndex; i <= endIndex; i++) {
            if (lexemeTable.get(i).getType().equalsIgnoreCase("SEMICOLON")) {
                if (!ignoreBracket)
                    ignoreBracket = true; //Reset value
                aStatement.append(lexemeTable.get(i).getLexeme());
                statementList.add(aStatement.toString());
                aStatement.setLength(0);
            } else if (lexemeTable.get(i).getType().equalsIgnoreCase("STATEMENT_IF_STATEMENT")) {
                i = doIf(i, "if");
            } else if (lexemeTable.get(i).getType().equalsIgnoreCase("STATEMENT_ELSE_STATEMENT")) {
                i = doElse(i);
            } else if (lexemeTable.get(i).getType().equalsIgnoreCase("STATEMENT_FOR_STATEMENT")) {
                i = doIf(i, "for");
            } else if (lexemeTable.get(i).getType().equalsIgnoreCase("STATEMENT_FOREACH_STATEMENT")) {
                i = doIf(i, "foreach");
            } else if (lexemeTable.get(i).getType().equalsIgnoreCase("STATEMENT_ELSE_IF_STATEMENT")) {
                i = doIf(i, "else if");
            } else if (lexemeTable.get(i).getType().equalsIgnoreCase("STATEMENT_WHILE_STATEMENT")) {
                if (!doWhileFlag) {
                    i = doIf(i, "while");
                } else {
                    i = handleDoWhile(i);
                    doWhileFlag = false; //In order to reuse again.
                }
            } else if (lexemeTable.get(i).getType().equalsIgnoreCase("STATEMENT_DO_STATEMENT")) {
                doWhileFlag = true;
                i = doElse(i);
            } else if (lexemeTable.get(i).getType().equalsIgnoreCase("STATEMENT_SWITCH_STATEMENT")) {
                i = doIf(i, "switch");
            } else if (lexemeTable.get(i).getType().equalsIgnoreCase("STATEMENT_CASE_STATEMENT") ||
                    lexemeTable.get(i).getType().equalsIgnoreCase("STATEMENT_DEFAULT_STATEMENT")) {
                i = handleSwitchCaseBlock(i, endIndex) - 1;
                //without -1 cannot recognize if statement after case.
                //continue to step over : lexeme.
            } else {
                /*if (lexemeTable.get(i).getType().equalsIgnoreCase("LB") ||
                        lexemeTable.get(i).getType().equalsIgnoreCase("RB"))
                    continue;*/
                //System.out.println(lexemeTable.get(i).getLexeme());
                if (lexemeTable.get(i).getType().equalsIgnoreCase("VARIABLE"))
                    ignoreBracket = false;
                if ((lexemeTable.get(i).getType().equalsIgnoreCase("LB") ||
                        lexemeTable.get(i).getType().equalsIgnoreCase("RB")) && !ignoreBracket) {
                    //Append LB and RB in array instantiation.
                    aStatement.append(lexemeTable.get(i).getLexeme()).append(" ");
                }

                if (!lexemeTable.get(i).getType().equalsIgnoreCase("LB") &&
                        !lexemeTable.get(i).getType().equalsIgnoreCase("RB") &&
                        !lexemeTable.get(i).getType().equalsIgnoreCase("MULTI_COMMENT") &&
                        !lexemeTable.get(i).getType().equalsIgnoreCase("COMMENT"))
                    aStatement.append(lexemeTable.get(i).getLexeme()).append(" ");
            }
        }
        addNode(); //ADD NEW NODE********

        /*System.out.println("STATEMENT");
        for (Node aNode : statementNodeStack) {
            System.out.println(aNode.getData());
        }
        System.out.println("CONDITION");
        for (Node aNode : conditionNodeStack) {
            System.out.println(aNode.getData());
        }*/
    }

    private int handleSwitchCaseBlock(int startIndex, int endIndex) {
        boolean isFoundCase = false;
        int tempTokenIndex = startIndex;
        int currentState = 0;
        int startBodyIndex = 0, endBodyIndex = 0;
        do {
            switch (currentState) {
                case 0:
                    if (lexemeTable.get(tempTokenIndex).getType().equalsIgnoreCase("STATEMENT_CASE_STATEMENT") ||
                            lexemeTable.get(tempTokenIndex).getType().equalsIgnoreCase("STATEMENT_DEFAULT_STATEMENT")) {
                        currentState = 1;
                    } else {
                        currentState = 0;
                    }
                    aStatement.append(lexemeTable.get(tempTokenIndex).getLexeme()).append(" ");
                    ++tempTokenIndex;
                    break;
                case 1:
                    if (lexemeTable.get(tempTokenIndex).getType().equalsIgnoreCase("COLON")) {
                        aStatement.append(lexemeTable.get(tempTokenIndex).getLexeme()).append(" ");
                        currentState = 2;
                        ++tempTokenIndex;
                        startBodyIndex = tempTokenIndex;
                    } else {
                        currentState = 1;
                        aStatement.append(lexemeTable.get(tempTokenIndex).getLexeme()).append(" ");
                        ++tempTokenIndex;
                    }
                    break;
                case 2:
                    if (lexemeTable.get(tempTokenIndex).getType().equalsIgnoreCase("STATEMENT_CASE_STATEMENT") ||
                            lexemeTable.get(tempTokenIndex).getType().equalsIgnoreCase("STATEMENT_DEFAULT_STATEMENT") ||
                            tempTokenIndex == endIndex) {
                        isFoundCase = true;
                        endBodyIndex = tempTokenIndex - 1;
                    } else {
                        currentState = 2;
                        ++tempTokenIndex;
                    }
                    break;
                default:
                    break;
            }
        } while (!isFoundCase);
        constructBlock(startBodyIndex, endBodyIndex);
        return tempTokenIndex;
    }


    private int getCondition(int startIndex, String statementType) {
        int tempTokenIndex = startIndex + 1;
        int openParen = 0, closeParen = 0;
        boolean isBalanceParen;
        aStatement.append(statementType.concat(" "));
        do {
            if (lexemeTable.get(tempTokenIndex).getType().equalsIgnoreCase("LP"))
                ++openParen;
            else if (lexemeTable.get(tempTokenIndex).getType().equalsIgnoreCase("RP"))
                ++closeParen;
            isBalanceParen = openParen == closeParen;

            aStatement.append(lexemeTable.get(tempTokenIndex).getLexeme()).append(" ");
            ++tempTokenIndex;
        } while (!isBalanceParen);
        statementList.add(aStatement.toString());
        addNode(); //ADD NEW NODE********
        return tempTokenIndex;
    }

    private int getForCondition(int startIndex) {
        String statementType = "if";
        int currentState = 0;
        int tempTokenIndex = startIndex + 1;
        int openParen = 0, closeParen = 0;
        boolean isBalanceParen;
        aStatement.append(statementType.concat(" ("));
        do {
            if (lexemeTable.get(tempTokenIndex).getType().equalsIgnoreCase("LP"))
                ++openParen;
            else if (lexemeTable.get(tempTokenIndex).getType().equalsIgnoreCase("RP"))
                ++closeParen;
            isBalanceParen = openParen == closeParen;

            switch (currentState) {
                case 0:
                    if (lexemeTable.get(tempTokenIndex).getType().equalsIgnoreCase("SEMICOLON")) {
                        currentState = 1;
                    } else {
                        currentState = 0;
                    }
                    ++tempTokenIndex;
                    break;
                case 1:
                    if (lexemeTable.get(tempTokenIndex).getType().equalsIgnoreCase("SEMICOLON")) {
                        currentState = 2; //Jump to default = already got a condition statement.
                    } else {
                        aStatement.append(lexemeTable.get(tempTokenIndex).getLexeme()).append(" ");
                        currentState = 1;
                    }
                    ++tempTokenIndex;
                    break;
                default:
                    ++tempTokenIndex;
                    break;
            }
        } while (!isBalanceParen);
        statementList.add(aStatement.append(")").toString());
        addNode(); //ADD NEW NODE********
        return tempTokenIndex;
    }

    private int getForEachCondition(int startIndex) {
        String statementType = "foreach in ( ";
        int currentState = 0;
        int tempTokenIndex = startIndex + 1; //+1 to skip current lexeme.
        int openParen = 0, closeParen = 0;
        boolean isBalanceParen;
        aStatement.append(statementType);
        do {
            if (lexemeTable.get(tempTokenIndex).getType().equalsIgnoreCase("LP"))
                ++openParen;
            else if (lexemeTable.get(tempTokenIndex).getType().equalsIgnoreCase("RP"))
                ++closeParen;
            isBalanceParen = openParen == closeParen;

            switch (currentState) {
                case 0:
                    if (lexemeTable.get(tempTokenIndex).getType().equalsIgnoreCase("STATEMENT_IN_STATEMENT")) {
                        currentState = 1;
                    } else {
                        currentState = 0;
                    }
                    ++tempTokenIndex;
                    break;
                case 1:
                    aStatement.append(lexemeTable.get(tempTokenIndex).getLexeme()).append(" ");
                    currentState = 1;
                    ++tempTokenIndex;
                    break;
                default:
                    ++tempTokenIndex;
                    break;
            }
        } while (!isBalanceParen);
        statementList.add(aStatement.toString());
        addNode(); //ADD NEW NODE********
        return tempTokenIndex;
    }

    private int determineBodyScope(int startIndex) {
        //Check whether it's conditional statement with or without bracket.
        if (lexemeTable.get(startIndex).getType().equalsIgnoreCase("LB")) {
            System.out.println("determineBodyScope MULTI");
            return getMultipleBodyScope(startIndex);
        } else {
            System.out.println("determineBodyScope SINGLE");
            return getSingleBodyScope(startIndex);
        }
    }

    private int getMultipleBodyScope(int startIndex) {
        int tempStartIndex = startIndex;
        int opeBracket = 0, closeBracket = 0;
        boolean isBalanceBracket;
        do {
            if (lexemeTable.get(tempStartIndex).getType().equalsIgnoreCase("LB")) {
                ++opeBracket;
            } else if (lexemeTable.get(tempStartIndex).getType().equalsIgnoreCase("RB")) {
                ++closeBracket;
            }
            isBalanceBracket = !(opeBracket == 0 && closeBracket == 0) && opeBracket == closeBracket;
            ++tempStartIndex;
        } while (!isBalanceBracket);
        return tempStartIndex - 1;
    }

    private int getSingleBodyScope(int startIndex) {
        int tempStartIndex = startIndex;
        do {
            ++tempStartIndex;
        } while (!lexemeTable.get(tempStartIndex).getType().equalsIgnoreCase("SEMICOLON"));
        return tempStartIndex;
    }

    private int doIf(int startIndex, String statementType) {
        if (statementType.equalsIgnoreCase("for")) {
            startIndex = getForCondition(startIndex);
        } else if (statementType.equalsIgnoreCase("foreach")) {
            startIndex = getForEachCondition(startIndex);
        } else {
            startIndex = getCondition(startIndex, statementType);
        }
        int startBodyIndex = startIndex;
        int endBodyIndex = determineBodyScope(startBodyIndex);
        constructBlock(startBodyIndex, endBodyIndex);
        return endBodyIndex;
        //**************************************************************************************
        //tempStartIndex = getBody(tempStartIndex);
        //Pair<List<String>, Integer> tempPairBody = getBody(tempStartIndex);
        //allBlock.add(new Node(tempPairBody.getKey()));
    }

    private int doElse(int startIndex) {
        int endBodyIndex = determineBodyScope(startIndex + 1);
        //System.out.println(startBodyIndex + " " + endBodyIndex);
        //System.out.println(startIndex + 1 + " " + endBodyIndex);
        constructBlock(startIndex + 1, endBodyIndex); // +1 to skip else token
        return endBodyIndex;
    }

    private int handleDoWhile(int startIndex) {
        int tempTokenIndex = startIndex;
        int openParen = 0, closeParen = 0;
        boolean isBalanceParen;
        int currentState = 0;
        aStatement.append("if ");
        do {
            switch (currentState) {
                case 0:
                    if (lexemeTable.get(tempTokenIndex).getType().equalsIgnoreCase("STATEMENT_WHILE_STATEMENT")) {
                        currentState = 1;
                    } else {
                        currentState = 0;
                    }
                    ++tempTokenIndex;
                    break;
                case 1:
                    if (lexemeTable.get(tempTokenIndex).getType().equalsIgnoreCase("LP")) {
                        aStatement.append(lexemeTable.get(tempTokenIndex).getLexeme()).append(" ");
                        currentState = 2;
                    } else {
                        currentState = 0;
                    }
                    ++tempTokenIndex;
                    break;
                case 2:
                    aStatement.append(lexemeTable.get(tempTokenIndex).getLexeme()).append(" ");
                    ++tempTokenIndex;
                    break;
                default:
                    ++tempTokenIndex;
                    break;
            }

            if (lexemeTable.get(tempTokenIndex).getType().equalsIgnoreCase("LP"))
                ++openParen;
            else if (lexemeTable.get(tempTokenIndex).getType().equalsIgnoreCase("RP"))
                ++closeParen;
            isBalanceParen = openParen == closeParen;

        } while (!isBalanceParen);
        statementList.add(aStatement.append(") ").toString());
        addNode(); //ADD NEW NODE********
        return tempTokenIndex + 1; //+1 to skip ; after do...while statement.
    }

    private void prepareNode() {
        /*for (MyMethod aMethod : allMethod) { // iterate each method
            constructBlock(aMethod.getStartIndex(), aMethod.getEndIndex(), null);
            System.out.println("=============================");
        }*/
        for (MyMethod aMethod : allMethod) {
            methodScope = aMethod.getMethodName();
            constructBlock(aMethod.getStartIndex(), aMethod.getEndIndex());
        }
        methodScope = "NULL"; //Reset value.
    }

    private void addNode() {
        //Skip empty statement.
        if (statementList.size() == 0)
            return;
        allBlock.add(new Node(statementList, methodScope)); // construct a root (no parent)
        aStatement.setLength(0);
        statementList.clear();
    }

    //*********************************************************************************************************
    //*************************************** BELOW IS FOR EXCEL REPORT ***************************************
    //*********************************************************************************************************

    public String getFileName() {
        return new File(filePath).getName();
    }

    public List<MyMethod> getAllMethod() {
        return allMethod;
    }

    public int getnumberOfParameter() {
        int numberOfParameter = 0;
        for (MyMethod aMethod : allMethod) {
            numberOfParameter = numberOfParameter + aMethod.getNumberOfParameter();
        }
        return numberOfParameter;
    }

    public List<MyVariable> getAllGlobalVariable() {
        return allGlobalVariable;
    }

    public List<MyVariable> getAllLocalVariable() {
        return allLocalVariable;
    }

    public MyHalstead getHalstead() {
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setMaximumFractionDigits(3);

        MyHalstead myHalstead = new MyHalstead();
        double n1, n2, N1 = 0.0, N2 = 0.0, pVocab, pLength, pVolume, pDifficulty,
                pEffort, timeReq, delivBugs;

        n1 = theOperatorList.size();
        n2 = theOperandList.size();
        myHalstead.setDistinctOpt(n1); //n1
        myHalstead.setDistinctOpr(n2); //n2

        for (MyPair<String, Integer> aPair : theOperatorList)
            N1 = N1 + aPair.getR();
        for (MyPair<String, Integer> aPair : theOperandList)
            N2 = N2 + aPair.getR();

        myHalstead.setNumberOfOpt(N1);
        myHalstead.setNumberOfOpr(N2);

        pVocab = n1 + n2;
        pLength = N1 + N2;
        myHalstead.setProgramVocab(pVocab);
        myHalstead.setProgramLength(pLength);

        pVolume = pLength * (Math.log(pVocab) / Math.log(2));
        myHalstead.setVolume(pVolume);

        pDifficulty = (n1 / 2.0) * (N2 / n2);
        myHalstead.setDifficulty(pDifficulty);

        pEffort = pDifficulty * pVolume;
        myHalstead.setEffort(pEffort);

        timeReq = pEffort / 18.0;
        myHalstead.setTimeRequired(timeReq);

        delivBugs = ((Math.pow(pEffort, 0.667)) / 3000.0);
        myHalstead.setDeliveredBugs(delivBugs);

        return myHalstead;
    }

    public int getAssignmentStmt() {
        int numberOfAssignmentStmt = 0;

        for (MyOperator anOperator : allOperator) {
            if (anOperator.getOperatorName().equalsIgnoreCase("ASSIGNMENT"))
                ++numberOfAssignmentStmt;
        }

        return numberOfAssignmentStmt;
    }

    public List<MyStatement> getAllStatement() {
        return allStatement;
    }

    public List<Node> getallBlock() {
        return allBlock;
    }

    public int getLoop() {
        int numberOfLoop = 0;
        for (MyStatement aStatement : allStatement) {
            if (aStatement.getStatementName().equalsIgnoreCase("FOR_STATEMENT") ||
                    aStatement.getStatementName().equalsIgnoreCase("FOREACH_STATEMENT") ||
                    aStatement.getStatementName().equalsIgnoreCase("WHILE_STATEMENT") ||
                    aStatement.getStatementName().equalsIgnoreCase("DO_WHILE_STATEMENT"))
                ++numberOfLoop;
        }
        return numberOfLoop;
    }

    public int getLOC() {
        int totalLines = -1;

        try (FileReader fileReader = new FileReader(filePath);
             LineNumberReader lineReader = new LineNumberReader(fileReader)) {
            while ((lineReader.readLine()) != null)
                totalLines = lineReader.getLineNumber();
        } catch (Exception e) {
            System.err.println("Error during getLOC.");
            e.printStackTrace();
        }
        return totalLines;
    }
}
