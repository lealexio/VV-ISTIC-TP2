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

public class Main {

    public static void main(String[] args) throws IOException {
        SourceRoot root = new SourceRoot(Paths.get(System.getProperty("user.dir")+"/code/Exercise4/input"));

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
        root.parse("", (localPath, absolutePath, result) -> {
            result.ifSuccessful(unit -> unit.accept(printer, null));
            printer.privateFields.forEach(field -> System.out.println("Public getter is missing for private field '" + field + "' in class '" + printer.className + ".java' of package '" + printer.packageName + "'"));
            printer.toCsv(System.getProperty("user.dir")+"/code/Exercise4/output/result.csv");
            return SourceRoot.Callback.Result.DONT_SAVE;
        });
    }


}
