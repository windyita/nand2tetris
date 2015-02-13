/**
 * Created by yangyan on 11/18/14.
 */
import java.io.*;
import javax.swing.*;

public class CompilationEngine {

    private JackTokenizer tokenizer;
    private	VMWriter vmWriter;
    private SymbolTable symbolTable;
    private ASCIITable asciiTable;
    private File vmFile;
    private String className = "";
    private int if_cnt = -1;
    private int while_cnt = -1;
    private Boolean look_ahead = false;

    private enum SubroutineType {
        CONSTRUCTOR,
        FUNCTION,
        METHOD};

    public CompilationEngine(JackTokenizer tokenizer, VMWriter vmWriter, File vmFile) {
        this.tokenizer = tokenizer;
        this.vmWriter = vmWriter;
        this.vmFile = vmFile;
        symbolTable = new SymbolTable();
        asciiTable = new ASCIITable();
        CompileClass();
    }

    private void CompileClass() {

        advance();
        className = tokenizer.identifier();
        advance();

        while (tokenizer.hasMoreTokens() ) {
            tokenizer.advance();
            if (isClassVariable()) {
                CompileClassVarDec();
                continue;
            }
            if (isSubroutine()) {
                CompileSubroutine();
                continue;
            }
        }
        vmWriter.close();
    }

    private void CompileClassVarDec() {
        Kind varKind;
        String varType;
        String varName;
        varKind = Kind.valueOf(tokenizer.keyWord().toString().toUpperCase());
        advance();

        if (isIdentifier() ) {
            varType = tokenizer.identifier();
        }
        else {
            varType = tokenizer.keyWord().toString();
        }
        advance();
        varName = tokenizer.identifier();
        symbolTable.Define(varName, varType, varKind);

        advance();
        while (!isSemiColon() ) {
            if (isIdentifier() ) {
                varName = tokenizer.identifier();
                symbolTable.Define(varName, varType, varKind);
            }
            advance();
        }
    }

    private void CompileSubroutine() {
        symbolTable.startSubroutine();
        SubroutineType subroutineType = SubroutineType.valueOf(tokenizer.keyWord().toString().toUpperCase() );
        if_cnt = -1;
        while_cnt = -1;

        switch (subroutineType) {
            case CONSTRUCTOR:
                compileConstructor();
                break;
            case METHOD:
                compileMethod();
                break;
            case FUNCTION:
                compileFunction();
                break;
            default:
                break;
        }
    }

    private void compileConstructor() {
        String constructorName;

        int local_cnt;
        advance();
        advance();
        constructorName = className + "." + tokenizer.identifier();

        advance();
        advance();
        compileParameterList();

        advance();
        advance();

        while (isSubroutineVar() ) {
            if (isSubroutineVar() ) {
                compileVarDec();
            }
            advance();
        }
        local_cnt = symbolTable.VarCount(Kind.VAR);

        vmWriter.writeFunction(constructorName, local_cnt);

        int nFields = symbolTable.VarCount(Kind.FIELD);
        vmWriter.writePush(Segment.CONST, nFields);
        vmWriter.writeCall("Memory.alloc", 1);
        vmWriter.writePop(Segment.POINTER, 0);

        compileStatements();

    }

    private void compileMethod() {
        String returnType;
        String subroutineName;
        int local_cnt = 0;

        advance();
        if (isKeyWord() ) {
            returnType = "void";
        }
        else {
            returnType = tokenizer.identifier();
        }
        advance();

        subroutineName = className + "." + tokenizer.identifier();

        advance();
        advance();
        symbolTable.Define("ObjectReference","null", Kind.ARG);
        compileParameterList();
        advance();
        advance();

        while (isSubroutineVar() ) {
            if (isSubroutineVar() ) {
                compileVarDec();
            }
            advance();
        }

        local_cnt = symbolTable.VarCount(Kind.VAR);

        vmWriter.writeFunction(subroutineName, local_cnt);
        vmWriter.writePush(Segment.ARGUMENT, 0);
        vmWriter.writePop(Segment.POINTER, 0);
        compileStatements();
    }

