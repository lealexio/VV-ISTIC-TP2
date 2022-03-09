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
        SourceRoot root = new SourceRoot(Paths.get("C:/Users/alexl/Documents/FAC/M2/VV/VV-ISTIC-TP2/code/Exercise4"));
        //PublicElementsPrinter printer = new PublicElementsPrinter();
        PublicPrivateVariableNoGetter printer = new PublicPrivateVariableNoGetter();
        root.parse("", (localPath, absolutePath, result) -> {
            result.ifSuccessful(unit -> unit.accept(printer, null));

            System.out.println("Public getters are missing for private fields : " + printer.privateFields);
            return SourceRoot.Callback.Result.DONT_SAVE;
        });
    }


}
