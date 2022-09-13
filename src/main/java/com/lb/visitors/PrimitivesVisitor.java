package com.lb.visitors;

import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.ModifierVisitor;

public class PrimitivesVisitor extends ModifierVisitor<Void> {

    @Override
    public ClassOrInterfaceType visit(ClassOrInterfaceType n, Void arg) {
        super.visit(n, arg);

        // change non-primitive Types to their primitive equivalent
        switch (n.getNameAsString()) {
            case "Integer" -> n.setName("int");
            case "Long" -> n.setName("long");
            // TODO: BigDecimal
        }

        return n;
    }
}