    private void compileFunction() {
        String returnType;
        String subroutineName;
        int local_cnt ;

        advance();
        if (isKeyWord() ) {
            returnType = "void";
        }
        else {
            returnType = tokenizer.identifier();
        }
        advance();

        subroutineName = className + "." + tokenizer.identifier();

        advance();
        advance();
        compileParameterList();

        advance();
        advance();

        while (isSubroutineVar() ) {
            if (isSubroutineVar() ) {
                compileVarDec();
            }
            advance();
        }

        local_cnt = symbolTable.VarCount(Kind.VAR);
        vmWriter.writeFunction(subroutineName, local_cnt);
        compileStatements();
    }

    private void compileParameterList() {
        String argType;
        String argName;
        Kind argKind = Kind.ARG;

        if (isCloseParenthesis() ) {
            return;
        }
        if (isKeyWord() ) {
            argType = tokenizer.keyWord().toString();
        }
        else {
            argType = tokenizer.identifier();
        }
        advance();
        argName = tokenizer.identifier();
        symbolTable.Define(argName, argType, argKind);
        advance();
        while (!isCloseParenthesis() ) {
            if (isComma() ) {
                advance();
            }
            if (isKeyWord() ) {
                argType = tokenizer.keyWord().toString();
                advance();
                argName = tokenizer.identifier();
                advance();
                symbolTable.Define(argName, argType, argKind);
            }
            else {
                argType = tokenizer.identifier();
                advance();
                argName = tokenizer.identifier();
                advance();
                symbolTable.Define(argName, argType, argKind);
            }
        }
    }

    private void compileVarDec() {
        Kind varKind = Kind.VAR;
        String varType;
        String varName;

        advance();
        if (isKeyWord() ) {
            varType = tokenizer.keyWord().toString();
        }
        else {
            varType = tokenizer.identifier();
        }
        advance();
        varName = tokenizer.identifier();
        symbolTable.Define(varName, varType, varKind);

        while (tokenizer.hasMoreTokens() ) {
            tokenizer.advance();
            if (isSemiColon() ) {
                break;
            }
            if (isIdentifier() ) {
                varName = tokenizer.identifier();
                symbolTable.Define(varName, varType, varKind);
            }
        }
    }

    private void compileStatements() {
        while (!isCloseCurlyBracket() ) {
            switch (tokenizer.keyWord()) {
                case LET:
                    compileLet();
                    break;
                case IF:
                    compileIf();
                    break;
                case WHILE:
                    compileWhile();
                    break;
                case DO:
                    compileDo();
                    break;
                case RETURN:
                    compileReturn();
                default: break;
            }
            if (!look_ahead) {
                advance();
            }
            else {
                look_ahead = false;
            }
        }
    }

    private void compileDo() {
        Boolean isExternalSubroutine = false;
        String subroutineCall="";
        advance();
        String subroutineOwner = tokenizer.identifier();
        advance();

        if (isPeriod() ) {
            advance();
            String subroutineName =  tokenizer.identifier();

            advance();
            if (subroutineOwner.equals(className)) {
                compileInternalFunctionCall(className, subroutineName);
            }
            else if (!symbolTable.TypeOf(subroutineOwner).isEmpty() ) {
                compileExternalMethodCall(subroutineOwner, subroutineName);
            }
            else if (isSystemSubroutine(subroutineOwner) ) {
                compileSystemCall(subroutineOwner, subroutineName);
            }
            else {
                compileExternalFunctionCall(subroutineOwner, subroutineName);
            }
        }
        else {
            compileInternalMethodCall(className, subroutineOwner);
        }
        vmWriter.writePop(Segment.TEMP, 0);
        advance();

    }

