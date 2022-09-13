package com.lb.visitors;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.lb.util.ApplicationInformation;
import com.lb.util.DefaultValues;

import java.util.HashSet;
import java.util.Set;

public class ApplicationVisitor extends ModifierVisitor<ApplicationInformation> {

    @Override
    public MarkerAnnotationExpr visit(MarkerAnnotationExpr n, ApplicationInformation applicationInformation) {
        super.visit(n, applicationInformation);

        if (n.getNameAsString().equals("SpringBootApplication")) {

            ClassOrInterfaceDeclaration c = (ClassOrInterfaceDeclaration) n.getParentNode().get();
            MethodDeclaration mainMethod = c.getMethodsByName("main").get(0);

            if (c.getFullyQualifiedName().isPresent()) {
                applicationInformation.setApplicationName(c.getFullyQualifiedName().get());
            }

            BlockStmt mainBody = new BlockStmt();
            Set<ClassOrInterfaceDeclaration> s = new HashSet<>();

            for (MethodDeclaration md: applicationInformation.getServiceClassMethods()) {

                if (md.getParentNode().isPresent() && md.getParentNode().get() instanceof ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {

                    // Create Service-object
                    if (!s.contains(classOrInterfaceDeclaration)) {
                        s.add(classOrInterfaceDeclaration);

                        var rootCU = n.findRootNode().findCompilationUnit().get();
                        rootCU.addImport(classOrInterfaceDeclaration.getFullyQualifiedName().get());

                        mainBody.addStatement(new AssignExpr(new VariableDeclarationExpr(
                                new ClassOrInterfaceType(
                                        null,
                                        classOrInterfaceDeclaration.getName(),
                                        null),
                                classOrInterfaceDeclaration.getNameAsString().toLowerCase()),
                                new ObjectCreationExpr(
                                        null,
                                        new ClassOrInterfaceType(
                                            null,
                                            classOrInterfaceDeclaration.getNameAsString()),
                                            new NodeList<>()
                                ),
                                AssignExpr.Operator.ASSIGN
                        ));
                    }

                    NameExpr classNameExpr = classOrInterfaceDeclaration.getNameAsExpression();
                    classNameExpr.setName(classNameExpr.getNameAsString().toLowerCase());

                    MethodCallExpr m = new MethodCallExpr(classNameExpr, md.getNameAsString());

                    for (Parameter p : md.getParameters()) {
                        m.addArgument(DefaultValues.getDefaultValue(p.getTypeAsString()));
                    }

                    mainBody.addStatement(m);
                }
            }
            mainMethod.setBody(mainBody);

            return null;
        }

        return n;
    }

    @Override
    public ImportDeclaration visit(ImportDeclaration n, ApplicationInformation applicationInformation) {
        super.visit(n, applicationInformation);

        if (n.getNameAsString().contains("springframework")) {
            return null;
        }

        return n;
    }

}
