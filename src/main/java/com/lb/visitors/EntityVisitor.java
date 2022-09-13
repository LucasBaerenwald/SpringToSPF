package com.lb.visitors;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.lb.util.DefaultValues;

import java.util.Optional;

public class EntityVisitor extends ModifierVisitor<Void> {

    @Override
    public ImportDeclaration visit(ImportDeclaration n, Void arg) {
        super.visit(n, arg);

        if (n.getNameAsString().equals("javax.persistence")) {
            return null;
        }

        return n;
    }

    @Override
    public NormalAnnotationExpr visit(NormalAnnotationExpr n, Void arg) {
        super.visit(n, arg);

        if (n.getNameAsString().equals("GeneratedValue")) {
            return null;
        }

        return n;
    }

    @Override
    public MarkerAnnotationExpr visit(MarkerAnnotationExpr n, Void arg) {
        super.visit(n, arg);

        switch (n.getNameAsString()) {
            case "Entity" -> {
                // TODO: Abfragen, ob schon ein Konstruktor vorhanden ist
                addConstructorWithParametersForEntity(n);
                addConstructorForEntity(n);
                return null;
            }
            case "Table", "Id" -> {
                return null;
            }
        }

        return n;
    }

    private void addConstructorForEntity(MarkerAnnotationExpr n) {

        Optional<Node> parent = n.getParentNode();

        if (parent.isEmpty() || !parent.get().getClass().equals(ClassOrInterfaceDeclaration.class)) {
            return;
        }

        ClassOrInterfaceDeclaration classDeclaration = (ClassOrInterfaceDeclaration) parent.get();
        ConstructorDeclaration constructorDeclaration = classDeclaration.addConstructor(Modifier.Keyword.PUBLIC);

        BlockStmt constructorBody = new BlockStmt();

        for (FieldDeclaration fd : classDeclaration.getFields()) {
            for (VariableDeclarator vd : fd.getVariables()) {
                constructorBody.addStatement(new ExpressionStmt(new AssignExpr(
                        new FieldAccessExpr(new ThisExpr(), vd.toString()),
                        new NameExpr(DefaultValues.getDefaultValue(fd.getCommonType().toString())),
                        AssignExpr.Operator.ASSIGN)));
            }
        }

        constructorDeclaration.setBody(constructorBody);
    }

    private void addConstructorWithParametersForEntity(MarkerAnnotationExpr n) {

        Optional<Node> parent = n.getParentNode();

        if (parent.isEmpty() || !parent.get().getClass().equals(ClassOrInterfaceDeclaration.class)) {
            return;
        }

        ClassOrInterfaceDeclaration classDeclaration = (ClassOrInterfaceDeclaration) parent.get();
        ConstructorDeclaration constructorDeclaration = classDeclaration.addConstructor(Modifier.Keyword.PUBLIC);

        BlockStmt constructorBody = new BlockStmt();

        for (FieldDeclaration fd : classDeclaration.getFields()) {
            for (VariableDeclarator vd : fd.getVariables()) {
                constructorDeclaration.addParameter(fd.getCommonType(), vd.toString());

                constructorBody.addStatement(new ExpressionStmt(new AssignExpr(
                        new FieldAccessExpr(new ThisExpr(), vd.toString()),
                        new NameExpr(vd.toString()),
                        AssignExpr.Operator.ASSIGN)));
            }
        }

        constructorDeclaration.setBody(constructorBody);
    }
}
