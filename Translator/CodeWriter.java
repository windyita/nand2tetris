/**
 * Created by yangyan on 11/24/14.
 */
import java.io.BufferedWriter;
import java.io.IOException;

/*
 * Translates VM commands into Hack assembly code.
 */
public class CodeWriter  {

    private int labelNumber;
    private int callNumber;
    private String currentFile;
    private String currentFunction;
    private BufferedWriter outFile;

    public CodeWriter(BufferedWriter outputFile){
        outFile = outputFile;
        labelNumber=0;
        callNumber=0;
        currentFunction = null;
    }

    public void close() throws IOException{
        outFile.close();
    }

    public void setFileName(String fileName){
        currentFile = fileName;
    }

    /**
     * Write assembly code that effect the vm initialization,
     * also called bootstrap code.
     * This code must be placed at the beginning of the output file.
     */
    public void writeInit() throws IOException{
        writeLine("@256");
        writeLine("D=A");
        writeLine("@SP");
        writeLine("M=D");
        writeCall(new String("Sys.init"),0);
    }

    /**
     * Writes assembly code that effects the label command
     */
    public void writeLabel(String label) throws IOException{
        writeLine("("+ currentFunction+"$"+label + ")");
    }

    /**
     * GOTO
     */
    public void writeGoto(String label) throws IOException {
        writeLine("@"+ currentFunction+"$"+label);
        writeLine("0;JMP");
    }

    public void writeFunction(String functionName, int numLocals) throws IOException{
        currentFunction = functionName;
        writeLine("("+functionName+")");
        for (int k=0;k<numLocals;k++)
            writePushCommand(new String("constant"),0);
    }

    public void writeLine(String line) throws IOException {
        outFile.write(line);
        outFile.newLine();
        outFile.flush();

    }

    public void writeIncreaseSP() throws IOException{
        writeLine("@SP");
        writeLine("M=M+1");
    }

    public void writeBinaryPrefix() throws IOException{
        writeLine("@SP");
        writeLine("AM=M-1");
        writeLine("D=M");
        writeLine("@SP");
        writeLine("AM=M-1");
    }

    public void writeUnaryPrefix() throws IOException{
        writeLine("@SP");
        writeLine("AM=M-1");
    }


    public void writeAdd() throws IOException{
        writeBinaryPrefix();
        writeLine("M=M+D");
        writeIncreaseSP();

    }
    public void writeSub() throws IOException{
        writeBinaryPrefix();
        writeLine("M=M-D");
        writeIncreaseSP();
    }
    public void writeNeg() throws IOException{
        writeUnaryPrefix();
        writeLine("M=-M");
        writeIncreaseSP();
    }
    public void writeEq() throws IOException{
        writeBinaryPrefix();
        writeLine("D=M-D");
        writeLine("@EQJUMP_" + labelNumber);
        writeLine("D;JEQ");
        writeLine("D=0");
        writeLine("@EQEND_" + labelNumber);
        writeLine("0;JMP");
        writeLine("(EQJUMP_"+labelNumber+")");
        writeLine("D=-1");
        writeLine("(EQEND_"+labelNumber+")");
        writeLine("@SP");
        writeLine("A=M");
        writeLine("M=D");
        writeIncreaseSP();
        labelNumber++;
    }
    public void writeGt() throws IOException{
        writeBinaryPrefix();
        writeLine("D=M-D");
        writeLine("@GTJUMP_" + labelNumber);
        writeLine("D;JGT");
        writeLine("D=0");
        writeLine("@GTEND_" + labelNumber);
        writeLine("0;JMP");
        writeLine("(GTJUMP_"+labelNumber+")");
        writeLine("D=-1");
        writeLine("(GTEND_"+labelNumber+")");
        writeLine("@SP");
        writeLine("A=M");
        writeLine("M=D");
        writeIncreaseSP();
        labelNumber++;
    }
    public void writeLt() throws IOException{
        writeBinaryPrefix();
        writeLine("D=M-D");
        writeLine("@LTJUMP_"+labelNumber);
        writeLine("D;JLT");
        writeLine("D=0");
        writeLine("@LTEND_"+labelNumber);
        writeLine("0;JMP");
        writeLine("(LTJUMP_"+labelNumber+")");
        writeLine("D=-1");
        writeLine("(LTEND_"+labelNumber+")");
        writeLine("@SP");
        writeLine("A=M");
        writeLine("M=D");
        writeIncreaseSP();
        labelNumber++;
    }

    public void writeAnd() throws IOException{
        writeBinaryPrefix();
        writeLine("M=D&M");
        writeIncreaseSP();

    }
    public void writeOr() throws IOException{
        writeBinaryPrefix();
        writeLine("M=D|M");
        writeIncreaseSP();

    }
    public void writeNot() throws IOException{
        writeUnaryPrefix();
        writeLine("M=!M");
        writeIncreaseSP();
    }



    public void writeArithmetic(String command) throws IOException {
        if (command.equals("add")) {writeAdd(); return;}
        if (command.equals("sub")) {writeSub(); return;}
        if (command.equals("neg")) {writeNeg(); return;}
        if (command.equals("eq")) {writeEq(); return;}
        if (command.equals("gt")) {writeGt(); return;}
        if (command.equals("lt")) {writeLt(); return;}
        if (command.equals("and")) {writeAnd(); return;}
        if (command.equals("or")) {writeOr(); return;}
        if (command.equals("not")) {writeNot(); return;}

    }

    public void pushDRegToStack() throws IOException{
        writeLine("@SP");
        writeLine("A=M");
        writeLine("M=D");
        writeIncreaseSP();
    }

