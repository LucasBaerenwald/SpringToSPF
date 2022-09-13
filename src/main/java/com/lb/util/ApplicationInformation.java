package com.lb.util;

import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.ArrayList;
import java.util.List;

public class ApplicationInformation {

    private String applicationName;
    private String serviceClassPackage;
    private List<MethodDeclaration> serviceClassMethods;

    public ApplicationInformation() {
        applicationName = "";
        serviceClassPackage = "";
        serviceClassMethods = new ArrayList<>();
    }


    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public List<MethodDeclaration> getServiceClassMethods() {
        return serviceClassMethods;
    }

    public void setServiceClassMethods(List<MethodDeclaration> serviceClassMethods) {
        this.serviceClassMethods = serviceClassMethods;
    }

    public String getServiceClassPackage() {
        return serviceClassPackage;
    }

    public void setServiceClassPackage(String serviceClassPackage) {
        this.serviceClassPackage = serviceClassPackage;
    }
}