    private void compileLet() {
        advance();
        String varName = tokenizer.identifier();
        int nVarIndex = symbolTable.IndexOf(varName);
        Kind varKind = symbolTable.KindOf(varName);
        advance();

        if (isOpenBracket() ) {

            advance();
            CompileExpression();
            vmWriter.writePush(symbolTable.SegmentOf(varName),symbolTable.IndexOf(varName) );
            vmWriter.WriteArithmetic(Operator.ADD);

            advance();
            advance();
            CompileExpression();

            vmWriter.writePop(Segment.TEMP, 0);
            vmWriter.writePop(Segment.POINTER, 1);
            vmWriter.writePush(Segment.TEMP, 0);
            vmWriter.writePop(Segment.THAT, 0);
        }
        else {
            advance();
            CompileExpression();
            Segment varSegment = symbolTable.SegmentOf(varName);
            vmWriter.writePop(varSegment, nVarIndex);
        }

        if (!isSemiColon() ) {
            advance();
        }

    }

    private void compileInternalMethodCall(String owner, String subroutine) {
        vmWriter.writePush(Segment.POINTER, 0);
        advance();
        int nArgs = CompileExpressionList();
        nArgs++;
        vmWriter.writeCall(owner + "." + subroutine, nArgs);
    }

    private void compileInternalFunctionCall(String owner, String subroutine) {
        advance();
        int nArgs = CompileExpressionList();
        vmWriter.writeCall(owner + "." + subroutine, nArgs);
    }

    private void compileExternalMethodCall(String owner, String subroutine) {
        Segment segment = symbolTable.SegmentOf(owner);
        int index   = symbolTable.IndexOf(owner);
        vmWriter.writePush(segment, index);
        advance();
        int nArgs = CompileExpressionList();
        nArgs++;
        vmWriter.writeCall(symbolTable.TypeOf(owner) + "." + subroutine, nArgs);
    }

    private void compileExternalFunctionCall(String owner, String subroutine) {
        advance();
        int nArgs = CompileExpressionList();
        vmWriter.writeCall(owner + "." + subroutine, nArgs);
    }

    private void compileSystemCall(String owner, String subroutine) {
        advance();
        int nArgs = CompileExpressionList();
        vmWriter.writeCall(owner + "." + subroutine, nArgs);
    }

    private void compileWhile() {
        while_cnt++;
        int	whileDepth = while_cnt;
        vmWriter.WriteLabel("WHILE_EXP" + Integer.toString(whileDepth) );
        advance();
        advance();

        CompileExpression();

        vmWriter.WriteArithmetic(Operator.NOT);

        advance();
        advance();
        vmWriter.WriteIf("WHILE_END" + Integer.toString(whileDepth) );
        compileStatements();
        vmWriter.WriteGoto("WHILE_EXP" + Integer.toString(whileDepth) );
        vmWriter.WriteLabel("WHILE_END" + Integer.toString(whileDepth) );
    }

    private void compileReturn() {
        advance();
        if (!isSemiColon() ) {
            CompileExpression();
            vmWriter.writeReturn();
        }
        else {
            vmWriter.writePush(Segment.CONST, 0);
            vmWriter.writeReturn();

        }
    }

    private void compileIf() {
        if_cnt++;
        int ifDepth = if_cnt;
        advance();
        advance();
        CompileExpression();
        vmWriter.WriteIf("IF_TRUE" + Integer.toString(ifDepth) );
        vmWriter.WriteGoto("IF_FALSE" + Integer.toString(ifDepth) );
        advance();
        advance();
        vmWriter.WriteLabel("IF_TRUE" + Integer.toString(ifDepth) );
        compileStatements();

        advance();

        if (isElse() ) {
            vmWriter.WriteGoto("IF_END" + Integer.toString(ifDepth) );
            vmWriter.WriteLabel("IF_FALSE" + Integer.toString(ifDepth) );
            advance();
            advance();
            compileStatements();
            vmWriter.WriteLabel("IF_END" + Integer.toString(ifDepth) );
        }
        else {
            vmWriter.WriteLabel("IF_FALSE" + Integer.toString(ifDepth) );
            look_ahead = true;
        }
    }