    public void writePushFromPointedSegment(String segment,int index) throws IOException{
        writeLine("@"+index);
        writeLine("D=A");
        writeLine("@"+segment);
        writeLine("A=D+M");
        writeLine("D=M");
        pushDRegToStack();
    }


    public void writePopToPointedSegment(String segment, int index) throws IOException{
        writeLine("@"+index);
        writeLine("D=A");
        writeLine("@"+segment);
        writeLine("D=D+M");
        writeLine("@R13");
        writeLine("M=D");
        writeLine("@SP");
        writeLine("AM=M-1");
        writeLine("D=M");
        writeLine("@R13");
        writeLine("A=M");
        writeLine("M=D");
    }

    public void writePushCommand(String segment , int index) throws IOException{
        if (segment.equals("constant")) {
            writeLine("@"+index);
            writeLine("D=A");
            pushDRegToStack();
            return;
        }

        if (segment.equals("local")){
            writePushFromPointedSegment("LCL", index);
            return;
        }

        if (segment.equals("argument")){
            writePushFromPointedSegment("ARG", index);
            return;
        }

        if (segment.equals("this")){
            writePushFromPointedSegment("THIS", index);
            return;
        }

        if (segment.equals("that")){
            writePushFromPointedSegment("THAT", index);
            return;
        }

        if (segment.equals("pointer")){
            if (index==0)
                writeLine("@THIS");
            else writeLine("@THAT");
            writeLine("D=M");
            pushDRegToStack();
            return;
        }

        if (segment.equals("temp")){
            writeLine("@"+index);
            writeLine("D=A");
            writeLine("@R5");
            writeLine("A=D+A");
            writeLine("D=M");
            pushDRegToStack();
            return;
        }

        if (segment.equals("static")){
            writeLine("@"+currentFile + "." + index);
            writeLine("D=M");
            pushDRegToStack();
            return;
        }
    }

    public void writePopCommand(String segment,int index) throws IOException{
        if (segment.equals("local")){
            writePopToPointedSegment("LCL", index);
            return;
        }

        if (segment.equals("argument")){

            writePopToPointedSegment("ARG", index);
            return;
        }

        if (segment.equals("this")){
            writePopToPointedSegment("THIS", index);
            return;
        }

        if (segment.equals("that")){
            writePopToPointedSegment("THAT", index);
            return;
        }

        if (segment.equals("pointer")){
            writeLine("@SP");
            writeLine("AM=M-1");
            writeLine("D=M");
            if (index==0)
                writeLine("@THIS");
            else writeLine("@THAT");
            writeLine("M=D");
            return;
        }

        if (segment.equals("temp")){
            writeLine("@"+index);
            writeLine("D=A");
            writeLine("@R5");
            writeLine("D=D+A");
            writeLine("@R13");
            writeLine("M=D");
            writeLine("@SP");
            writeLine("AM=M-1");
            writeLine("D=M");
            writeLine("@R13");
            writeLine("A=M");
            writeLine("M=D");
            return;
        }

        if (segment.equals("static")){
            writeLine("@SP");
            writeLine("AM=M-1");
            writeLine("D=M");
            writeLine("@"+currentFile + "." + index);
            writeLine("M=D");
            return;
        }

    }

    public void WritePushPop(CommandType command, String segment, int index) throws IOException{
        if (command == CommandType.C_PUSH)
            writePushCommand(segment,index);
        else if (command == CommandType.C_POP)
            writePopCommand(segment,index);

        return;
    }

    public void writeIf(String label) throws IOException{
        writeLine("@SP");
        writeLine("AM=M-1");
        writeLine("D=M");
        writeLine("@" + currentFunction+"$"+label);
        writeLine("D;JNE");
    }

    public void writeCall(String functionName, int numArgs) throws IOException {
        writeLine("@CALL_"+callNumber);
        writeLine("D=A");
        pushDRegToStack();
        writeLine("@LCL");
        writeLine("D=M");
        pushDRegToStack();
        writeLine("@ARG");
        writeLine("D=M");
        pushDRegToStack();
        writeLine("@THIS");
        writeLine("D=M");
        pushDRegToStack();
        writeLine("@THAT");
        writeLine("D=M");
        pushDRegToStack();
        writeLine("@"+(numArgs+5));
        writeLine("D=A");
        writeLine("@SP");
        writeLine("D=M-D");
        writeLine("@ARG");
        writeLine("M=D");
        writeLine("@SP");
        writeLine("D=M");
        writeLine("@LCL");
        writeLine("M=D");
        writeLine("@"+functionName);
        writeLine("0;JMP");
        writeLine("(CALL_"+callNumber+")");
        callNumber++;
    }

    public void writeReturn() throws IOException{
        writeLine("@LCL");
        writeLine("D=M");
        writeLine("@R15");
        writeLine("M=D");
        writeLine("@5");
        writeLine("A=D-A");
        writeLine("D=M");
        writeLine("@R14");
        writeLine("M=D");
        writePopCommand(new String("argument"),0);
        writeLine("@ARG");
        writeLine("D=M+1");
        writeLine("@SP");
        writeLine("M=D");
        writeLine("@R15");
        writeLine("AM=M-1");
        writeLine("D=M");
        writeLine("@THAT");
        writeLine("M=D");
        writeLine("@R15");
        writeLine("AM=M-1");
        writeLine("D=M");
        writeLine("@THIS");
        writeLine("M=D");
        writeLine("@R15");
        writeLine("AM=M-1");
        writeLine("D=M");
        writeLine("@ARG");
        writeLine("M=D");
        writeLine("@R15");
        writeLine("AM=M-1");
        writeLine("D=M");
        writeLine("@LCL");
        writeLine("M=D");
        writeLine("@R14");
        writeLine("A=M");
        writeLine("0;JMP");
    }


}
