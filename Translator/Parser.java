/**
 * Created by yangyan on 11/19/14.
 */
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class Parser {

    private Scanner file;
    private String[] currentCommand;
    private String nextCommand;
    private HashMap<String, CommandType> commandMap;

    /**
     * Constructor
     */
    Parser(File inputFile) throws IOException{
        file = new Scanner(new FileReader(inputFile));
        nextCommand = null;
        commandMap = new HashMap<String, CommandType>();
        commandMap.put(new String("add"), CommandType.C_ARITHMETIC);
        commandMap.put(new String("sub"), CommandType.C_ARITHMETIC);
        commandMap.put(new String("neg"), CommandType.C_ARITHMETIC);
        commandMap.put(new String("eq"), CommandType.C_ARITHMETIC);
        commandMap.put(new String("gt"), CommandType.C_ARITHMETIC);
        commandMap.put(new String("lt"), CommandType.C_ARITHMETIC);
        commandMap.put(new String("and"), CommandType.C_ARITHMETIC);
        commandMap.put(new String("or"), CommandType.C_ARITHMETIC);
        commandMap.put(new String("not"), CommandType.C_ARITHMETIC);
        commandMap.put(new String("push"), CommandType.C_PUSH);
        commandMap.put(new String("pop"), CommandType.C_POP);
        commandMap.put(new String("label"),CommandType.C_LABEL);
        commandMap.put(new String("goto"),CommandType.C_GOTO);
        commandMap.put(new String("if-goto"),CommandType.C_IF);
        commandMap.put(new String("call"),CommandType.C_CALL);
        commandMap.put(new String("function"),CommandType.C_FUNCTION);
        commandMap.put(new String("return"),CommandType.C_RETURN);
        advanceToNextCommand();
    }

    private boolean checkIsCommand(String line){
        String trimmedLine = line.trim();
        if (!trimmedLine.isEmpty() && !trimmedLine.substring(0,2).equals(new String("//"))) {
            return true;
        }
        else
            return false;
    }

    private void advanceToNextCommand(){
        String currentLine;
        while (file.hasNextLine()){
            currentLine = file.nextLine();
            if (checkIsCommand(currentLine)){
                nextCommand = currentLine;
                return;
            }

        }
        nextCommand = null;
        file.close();
    }


    /**
     * Returns true if there are move commands in the file
     */
    public boolean hasMoreCommands(){
        if (nextCommand != null)
            return true;
        else return false;
    }

    /**
     * Reads the next command from the input and makes it the current command.
     * Should be called only if hasMoreCommands() is true.
     */
    public void advance(){
        currentCommand = nextCommand.trim().split("\\s+");
        advanceToNextCommand();
    }

    /**
     * Returns the type of the current VM command.
     * C_ARITHMETIC is returned for all the arithmetic commands.
     */
    public CommandType commandType(){
        if (currentCommand[0].indexOf("//") != -1) {
            currentCommand[0] = currentCommand[0].substring(0, currentCommand[0].indexOf("//"));
        }
        return commandMap.get(currentCommand[0]);

    }
    
    public String arg1(){
        if (commandMap.get(currentCommand[0]) == CommandType.C_ARITHMETIC) {
            return currentCommand[0];
        }
        else
        {
            if (currentCommand[1].indexOf("//") == -1)
                return currentCommand[1];
            else return currentCommand[1].substring(0,currentCommand[1].indexOf("//"));
        }

    }

    public int arg2(){
        String arg2;
        if (currentCommand.length >= 2)
        {
            if (currentCommand[2].indexOf("//") == -1) {
                arg2 = currentCommand[2];
            }
            else {
                arg2= currentCommand[2].substring(0,currentCommand[2].indexOf("//"));
            }
            return new Integer(arg2).intValue();
        }
        else return -1;
    }
}
