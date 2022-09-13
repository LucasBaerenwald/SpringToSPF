package com.lb.visitors;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;

public class StringConcatVisitor extends ModifierVisitor<Void> {

    @Override
    public Expression visit(BinaryExpr n, Void arg) {
        super.visit(n, arg);

        if ((n.getLeft() instanceof StringLiteralExpr || n.getRight() instanceof StringLiteralExpr) && n.getOperator().equals(BinaryExpr.Operator.PLUS)) {
            return new StringLiteralExpr("");
        }

        return n;
    }

}
