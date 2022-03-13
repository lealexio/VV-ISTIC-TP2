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
public class PublicPrivateVariableTCC extends VoidVisitorWithDefaults<Void> {

    List<VariableDeclarator> privateFields = new ArrayList<>();
    List<VariableDeclarator> publicFields = new ArrayList<>();
    List<VariableDeclarator> allFields = new ArrayList<>();
    Map<MethodDeclaration, List<NameExpr>> fieldsByMethod = new HashMap<>();

    String packageName;
    String className;

    public void visitTypeDeclaration(TypeDeclaration<?> declaration, Void arg) {
        //if(!declaration.isPrivate()) return;
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
        List<NameExpr> nameExpr = new ArrayList<>();
        declaration.getBody().ifPresent(body -> body.getStatements().forEach(stmt -> {
            stmt.getChildNodes().forEach(node -> {
                getFieldsFromNode(nameExpr, node);
            });
        }));
        //System.out.println("\nFields " + nameExpr + " used in declaration \n"+declaration.toString());
        fieldsByMethod.put(declaration, nameExpr);
    }

    /**
     * Add current class fields used in a node
     * @param nameExprNodes list of nameExpr present in method declaration, which are fields of class
     * @param node to explore
     */
    public void getFieldsFromNode(List<NameExpr> nameExprNodes, Node node){
        // If a node is a closed leaflet, thus a NameExpr

        if(allFields.isEmpty()){
            return;
        }
        else if(node instanceof NameExpr ){
            allFields.forEach(f -> {
                // If current expression name if a field of the current class
                if(f.getName().equals(((NameExpr) node).getName())){
                    // Remove doublon
                    if(!nameExprNodes.contains(node)){
                        nameExprNodes.add((NameExpr) node);
                        // Break foreach
                        return;
                    }
                }
            });
        }
        // Recursive loop
        else{
            for(Node child: node.getChildNodes()){
                getFieldsFromNode(nameExprNodes, child);
            }
        }
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
        int directLinks = 0;

        List<List<MethodDeclaration>> doubles = new ArrayList<>();
        // Couple creation
        fieldsByMethod.forEach((methodDeclaration, nameExprs) -> {
            fieldsByMethod.forEach((methodDeclaration1, nameExprs1) -> {
                // If methods are not same
                if(!methodDeclaration.equals(methodDeclaration1)){
                    // Temp couple
                    List<MethodDeclaration> tmp_couple = new ArrayList<>(Arrays.asList(methodDeclaration, methodDeclaration1));

                    // Check if couple already exists
                    boolean exists = false;
                    for(List<MethodDeclaration> d : doubles){
                        if(d.containsAll(tmp_couple)){
                            exists=true;
                            break;
                        }
                    }
                    if(!exists){
                        doubles.add(tmp_couple);
                    }

                }
                //System.out.println(methodDeclaration+"and"+methodDeclaration1);
            });
        });
        // Foreach couple, if there is union between used fields, add one direct link
        for(List<MethodDeclaration> d : doubles){
            if(!Collections.disjoint(fieldsByMethod.get(d.get(0)), fieldsByMethod.get(d.get(1)))){
                directLinks++;
            }
        }

        return directLinks;
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