    private void CompileExpression() {

        Boolean startOfTerm = true;

        while (!isCloseParenthesis() && !isSemiColon() && !isCloseBracket() && !isComma() ) {
            if (isOperator() && !startOfTerm ) {
                startOfTerm = false;
                Operator operator = getOperator();
                advance();
                CompileTerm();
                vmWriter.WriteArithmetic(operator);
            }
            else {
                CompileTerm();
                startOfTerm = false;
            }
            if (!look_ahead) {
                advance();
            }
            else {
                look_ahead = false;
            }
        }
    }

    private void CompileTerm() {
        if (isOpenParenthesis() ) {
            advance();
            CompileExpression();
        }
        else if (isUnaryOp() ) {
            Operator unaryOp = getUnaryOperator();
            advance();
            CompileTerm();
            vmWriter.WriteArithmetic(unaryOp);
        }
        else if (isIntegerConstant() ) {
            int value = tokenizer.intVal();
            vmWriter.writePush(Segment.CONST, value);
        }
        else if (isStringConstant() ) {
            String stringConst = tokenizer.stringVal();
            int stringSize = stringConst.length();
            vmWriter.writePush(Segment.CONST, stringSize);
            vmWriter.writeCall("String.new", 1);

            for (int i = 0; i < stringSize; i++) {
                String character = Character.toString(stringConst.charAt(i) );
                int asciiCode = (asciiTable.getDecimalCode(character) );
                vmWriter.writePush(Segment.CONST, asciiCode);
                vmWriter.writeCall("String.appendChar", 2);
            }
        }
        else if (isKeyWord() ) {
            if (isTrueKeyWord() ) {
                vmWriter.writePush(Segment.CONST, 0);
                vmWriter.WriteArithmetic(Operator.NOT);
            }
            else if (isFalseKeyWord() ) {
                vmWriter.writePush(Segment.CONST, 0);
            }
            else if (isNullKeyWord() ) {
                vmWriter.writePush(Segment.CONST, 0);
            }
            else {
                vmWriter.writePush(Segment.POINTER, 0);
            }
        }
        else {
            String varName = tokenizer.identifier();
            advance();
            if (isPeriod() ) {
                advance();
                String subroutineOwner = varName;
                String subroutineName =  tokenizer.identifier();
                advance();

                if (subroutineOwner.equals(className)) {
                    compileInternalFunctionCall(className, subroutineName);
                }
                else if (!symbolTable.TypeOf(subroutineOwner).isEmpty() ) {
                    compileExternalMethodCall(subroutineOwner, subroutineName);
                }
                else if (isSystemSubroutine(subroutineOwner) ) {
                    compileSystemCall(subroutineOwner, subroutineName);
                }
                else {
                    compileExternalFunctionCall(subroutineOwner, subroutineName);
                }
            }
            else if (isOpenParenthesis() ) {
                String subroutineOwner = varName;
                compileInternalMethodCall(className, subroutineOwner);
            }
            else if  (isOpenBracket() ) {
                advance();

                CompileExpression();
                vmWriter.writePush(symbolTable.SegmentOf(varName),symbolTable.IndexOf(varName) );
                vmWriter.WriteArithmetic(Operator.ADD);
                vmWriter.writePop(Segment.POINTER, 1);
                vmWriter.writePush(Segment.THAT, 0);
            }
            else {
                Kind varKind = symbolTable.KindOf(varName);
                int varIndex = symbolTable.IndexOf(varName);
                Segment varSegment = symbolTable.SegmentOf(varName);
                vmWriter.writePush(varSegment, varIndex);
                look_ahead = true;
            }
        }
    }

