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
    private static Hashtable<Character, String> symbols = new Hashtable<>();

    //private static boolean endOfLine = false;
    private int isForeverLoop = 0;

    static {
        LOGGER.info("Initiating reserved words");
        //################### VARIABLES ###################
        reservedWords.put("bool", "VARIABLE");
        reservedWords.put("byte", "VARIABLE");
        reservedWords.put("sbyte", "VARIABLE");
        reservedWords.put("char", "VARIABLE");
        reservedWords.put("decimal", "VARIABLE");
        reservedWords.put("double", "VARIABLE");
        reservedWords.put("float", "VARIABLE");
        reservedWords.put("int", "VARIABLE");//int32
        reservedWords.put("uint", "VARIABLE");//unsigned int32
        reservedWords.put("long", "VARIABLE");//int64
        reservedWords.put("ulong", "VARIABLE");//uinsigned int64
        reservedWords.put("object", "VARIABLE");
        reservedWords.put("short", "VARIABLE");//int16
        reservedWords.put("ushort", "VARIABLE");//unsigned int16
        reservedWords.put("string", "VARIABLE");
        reservedWords.put("var", "VARIABLE");
        //---------------------
        reservedWords.put("Boolean", "VARIABLE");
        reservedWords.put("Byte", "VARIABLE");
        reservedWords.put("SByte", "VARIABLE");
        reservedWords.put("Char", "VARIABLE");
        reservedWords.put("Decimal", "VARIABLE");
        reservedWords.put("Double", "VARIABLE");
        reservedWords.put("Single", "VARIABLE");
        reservedWords.put("Int32", "VARIABLE");//int32
        reservedWords.put("UInt32", "VARIABLE");//unsigned int32
        reservedWords.put("Int64", "VARIABLE");//int64
        reservedWords.put("UInt64", "VARIABLE");//unsigned int64
        reservedWords.put("Object", "VARIABLE");
        reservedWords.put("Int16", "VARIABLE");//int16
        reservedWords.put("UInt16", "VARIABLE");//unsigned int16
        reservedWords.put("String", "VARIABLE");
        //#################################################

        //################### CLASS ###################
        reservedWords.put("class", "CLASS");
        reservedWords.put("public", "MODIFIER");
        reservedWords.put("private", "MODIFIER");
        reservedWords.put("internal", "MODIFIER");
        reservedWords.put("protected", "MODIFIER");
        //reservedWords.put("protected internal", "MODIFIER");

        reservedWords.put("static", "STATIC");
        reservedWords.put("void", "VOID");

        reservedWords.put("return", "RETURN");
        reservedWords.put("namespace", "NAMESPACE");
        //#################################################

        //################### STATEMENT ###################
        reservedWords.put("if", "STATEMENT_IF_STATEMENT");
        reservedWords.put("else", "STATEMENT_ELSE_STATEMENT");
        reservedWords.put("for", "STATEMENT_FOR_STATEMENT");
        reservedWords.put("foreach", "STATEMENT_FOREACH_STATEMENT");
        reservedWords.put("in", "STATEMENT_IN_STATEMENT");
        reservedWords.put("while", "STATEMENT_WHILE_STATEMENT");
        reservedWords.put("do", "STATEMENT_DO_  STATEMENT");
        reservedWords.put("switch", "STATEMENT_SWITCH_STATEMENT");
        reservedWords.put("case", "STATEMENT_CASE_STATEMENT");
        reservedWords.put("default", "STATEMENT_DEFAULT_STATEMENT");

        //#################################################

        //#################### SYMBOLS ####################
        symbols.put('&', "OPERATOR_AND(LOGICAL)");
        symbols.put('|', "OPERATOR_OR(LOGICAL)");
        symbols.put('^', "OPERATOR_XOR(LOGICAL)");
        symbols.put('=', "OPERATOR_ASSIGNMENT");
        symbols.put('!', "OPERATOR_NOT");
        symbols.put('<', "OPERATOR_LT");
        symbols.put('>', "OPERATOR_GT");
        symbols.put('~', "OPERATOR_NOT(BITWISE)");

        symbols.put('/', "SLASH");

        symbols.put('{', "LB");
        symbols.put('}', "RB");
        symbols.put('(', "LP");
        symbols.put(')', "RP");
        symbols.put('[', "LSB");
        symbols.put(']', "RSB");

        symbols.put(';', "SEMICOLON");
        symbols.put(':', "COLON");

        symbols.put('+', "OPERATOR_ADDITION");
        symbols.put('-', "OPERATOR_SUBTRACTION");
        symbols.put('*', "OPERATOR_MULTIPLICATION");
        symbols.put('/', "OPERATOR_DIVISION");
        symbols.put('%', "OPERATOR_MODULUS");

        symbols.put('.', "FUNC_CALL");
        symbols.put(',', "COMMA");
        symbols.put('"', "STRING");
        symbols.put('\'', "CHAR");
        //#################################################
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
            // LOGGER.error("Test Flow method : readNextLine()");
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
    private Token doCheckSym(char c) throws IOException {
        switch (c) {
            case '&':
                if (nextChar('&')) return new Word("OPERATOR_AND(CONDITIONAL)", "&&");
                else return new Word(symbols.get(c), "&");
            case '|':
                if (nextChar('|')) return new Word("OPERATOR_OR(CONDITIONAL)", "||");
                else return new Word(symbols.get(c), "|");
            case '^':
                return new Word(symbols.get(c), "^");
            case '=':
                if (nextChar('=')) return new Word("OPERATOR_EQ", "==");
                else return new Word(symbols.get(c), "=");
            case '!':
                if (nextChar('=')) return new Word("OPERATOR_NEQ", "!=");
                else return new Word(symbols.get(c), "!");
            case '<':
                if (nextChar('=')) return new Word("OPERATOR_LTE", "<=");
                else if (nextChar('<')) return new Word("OPERATOR_SHIFT_LEFT", "<<");
                else return new Word(symbols.get(c), "<");
            case '>':
                if (nextChar('=')) return new Word("OPERATOR_GTE", ">=");
                else if (nextChar('>')) return new Word("OPERATOR_SHIFT_RIGHT", ">>");
                else return new Word(symbols.get(c), ">");
            case '~':
                return new Word(symbols.get(c), "~");
            case '{':
                return new Word(symbols.get(c), "{");
            case '}':
                return new Word(symbols.get(c), "}");
            case '(':
                return new Word(symbols.get(c), "(");
            case ')':
                return new Word(symbols.get(c), ")");
            case '[':
                return new Word(symbols.get(c), "[");
            case ']':
                return new Word(symbols.get(c), "]");
            case ';':
                return new Word(symbols.get(c), ";");
            case ':':
                return new Word(symbols.get(c), ":");
            case '+':
                if (nextChar('=')) {

                    //currentLine = currentLine.substring(currentLine.length()); // read a new line to avoid Null..exception.
                    return new Word("OPERATOR_ADDITION_EQ", "+=");

                } else if (nextChar('+')) {
                    return new Word("OPERATOR_INCREMENT", "++");
                } else {
                    return new Word(symbols.get(c), "+");
                }
            case '-':
                if (nextChar('=')) {

                    //currentLine = currentLine.substring(currentLine.length()); // read a new line to avoid Null..exception.
                    return new Word("OPERATOR_SUBTRACTION_EQ", "-=");

                } else if (nextChar('-')) {
                    return new Word("OPERATOR_DECREMENT", "--");
                } else {
                    return new Word(symbols.get(c), "-");
                }
            case '*':
                if (nextChar('=')) {

                    //currentLine = currentLine.substring(currentLine.length()); // read a new line to avoid Null..exception.
                    return new Word("OPERATOR_MULTIPLICATION_EQ", "*=");

                } else {
                    return new Word(symbols.get(c), "*");
                }
            case '/':
                /*if (nextChar('/')) {

                    //readNextLine();
                    //currentLine = currentLine.substring(currentLine.length()); // read a new line to avoid Null..exception.
                    return new Word("COMMENT", "//");
                */
                if (nextChar('/')) {
                    readNextLine();
                    return new Word("COMMENT", "//");

                } else if (nextChar('*')) {
                    do {
                        try {
                            nextChar();
                            if (peek == '*') {
                                if (nextChar('/')) {
                                    return new Word("MULTI_COMMENT", "/*...*/");
                                } // else continues
                            }
                        } catch (NullPointerException ex) {
                            return new Word("MULTI_COMMENT", "/*");
                        }
                    } while (true);
                    // return new Word("START_MULTI_COMMENT", "/*");
                } else if (nextChar('=')) {

                    //currentLine = currentLine.substring(currentLine.length()); // read a new line to avoid Null..exception.
                    return new Word("OPERATOR_DIVISION_EQ", "/=");

                } else {
                    return new Word(symbols.get(c), "/");
                }

            case '%':
                if (nextChar('=')) {

                    //currentLine = currentLine.substring(currentLine.length()); // read a new line to avoid Null..exception.
                    return new Word("OPERATOR_MODULUS_EQ", "%=");

                } else {
                    return new Word(symbols.get(c), "%");
                }
            case '.':
                return new Word(symbols.get(c), ".");
            case ',':
                return new Word(symbols.get(c), ",");
            case '"':
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
            case '\'':
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
        if (symbols.containsKey(peek)) {
            return doCheckSym(peek);
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
                        (Parser.lexemeTable.size()-1).getType().equalsIgnoreCase("STATEMENT_ELSE_STATEMENT")) {
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
