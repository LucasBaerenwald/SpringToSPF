package com.lb.visitors;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.visitor.ModifierVisitor;

import static com.github.javaparser.StaticJavaParser.parseType;

public class RepositoryVisitor extends ModifierVisitor<Void> {

    @Override
    public ClassOrInterfaceDeclaration visit(ClassOrInterfaceDeclaration n, Void arg) {
        super.visit(n, arg);

        for (ClassOrInterfaceType c: n.getExtendedTypes()) {
            if (c.getName().toString().equals("JpaRepository")) {

                if (c.getTypeArguments().isPresent()) {
                    adjustRepositoryClass(n, c.getTypeArguments().get().get(0).asClassOrInterfaceType());
                }

            }
        }

        return n;
    }

    @Override
    public ImportDeclaration visit(ImportDeclaration n, Void arg) {
        super.visit(n, arg);

        if (n.getNameAsString().equals("org.springframework.data.jpa.repository.JpaRepository")) {
            return null;
        }

        return n;
    }

    private void adjustRepositoryClass(ClassOrInterfaceDeclaration n, ClassOrInterfaceType entityType) {

        Node parentNode = n.getParentNode().get();
        CompilationUnit parentCU = parentNode.findCompilationUnit().get();

        parentCU.addImport("gov.nasa.jpf.vm.Verify");
        parentCU.addImport("java.util.ArrayList");
        parentCU.addImport("java.util.List");
        parentCU.addImport("java.util.Optional");

        var entityName = entityType.getNameAsString();

        n.setInterface(false);
        n.setExtendedTypes(new NodeList<>());

        // add findById-method
        n.addMethod("findById", Modifier.Keyword.PUBLIC)
                .setType("Optional<" + entityName + ">")
                .setParameters(new NodeList<>(
                    new Parameter(PrimitiveType.longType(), "id"))
                )
                .setBody(new BlockStmt()
                    .addStatement("boolean choice = Verify.getBoolean();")
                    .addStatement(new IfStmt(new NameExpr("choice"),
                            new ReturnStmt("Optional.of(new " + entityName + "())"),
                            new ReturnStmt("Optional.empty()"))
                    )
                );

        // add findAll-method
        n.addMethod("findAll", Modifier.Keyword.PUBLIC)
                .setType("List<" + entityName + ">")
                .setBody(new BlockStmt()
                        .addStatement("boolean choice = Verify.getBoolean();")
                        .addStatement(new IfStmt(new NameExpr("choice"),
                                new BlockStmt()
                                        .addStatement("List<" + entityName + "> list = new ArrayList<>();")
                                        .addStatement("list.add(new " + entityName + "());")
                                        .addStatement(new ReturnStmt("list")),
                                new ReturnStmt("new ArrayList<>()")
                                )
                        )
                );

        // add save-method
        n.addMethod("save", Modifier.Keyword.PUBLIC)
                .setParameters(new NodeList<>(
                        new Parameter(parseType(entityName), "p"))
                );

        // add deleteById-method
        n.addMethod("deleteById", Modifier.Keyword.PUBLIC)
                .setParameters(new NodeList<>(
                        new Parameter(PrimitiveType.longType(), "id"))
                );
    }

}