    private int CompileExpressionList() {
        int nArgs = 0;
        while (!isCloseParenthesis() ) {
            CompileExpression();
            nArgs++;
            if (isComma() ) {
                advance();
            }
        }
        return nArgs;
    }

    private void advance() {
        if (tokenizer.hasMoreTokens() ) {
            tokenizer.advance();
        }
    }

    private Boolean isSystemSubroutine(String subroutineOwner) {
        if (subroutineOwner.equals("Math") || subroutineOwner.equals("Array")
                || subroutineOwner.equals("Output") || subroutineOwner.equals("Screen")
                || subroutineOwner.equals("Keyboard") || subroutineOwner.equals("Memory")
                || subroutineOwner.equals("Sys") )
        {
            return true;
        }
        else {
            return false;
        }
    }

    private Boolean isClassVariable() {
        if (tokenizer.tokenType().equals(TokenType.T_KEYWORD) ) {
            if (tokenizer.keyWord().equals(Keyword.STATIC) || tokenizer.keyWord().equals(Keyword.FIELD)) {
                return true;
            }
            return false;
        }
        else {
            return false;
        }
    }

    private Boolean isSubroutine() {
        if (tokenizer.tokenType().equals(TokenType.T_KEYWORD) ) {
            if (tokenizer.keyWord().equals(Keyword.CONSTRUCTOR) || tokenizer.keyWord().equals(Keyword.FUNCTION) || tokenizer.keyWord().equals(Keyword.METHOD) ) {
                return true;
            }
            return false;
        }
        else {
            return false;
        }
    }

    private Boolean isSubroutineVar() {
        if (tokenizer.tokenType().equals(TokenType.T_KEYWORD) ) {
            if (tokenizer.keyWord().equals(Keyword.VAR) ) {
                return true;
            }
            return false;
        }
        else {
            return false;
        }
    }

    private Boolean isKeyWord() {
        if (tokenizer.tokenType().equals(TokenType.T_KEYWORD) ) {
            return true;
        }
        else {
            return false;
        }
    }

    private Boolean isSymbol() {
        if (tokenizer.tokenType().equals(TokenType.T_SYMBOL) ) {
            return true;
        }
        else {
            return false;
        }
    }

    private Boolean isIdentifier() {
        if (tokenizer.tokenType().equals(TokenType.T_ID) ) {
            return true;
        }
        else {
            return false;
        }
    }

    private Boolean isTokenA(String arg) {
        if (tokenizer.tokenType().equals(TokenType.T_SYMBOL) && tokenizer.symbol().equals(arg)) {
            return true;
        }
        else {
            return false;
        }
    }

    //  Most of these can be replaced with isTokenA(String arg) but seem less readable to me
    private Boolean isSemiColon() {
        if (isSymbol() && tokenizer.symbol().equals(";") ) {
            return true;
        }
        else {
            return false;
        }
    }

    private Boolean isOperator() {
        if (isSymbol() && tokenizer.symbol().equals("+") || tokenizer.symbol().equals("-") ||
                tokenizer.symbol().equals("*") || tokenizer.symbol().equals("/") ||
                tokenizer.symbol().equals("&amp;") || tokenizer.symbol().equals("|") ||
                tokenizer.symbol().equals("&lt;") || tokenizer.symbol().equals("&gt;") ||
                tokenizer.symbol().equals("=")
                ) {
            return true;
        }
        else {
            return false;
        }
    }

    private Operator getOperator() {
        if (tokenizer.symbol().equals("+") ) {
            return Operator.ADD;
        }
        else if (tokenizer.symbol().equals("-") ) {
            return Operator.SUB;
        }
        else if (tokenizer.symbol().equals("=") ) {
            return Operator.EQ;
        }
        else if (tokenizer.symbol().equals("&gt;") ) {
            return Operator.GT;
        }
        else if (tokenizer.symbol().equals("&lt;") ) {
            return Operator.LT;
        }
        else if (tokenizer.symbol().equals("&amp;") ) {
            return Operator.AND;
        }
        else if (tokenizer.symbol().equals("|") ) {
            return Operator.OR;
        }
        else if (tokenizer.symbol().equals("*") ) {
            return Operator.MULT;
        }
        else {
            return Operator.DIVIDE;
        }
    }

