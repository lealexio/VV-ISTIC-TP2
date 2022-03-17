package fr.istic.vv;

import com.github.javaparser.Problem;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.SourceRoot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Main {

    public static void main(String[] args) throws IOException {

        // Input file
        if(args.length == 0) {
            System.err.println("Should provide the path to the source code and the csv file in which to save the result");
            System.exit(1);
        }

        File inputFile = new File(args[0]);
        if(!inputFile.exists() || !inputFile.isDirectory() || !inputFile.canRead()) {
            System.err.println("Provide a path to an existing readable directory");
            System.exit(2);
        }
        SourceRoot root = new SourceRoot(inputFile.toPath());

        // Output File
        File outputFile;
        if(args.length == 2){
            outputFile = new File(args[1]);
            if(outputFile.isDirectory()) {
                System.err.println("Provide a path to a valid csv File");
                System.exit(2);
            }
            else if(!outputFile.getName().endsWith(".csv")){
                System.err.println("File format must be '.csv'");
                System.exit(2);
            }
        }
        else{
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy_hh'h'mm'm'ss's'");
            String strDate = dateFormat.format(Calendar.getInstance().getTime());
            outputFile = new File(System.getProperty("user.dir")+"/code/Exercise4/output/output_"+strDate+".csv");
            System.out.println("Save in default csv : " + outputFile.getAbsolutePath());
        }

        outputFile.mkdirs();
        PublicPrivateVariableNoGetter printer = new PublicPrivateVariableNoGetter();

        printer.createCsv(outputFile);

        root.parse("", (localPath, absolutePath, result) -> {
            result.ifSuccessful(unit -> unit.accept(printer, null));
            //printer.privateFields.forEach(field -> System.out.println("Public getter is missing for private field '" + field + "' in class '" + printer.className + ".java' of package '" + printer.packageName + "'"));
            printer.toCsv(outputFile);
            return SourceRoot.Callback.Result.DONT_SAVE;
        });
    }


}
