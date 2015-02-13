/**
 * Created by yangyan on 11/20/14.
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class VMTranslator {

    private static void translateFile(CodeWriter outFile , File inputFile) throws IOException
    {
        outFile.setFileName(inputFile.getName());
        Parser fileParser = new Parser(inputFile);
        while (fileParser.hasMoreCommands()){
            fileParser.advance();
            switch (fileParser.commandType()){
                case C_ARITHMETIC:{
                    outFile.writeArithmetic(fileParser.arg1());
                    break;
                }
                case C_POP:{
                    outFile.WritePushPop(CommandType.C_POP,fileParser.arg1(),fileParser.arg2());
                    break;
                }
                case C_PUSH:{
                    outFile.WritePushPop(CommandType.C_PUSH,fileParser.arg1(),fileParser.arg2());
                    break;
                }
                case C_LABEL:{
                    outFile.writeLabel(fileParser.arg1());
                    break;
                }
                case C_GOTO:{
                    outFile.writeGoto(fileParser.arg1());
                    break;
                }
                case C_IF:{
                    outFile.writeIf(fileParser.arg1());
                    break;
                }
                case C_CALL:{
                    outFile.writeCall(fileParser.arg1(),fileParser.arg2());
                    break;
                }
                case C_FUNCTION:{
                    outFile.writeFunction(fileParser.arg1(),fileParser.arg2());
                    break;
                }
                case C_RETURN:{
                    outFile.writeReturn();
                    break;
                }
            }
        }
    }

    private static void translateDirectory(CodeWriter outFile , File directory) throws IOException
    {
        File[] filesInDirectory = directory.listFiles();
        for (File file : filesInDirectory)
        {
            if (file.isDirectory())
                translateDirectory(outFile, file);
            else if (file.getName().substring(file.getName().length()-3).equals(".vm")) translateFile(outFile,file);
        }
    }

    public static void main(String[] args) {

        File source;
        String outputFileName;
        CodeWriter codeWriter = null;

        if (args.length != 1){
            System.out.println("Usage: java VMTranslator file|dir");
            return;
        }

        try{
            source = new File(args[0]);
            if (!source.exists()){
                System.out.println("File doesn't exists.");
                return;
            }

            if (source.isDirectory())
                outputFileName = new String(args[0]+"/"+source.getName()+".asm");
            else outputFileName = new String(args[0].substring(0,args[0].length()-3) + ".asm");

            codeWriter = new CodeWriter(new BufferedWriter(new FileWriter(outputFileName)));
            codeWriter.writeInit();

            if (source.isDirectory())
                translateDirectory(codeWriter,source);
            else translateFile(codeWriter,source);

            codeWriter.close();
        }catch (Exception e){
            System.out.println("Error reading/writing files.");

        }
        return;

    }

}
