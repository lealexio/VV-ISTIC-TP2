package fr.istic.vv;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;
import java.io.*;
import java.util.*;

// This class visits a compilation unit and
// prints all public enum, classes or interfaces along with their public methods
public class ComputeTCC extends VoidVisitorWithDefaults<Void> {

    List<VariableDeclarator> privateFields = new ArrayList<>();
    List<VariableDeclarator> publicFields = new ArrayList<>();
    List<VariableDeclarator> allFields = new ArrayList<>();
    Map<MethodDeclaration, Set<NameExpr>> fieldsByMethod = new HashMap<>();

    String packageName;
    String className;

    public void visitTypeDeclaration(TypeDeclaration<?> declaration, Void arg) {
        //if(!declaration.isPrivate()) return;
        privateFields = new ArrayList<>();
        publicFields = new ArrayList<>();
        allFields = new ArrayList<>();
        fieldsByMethod = new HashMap<>();
        System.out.println(declaration.getFullyQualifiedName().orElse("[Anonymous]"));

        for(FieldDeclaration field : declaration.getFields()) {
            field.accept(this, arg);
        }

        for(MethodDeclaration method : declaration.getMethods()) {
            method.accept(this, arg);
        }
        // Printing nested types in the top level
        for(BodyDeclaration<?> member : declaration.getMembers()) {
            if (member instanceof TypeDeclaration)
                member.accept(this, arg);
        }
    }

    @Override
    public void visit(CompilationUnit unit, Void arg) {
        unit.getPackageDeclaration().ifPresentOrElse(
                decl -> packageName=unit.getPackageDeclaration().get().getNameAsString(),
                () -> packageName = "Undefined"
        );

        for(TypeDeclaration<?> type : unit.getTypes()) {
            type.accept(this, null);
        }
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, Void arg) {
        className=declaration.getNameAsString();
        visitTypeDeclaration(declaration, arg);
    }

    @Override
    public void visit(EnumDeclaration declaration, Void arg) {
        visitTypeDeclaration(declaration, arg);
    }

    @Override
    public void visit(FieldDeclaration field, Void arg) {
        if(field.isPrivate()){
            privateFields.addAll(field.getVariables());
        }
        else if(field.isPublic()){
            publicFields.addAll(field.getVariables());
        }
        allFields.addAll(field.getVariables());
    }

    @Override
    public void visit(MethodDeclaration declaration, Void arg) {
        Set<NameExpr> nameExpr = new HashSet<>();

        if(!allFields.isEmpty()){

            if(declaration.getBody().isPresent()){
                nameExpr.addAll(declaration.getBody().get().findAll(NameExpr.class));
            }
            System.out.println("\nFields " + nameExpr + " used in declaration \n"+declaration.toString());
        }

        fieldsByMethod.put(declaration, nameExpr);
    }

    public float getTCC(){
        if(allFields.size()==0){
            return 0;
        }
        else{
            return (float)getDirectPairs() / (float)getPairs();
        }

    }

    public int getDirectPairs(){

        List<List<MethodDeclaration>> directPairs = new ArrayList<>();
        // Couple creation
        fieldsByMethod.forEach((methodDeclaration, nameExprs) -> {
            fieldsByMethod.forEach((methodDeclaration1, nameExprs1) -> {
                // If methods are not same
                if(!methodDeclaration.equals(methodDeclaration1)){

                    // Check if couple already exists
                    boolean exists = false;
                    for(List<MethodDeclaration> d : directPairs){
                        if(d.contains(methodDeclaration) && d.contains(methodDeclaration1)){
                            exists=true;
                            break;
                        }
                    }
                    if(!exists && !Collections.disjoint(fieldsByMethod.get(methodDeclaration), fieldsByMethod.get(methodDeclaration1))){
                        directPairs.add(new ArrayList<>(Arrays.asList(methodDeclaration, methodDeclaration1)));
                    }

                }
            });
        });
        return directPairs.size();
    }

    public int getPairs(){
        int nbPairs = 0;
        for(int i = 1; i < fieldsByMethod.size(); i++) {
            nbPairs += fieldsByMethod.size()-i;
        }
        return nbPairs;
    }

    public void createCsv(File csvFile){
        try {
            if (csvFile.createNewFile()) {
                System.out.println("File created : " + csvFile.getAbsolutePath());
            }
            PrintWriter writer = new PrintWriter(csvFile, "UTF-8");

            // Columns names
            writer.println("Package,Class,TCC");

        } catch (IOException e) {
            System.out.println("An error occurred");
            e.printStackTrace();
        }
    }

    public void toCsv(File csvFile) {
        try {
            PrintWriter writer = new PrintWriter(new FileOutputStream(csvFile, true));

            // Columns name
            writer.println(packageName+","+className+".java,"+getTCC());
            writer.close();

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

}
