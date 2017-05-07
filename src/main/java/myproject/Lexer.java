package myproject;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Hashtable;

class Lexer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Lexer.class);
    private BufferedReader myBuffer;
    //private static int startState = 0;
    private char peek = ' ';
    private String currentLine = "";
    private StringBuilder tokenString = new StringBuilder();
    private String tempToken = "";

    //Shared variables
    private static Hashtable<String, String> reservedWords = new Hashtable<>();
    private static Hashtable<String, String> symbols = new Hashtable<>();

    //private static boolean endOfLine = false;
    private int isForeverLoop = 0;

    static {
        LOGGER.info("Initiating reserved words");
        //################### VARIABLES ###################
        for (Variable aVariable : Variable.values())
            reservedWords.put(aVariable.getVariableName(), "Variable");
        //################### CLASS ###################
        reservedWords.put("class", "CLASS");
        reservedWords.put("public", "MODIFIER");
        reservedWords.put("private", "MODIFIER");
        reservedWords.put("internal", "MODIFIER");
        reservedWords.put("protected", "MODIFIER");

        reservedWords.put("static", "STATIC");
        reservedWords.put("void", "VOID");

        reservedWords.put("return", "RETURN");
        reservedWords.put("namespace", "NAMESPACE");

        //reservedWords.put("protected internal", "MODIFIER");
        //################### Statement ###################
        for (Statement aStatement : Statement.values())
            reservedWords.put(aStatement.getStatementLexeme(), aStatement.getStatementName());
        //#################### SYMBOLS ####################
        for (Symbol aSymbol : Symbol.values())
            symbols.put(aSymbol.getSymbolCharacter(), aSymbol.name());
    }

    Lexer(String filePath) throws FileNotFoundException {
        try {
            myBuffer = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
        } catch (FileNotFoundException ex) {
            LOGGER.error("[Lexer.java : 125] File not found.");
            throw ex;
        }
    }
    /*private void doCountLine() throws IOException {
        try (BufferedReader readLineBuffer = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)))) {
            while (readLineBuffer.readLine() != null)
                ++numOfLine;
        }
        LOGGER.info("NUMBER OF LINE : " + numOfLine);-
    }*/

    //####################################################################################
    private void readNextLine() throws IOException {
        try {
            currentLine = myBuffer.readLine();
            if (currentLine.length() <= 0) {
                do {
                    currentLine = myBuffer.readLine();
                } while (currentLine.length() <= 0);
            }
        } catch (NullPointerException ex) {
            //   LOGGER.error("Test Flow method : readNextLine()");
            if (isForeverLoop > 3) {
                throw new NullPointerException();
            }
            isForeverLoop++;
            currentLine = " "; // last char handle
        }
       /*if (true) {
            currentLine = myBuffer.readLine();
            ++numOfLine;
        } else {
            if ((currentLine = myBuffer.readLine()) == null) {
                LOGGER.info("NUMBER OF LINE : " + numOfLine);
                System.exit(0);
            } else {
                ++numOfLine;
                while (currentLine.length() <= 0) {
                    currentLine = myBuffer.readLine();
                    ++numOfLine;
                }
            }
        }*/
    }

    private void clearTokenString() {
        tokenString.setLength(0);
    }

    private void nextChar() throws IOException {
        if (currentLine.length() > 0) {
            peek = currentLine.charAt(0);
            tempToken = currentLine;
            currentLine = currentLine.substring(1);
        } else {
            readNextLine();
            peek = currentLine.charAt(0);
            tempToken = currentLine;
            currentLine = currentLine.substring(1);
        }
    }

    private boolean nextChar(char c) throws IOException {
        nextChar();
        if (peek != c) {
            currentLine = tempToken;
            return false;
        } else {
            return true;
        }
    }

    //####################################################################################
    private Token doCheckSym(String c) throws IOException {
        switch (c) {
            case "&":
                if (nextChar('&'))
                    return new Word(Symbol.OPERATOR_LOGICAL_AND.name(), Symbol.OPERATOR_LOGICAL_AND.getSymbolCharacter());
                else return new Word(symbols.get(c), c);
            case "|":
                if (nextChar('|'))
                    return new Word(Symbol.OPERATOR_LOGICAL_OR.name(), Symbol.OPERATOR_LOGICAL_OR.getSymbolCharacter());
                else return new Word(symbols.get(c), c);
            case "^":
                return new Word(symbols.get(c), c);
            case "=":
                if (nextChar('='))
                    return new Word(Symbol.OPERATOR_EQUAL.name(), Symbol.OPERATOR_EQUAL.getSymbolCharacter());
                else return new Word(symbols.get(c), c);
            case "!":
                if (nextChar('='))
                    return new Word(Symbol.OPERATOR_NOT_EQUAL.name(), Symbol.OPERATOR_NOT_EQUAL.getSymbolCharacter());
                else return new Word(symbols.get(c), c);
            case "<":
                if (nextChar('='))
                    return new Word(Symbol.OPERATOR_LT_EQUAL.name(), Symbol.OPERATOR_LT_EQUAL.getSymbolCharacter());
                else if (nextChar('<'))
                    return new Word(Symbol.OPERATOR_SHIFT_LEFT.name(), Symbol.OPERATOR_SHIFT_LEFT.getSymbolCharacter());
                else return new Word(symbols.get(c), c);
            case ">":
                if (nextChar('='))
                    return new Word(Symbol.OPERATOR_GT_EQUAL.name(), Symbol.OPERATOR_LT_EQUAL.getSymbolCharacter());
                else if (nextChar('>'))
                    return new Word(Symbol.OPERATOR_SHIFT_RIGHT.name(), Symbol.OPERATOR_SHIFT_RIGHT.getSymbolCharacter());
                else return new Word(symbols.get(c), c);
            case "~":
                return new Word(symbols.get(c), c);
            case "{":
                return new Word(symbols.get(c), c);
            case "}":
                return new Word(symbols.get(c), c);
            case "(":
                return new Word(symbols.get(c), c);
            case ")":
                return new Word(symbols.get(c), c);
            case "[":
                return new Word(symbols.get(c), c);
            case "]":
                return new Word(symbols.get(c), c);
            case ";":
                return new Word(symbols.get(c), c);
            case ":":
                return new Word(symbols.get(c), c);
            case "+":
                if (nextChar('=')) {
                    return new Word(Symbol.OPERATOR_ADDITION_EQUAL.name(), Symbol.OPERATOR_ADDITION_EQUAL.getSymbolCharacter());
                } else if (nextChar('+')) {
                    return new Word(Symbol.OPERATOR_INCREMENT.name(), Symbol.OPERATOR_INCREMENT.getSymbolCharacter());
                } else {
                    return new Word(symbols.get(c), c);
                }
            case "-":
                if (nextChar('=')) {
                    return new Word(Symbol.OPERATOR_SUBTRACTION_EQUAL.name(), Symbol.OPERATOR_SUBTRACTION_EQUAL.getSymbolCharacter());
                } else if (nextChar('-')) {
                    return new Word(Symbol.OPERATOR_DECREMENT.name(), Symbol.OPERATOR_DECREMENT.getSymbolCharacter());
                } else {
                    return new Word(symbols.get(c), c);
                }
            case "*":
                if (nextChar('='))
                    return new Word(Symbol.OPERATOR_MULTIPLICATION_EQUAL.name(), Symbol.OPERATOR_MULTIPLICATION_EQUAL.getSymbolCharacter());
                else
                    return new Word(symbols.get(c), c);

            case "/":
                if (nextChar('/')) {
                    readNextLine();
                    return new Word(Symbol.COMMENT.name(), Symbol.COMMENT.getSymbolCharacter());
                } else if (nextChar('*')) {
                    do {
                        try {
                            nextChar();
                            if (peek == '*') {
                                if (nextChar('/')) {
                                    return new Word(Symbol.MULTI_COMMENT.name(), Symbol.MULTI_COMMENT.getSymbolCharacter());
                                } // else continues
                            }
                        } catch (NullPointerException ex) {
                            return new Word(Symbol.MULTI_COMMENT.name(), Symbol.MULTI_COMMENT.getSymbolCharacter());
                            //return new Word("MULTI_COMMENT", "/*");
                        }
                    } while (true);
                } else if (nextChar('=')) {
                    return new Word(Symbol.OPERATOR_DIVISION_EQUAL.name(), Symbol.OPERATOR_DIVISION_EQUAL.getSymbolCharacter());
                } else {
                    return new Word(symbols.get(c), c);
                }

            case "%":
                if (nextChar('='))
                    return new Word(Symbol.OPERATOR_MODULUS_EQUAL.name(), Symbol.OPERATOR_MODULUS_EQUAL.getSymbolCharacter());
                else
                    return new Word(symbols.get(c), c);
            case ".":
                return new Word(symbols.get(c), c);
            case ",":
                return new Word(symbols.get(c), c);
            case "\"":
                StringBuilder myString = new StringBuilder();
                do {
                    nextChar();
                    if (peek == '\\') {
                        nextChar();
                        if (peek == '\"' || peek == '\'')
                            myString.append(peek);
                        else
                            myString.append("\\").append(peek); // + peek or currentLine = tempToken
                    } else if (peek == '"') {
                        return new Word(symbols.get(c), "\"" + myString.toString() + "\"");
                    } else {
                        myString.append(peek);
                    }
                } while (true);
            case "'":
                StringBuilder myChar = new StringBuilder();
                nextChar();
                if (peek == '\\') { // for character literal, hex, unicode ex.'\x0058', '\u0058'
                    do {
                        myChar.append(peek);
                        nextChar();
                    } while (peek != '\'');
                    return new Word(symbols.get(c), "\'" + myChar.toString() + "\'");
                } else if (Character.isLetterOrDigit(peek) || Character.isSpaceChar(peek)) {
                    String tempPeek = String.valueOf(peek);
                    if (nextChar('\''))
                        return new Word("CHAR", "\'" + String.valueOf(tempPeek) + "\'");
                    else
                        return new Word("UNKNOWN(SYNTAX ERROR?)", String.valueOf(peek));
                } else {
                    return new Word("UNKNOWN", String.valueOf(peek));
                }
        }
        return new Word("UNKNOWN", String.valueOf(peek)); // None is matched.
    }

    Token getNextToken() throws IOException {
        try {
            nextChar();
            while (Character.isSpaceChar(peek) || peek == '\t') {
                nextChar();
            }
        } catch (NullPointerException ex) {
            return null;
        }
        if (symbols.containsKey(String.valueOf(peek))) {
            return doCheckSym(String.valueOf(peek));
        } else if (Character.isLetter(peek) || peek == '_') {
            do {
                tokenString.append(peek);
                /*if (currentLine.isEmpty()) {
                    endOfLine = true;
                    break;
                }*/
                nextChar();
            } while (Character.isLetterOrDigit(peek) || peek == '_');
            String myLexeme = tokenString.toString();
            clearTokenString();
            if (reservedWords.containsKey(myLexeme)) {
                //if (!endOfLine)
                /*if (myLexeme.equalsIgnoreCase("if") && Parser.lexemeTable.get
                        (Parser.lexemeTable.size()-1).getType().equalsIgnoreCase(Statement.ELSE.getStatementName())) {
                    //In case of "else if" statement.

                    currentLine = tempToken;
                    Parser.lexemeTable.remove(Parser.lexemeTable.get(Parser.lexemeTable.size()-1));
                    return new Word("STATEMENT_ELSE_IF_STATEMENT", "else if");
                }*/
                currentLine = tempToken;
                return new Word(reservedWords.get(myLexeme), myLexeme);
            } else {
                //if (!endOfLine)
                currentLine = tempToken;
                return new Word("IDENTIFIER/UNDEFINED", myLexeme);
            }
        } else if (Character.isDigit(peek)) {
            int valI = 0;
            do {
                valI = 10 * valI + Character.digit(peek, 10);
                /*if (currentLine.isEmpty()) {
                    endOfLine = true;
                    break;
                }*/
                nextChar();
            } while (Character.isDigit(peek));
            if (peek == '.') {
                double valD = valI;
                double division = 10;
                while (true) {
                    /*if (currentLine.isEmpty()) {
                        endOfLine = true;
                        break;
                    }*/
                    nextChar();
                    if (!Character.isDigit(peek)) {
                        currentLine = tempToken;
                        break;
                    }
                    valD = valD + (Character.digit(peek, 10) / division);
                    division = division * 10;
                }
                return new Real("REAL", valD);
            } else {
                //if (!endOfLine && Character.isLetter(peek))
                currentLine = tempToken;
                return new Num("NUM", valI);

            }
        }
        return new Word("UNKNOWN", peek + ""); //None is matched.
    }
}
