package fr.istic.vv;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;
import java.util.ArrayList;
import java.util.List;


// This class visits a compilation unit and
// prints all public enum, classes or interfaces along with their public methods
public class PublicPrivateVariableNoGetter extends VoidVisitorWithDefaults<Void> {

    List<VariableDeclarator> privateFields = new ArrayList<>();

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
        for(TypeDeclaration<?> type : unit.getTypes()) {
            type.accept(this, null);
        }
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, Void arg) {
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
    }

    @Override
    public void visit(MethodDeclaration declaration, Void arg) {
        //if(!declaration.isPublic()) return;

        if(declaration.isPublic()) {
            declaration.getBody().ifPresent(body -> body.getStatements().forEach(stmt -> {
                if (stmt.isReturnStmt()) {
                    if (stmt.asReturnStmt().getExpression().isPresent()) {
                        privateFields.removeIf(pfield -> declaration.getNameAsString().toLowerCase().replace("get", "").equals(pfield.getName().asString()) && stmt.asReturnStmt().getExpression().get().toString().equals(pfield.getName().asString()));
                    }
                }
            }));
        }
    }

}