    private Operator getUnaryOperator() {
        if (tokenizer.symbol().equals("-") ) {
            return Operator.NEG;
        }
        else {
            return Operator.NOT;
        }
    }

    private Boolean isUnaryOp() {
        if (isSymbol() && (tokenizer.symbol().equals("~") || tokenizer.symbol().equals("-") ) ) {
            return true;
        }
        else {
            return false;
        }
    }

    private Boolean isTrueKeyWord() {
        if (isKeyWord() && tokenizer.keyWord().equals(Keyword.TRUE)) {
            return true;
        }
        else {
            return false;
        }
    }

    private Boolean isFalseKeyWord() {
        if (isKeyWord() && tokenizer.keyWord().equals(Keyword.FALSE)) {
            return true;
        }
        else {
            return false;
        }
    }

    private Boolean isNullKeyWord() {
        if (isKeyWord() && tokenizer.keyWord().equals(Keyword.NULL)) {
            return true;
        }
        else {
            return false;
        }
    }

    private Boolean isElse() {
        if (isKeyWord() && tokenizer.keyWord().equals(Keyword.ELSE)) {
            return true;
        }
        else {
            return false;
        }
    }

    private Boolean isComma() {
        if (isSymbol() && tokenizer.symbol().equals(",") ) {
            return true;
        }
        else {
            return false;
        }
    }

    private Boolean isEqualSign() {
        if (isSymbol() && tokenizer.symbol().equals("=") ) {
            return true;
        }
        else {
            return false;
        }
    }

    private Boolean isOpenParenthesis() {
        if (isSymbol() && tokenizer.symbol().equals("(") ) {
            return true;
        }
        else {
            return false;
        }
    }

    private Boolean isCloseParenthesis() {
        if (isSymbol() && tokenizer.symbol().equals(")") ) {
            return true;
        }
        else {
            return false;
        }
    }

    private Boolean isOpenBracket() {
        if (isSymbol() && tokenizer.symbol().equals("[") ) {
            return true;
        }
        else {
            return false;
        }
    }

    private Boolean isCloseBracket() {
        if (isSymbol() && tokenizer.symbol().equals("]") ) {
            return true;
        }
        else {
            return false;
        }
    }

    private Boolean isPeriod() {
        if (isSymbol() && tokenizer.symbol().equals(".") ) {
            return true;
        }
        else {
            return false;
        }
    }

    private Boolean isCloseCurlyBracket() {
        if (isSymbol() && tokenizer.symbol().equals("}") ) {
            return true;
        }
        else {
            return false;
        }
    }

    private Boolean isIntegerConstant() {
        if (tokenizer.tokenType().equals(TokenType.T_INT) ) {
            return true;
        }
        else {
            return false;
        }
    }

    private Boolean isStringConstant() {
        if (tokenizer.tokenType().equals(TokenType.T_STR) ) {
            return true;
        }
        else {
            return false;
        }
    }

    private void print_token() {
        if (tokenizer.tokenType().equals(TokenType.T_SYMBOL) ) {
            JOptionPane.showMessageDialog(null, tokenizer.symbol() );
        }
        if (tokenizer.tokenType().equals(TokenType.T_KEYWORD) ) {
            JOptionPane.showMessageDialog(null, tokenizer.keyWord().toString() );
        }
        if (tokenizer.tokenType().equals(TokenType.T_ID) ) {
            JOptionPane.showMessageDialog(null, tokenizer.identifier() );
        }
        if (tokenizer.tokenType().equals(TokenType.T_INT) ) {
            JOptionPane.showMessageDialog(null, tokenizer.intVal() );
        }
    }
}