package com.lb.visitors;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;

import java.util.ArrayList;
import java.util.List;

public class ControllerVisitor extends ModifierVisitor<Void> {

    @Override
    public MarkerAnnotationExpr visit(MarkerAnnotationExpr n, Void arg) {
        super.visit(n, arg);

        if (n.getNameAsString().equals("RestController")) {

            var cu = n.findRootNode().findCompilationUnit().get();
            while (!cu.getChildNodes().isEmpty()) {
                cu.getChildNodes().get(0).remove();
            }
        }

        return n;
    }
}
