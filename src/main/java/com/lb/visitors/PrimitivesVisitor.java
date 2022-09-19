package com.lb.visitors;

import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.ModifierVisitor;

public class PrimitivesVisitor extends ModifierVisitor<Void> {

    @Override
    public ClassOrInterfaceType visit(ClassOrInterfaceType n, Void arg) {
        super.visit(n, arg);

        // change non-primitive Types to their primitive equivalent
        switch (n.getNameAsString()) {
            case "Byte" -> n.setName("byte");
            case "Short" -> n.setName("short");
            case "Integer" -> n.setName("int");
            case "Long" -> n.setName("long");
            case "Float" -> n.setName("float");
            case "Double" -> n.setName("double");
            // TODO: Weitere Numeric-Datentypen integrieren
        }

        return n;
    }
}