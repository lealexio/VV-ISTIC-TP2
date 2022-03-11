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
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Main {

    public static void main(String[] args) throws IOException {
        Path inputDir = Paths.get(System.getProperty("user.dir")+"/code/Exercise4/input/commons-collections-master/src");
        String csvFilePath = System.getProperty("user.dir")+"/code/Exercise4/output/test.csv";
        SourceRoot root = new SourceRoot(inputDir);

//        if(args.length == 0) {
//            System.err.println("Should provide the path to the source code");
//            System.exit(1);
//        }
//        File file = new File(args[0]);
//        if(!file.exists() || !file.isDirectory() || !file.canRead()) {
//            System.err.println("Provide a path to an existing readable directory");
//            System.exit(2);
//        }

        //PublicElementsPrinter printer = new PublicElementsPrinter();
        PublicPrivateVariableNoGetter printer = new PublicPrivateVariableNoGetter();
        printer.createCsv(csvFilePath);
        root.parse("", (localPath, absolutePath, result) -> {
            result.ifSuccessful(unit -> unit.accept(printer, null));
            //printer.privateFields.forEach(field -> System.out.println("Public getter is missing for private field '" + field + "' in class '" + printer.className + ".java' of package '" + printer.packageName + "'"));
            printer.toCsv(csvFilePath);
            return SourceRoot.Callback.Result.DONT_SAVE;
        });
    }


}
