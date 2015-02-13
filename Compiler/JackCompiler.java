/**
 * Created by yangyan on 11/26/14.
 */
import java.io.*;
import javax.swing.*;

public class JackCompiler {

    JackTokenizer tokenizer;
    CompilationEngine compilationEngine;
    VMWriter vmWriter;


    public static void main(String[] args) {
        File source;
        JackCompiler compiler = new JackCompiler();
        if (args.length != 1){
            System.out.println("Usage: JackCompiler file|dir");
            return;
        }
        try{
            source = new File(args[0]);
            if (!source.exists()){
                System.out.println("File doesn't exists.");
                return;
            }
            if (source.isDirectory())
                compiler.translateDirectory(source);
            else compiler.translateFile(source);
        } catch (Exception e){
            System.out.println("Error reading/writing files.");
        }
        return;
    }

    /**
     * single file
     * @param current
     * @throws IOException
     */
    private void translateFile(File current) throws IOException {
        String output = current.getAbsolutePath();
        if (!(output.substring(output.length()-5,output.length()).equals(".jack"))) return;
        FileReader r = new FileReader(current);
        output = output.substring(0,output.length()-5) + ".vm" ;
        File vmFile = new File(output);

        try {
            tokenizer = new JackTokenizer(current);
            vmWriter = new VMWriter(new PrintWriter(new FileWriter(vmFile)));
            compilationEngine = new CompilationEngine(tokenizer, vmWriter, vmFile);
        }
        catch (IOException exception) {
            System.err.println("Error creating output stream");
            System.exit(1);
        }
    }

    /**
     * directory
     * @param directory
     * @throws IOException
     */
    private void translateDirectory(File directory) throws IOException {

        File[] files = directory.listFiles();
        for (File file : files)
        {
            if (file.isDirectory()) {
                translateDirectory(file);
            }
            else {
                translateFile(file);}
        }
    }

}