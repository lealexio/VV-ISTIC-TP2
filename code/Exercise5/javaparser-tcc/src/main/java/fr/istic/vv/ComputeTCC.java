package fr.istic.vv;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;
import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static guru.nidi.graphviz.attribute.Rank.RankDir.LEFT_TO_RIGHT;
import static guru.nidi.graphviz.model.Factory.*;
import static java.util.Collections.list;

// This class visits a compilation unit and
// prints all public enum, classes or interfaces along with their public methods
public class ComputeTCC extends VoidVisitorWithDefaults<Void> {

    List<VariableDeclarator> privateFields = new ArrayList<>();
    List<VariableDeclarator> publicFields = new ArrayList<>();
    List<VariableDeclarator> allFields = new ArrayList<>();
    Map<MethodDeclaration, Set<NameExpr>> fieldsByMethod = new HashMap<>();
    List<List<MethodDeclaration>> directPairs = new ArrayList<>();

    String packageName;
    String className;



    public void visitTypeDeclaration(TypeDeclaration<?> declaration, Void arg) {
        //if(!declaration.isPrivate()) return;
        privateFields = new ArrayList<>();
        publicFields = new ArrayList<>();
        allFields = new ArrayList<>();
        fieldsByMethod = new HashMap<>();
        directPairs = new ArrayList<>();
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
        this.computeDirectPairs();

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
            //System.out.println("\nFields " + nameExpr + " used in declaration \n"+declaration.toString());
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

    public void computeDirectPairs(){
        this.directPairs = new ArrayList<>();
        // Couple creation
        fieldsByMethod.forEach((methodDeclaration, nameExprs) -> {
            fieldsByMethod.forEach((methodDeclaration1, nameExprs1) -> {
                // If methods are not same
                if(!methodDeclaration.equals(methodDeclaration1)){

                    // Check if couple already exists
                    boolean exists = false;
                    for(List<MethodDeclaration> d : this.directPairs){
                        if(d.contains(methodDeclaration) && d.contains(methodDeclaration1)){
                            exists=true;
                            break;
                        }
                    }
                    if(!exists && !Collections.disjoint(fieldsByMethod.get(methodDeclaration), fieldsByMethod.get(methodDeclaration1))){
                        this.directPairs.add(new ArrayList<>(Arrays.asList(methodDeclaration, methodDeclaration1)));
                    }

                }
            });
        });
    }

    public int getDirectPairs(){
        return this.directPairs.size();
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


    public Set<NameExpr> intersect(Set<NameExpr> s1, Set<NameExpr> s2){
        Set<NameExpr> rt = new HashSet<>();
        if(s1.size()>s2.size()){
            s2.forEach( item -> {
                if(s1.contains(item)){
                    rt.add(item);
                }
            });
        }
        else{
            s1.forEach( item -> {
                if(s2.contains(item)){
                    rt.add(item);
                }
            });
        }
        return rt;
    }

    public void toGraph(File pngFile){

        List<guru.nidi.graphviz.model.Node> nodes = new ArrayList<>();
        directPairs.forEach(pair -> {
            nodes.add(node(pair.get(0).getNameAsString()).link(to(node(pair.get(1).getNameAsString())).with(Label.of(intersect(fieldsByMethod.get(pair.get(0)), fieldsByMethod.get(pair.get(1))).toString()))));
        });

        guru.nidi.graphviz.model.Node[] nodesArray = new guru.nidi.graphviz.model.Node[directPairs.size()];
        nodes.toArray(nodesArray);

        Graph g = graph("").with(nodesArray);

        try{
            Graphviz.fromGraph(g).height(1500).render(Format.PNG).toFile(pngFile);
        }catch (IOException e){
            System.out.println("Cannot save graph to " + pngFile.getAbsolutePath());
        }

    }

}
