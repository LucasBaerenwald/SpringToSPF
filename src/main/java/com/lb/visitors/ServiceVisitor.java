package com.lb.visitors;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.lb.util.ApplicationInformation;

public class ServiceVisitor extends ModifierVisitor<ApplicationInformation> {

    @Override
    public ImportDeclaration visit(ImportDeclaration n, ApplicationInformation applicationInformation) {
        super.visit(n, applicationInformation);

        if (n.getNameAsString().contains("Transactional")) {
            return null;
        }

        return n;
    }

    @Override
    public MarkerAnnotationExpr visit(MarkerAnnotationExpr n, ApplicationInformation applicationInformation) {
        super.visit(n, applicationInformation);

        switch (n.getNameAsString()) {
            case "Service" -> {
                ClassOrInterfaceDeclaration serviceClass = (ClassOrInterfaceDeclaration) n.getParentNode().get();
                applicationInformation.getServiceClassMethods().addAll(serviceClass.getMethods());

                if (serviceClass.getFullyQualifiedName().isPresent()) {
                    applicationInformation.setServiceClassPackage(serviceClass.getFullyQualifiedName().get());
                }

                return null;
            }
            case "Transactional" -> {
                return null;
            }
            case "Autowired" -> {
                // TODO: verschiedene Faelle abdecken

                // Constructor-Injection
                if (n.getParentNode().isPresent() && n.getParentNode().get() instanceof ConstructorDeclaration constructorDeclaration) {
                    boolean removeParameter = false;
                    Parameter parameterToRemove = null;

                    for (Parameter p : constructorDeclaration.getParameters()) {
                        if (p.getType().isPrimitiveType()) {
                            continue;
                        }

                        for (Statement statement : constructorDeclaration.getBody().getStatements()) {
                            if (statement instanceof ExpressionStmt && statement.asExpressionStmt().getExpression() instanceof AssignExpr) {
                                AssignExpr assignExpr = statement.asExpressionStmt().getExpression().asAssignExpr();

                                if (assignExpr.getValue().toString().equals(p.getNameAsString())) {
                                    assignExpr.setValue(new ObjectCreationExpr(
                                            null,
                                            new ClassOrInterfaceType(
                                                    null,
                                                    p.getTypeAsString()),
                                            new NodeList<>()));

                                    removeParameter = true;
                                    parameterToRemove = p;
                                }
                            }
                        }
                    }

                    if (removeParameter){
                        constructorDeclaration.remove(parameterToRemove);
                    }
                }

                return null;
            }
        }

        return n;
    }

}
