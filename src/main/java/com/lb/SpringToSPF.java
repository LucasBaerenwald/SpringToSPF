package com.lb;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.SourceRoot;
import com.lb.util.ApplicationInformation;
import com.lb.visitors.*;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFConfigException;
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.symbc.SymbolicInstructionFactory;
import gov.nasa.jpf.symbc.heap.HeapSymbolicListener;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class SpringToSPF {

    private static final Path inputPath = Path.of("input_src");
    private static final Path outputPath = Path.of("output_src");

    public static void main(String[] args) {

        if (!createAndValidateDirectories(inputPath, outputPath)) {
            return;
        }

        try {
            // parse sources
            SourceRoot sourceRoot = new SourceRoot(inputPath);

            CombinedTypeSolver typeSolver = new CombinedTypeSolver();
            typeSolver.add(new ReflectionTypeSolver());
            JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
            sourceRoot.getParserConfiguration().setSymbolResolver(symbolSolver);
            sourceRoot.tryToParse();

            ApplicationInformation applicationInformation = modifySources(sourceRoot);
            sourceRoot.saveAll(outputPath);

            buildModifiedSources("buildModifiedSources.xml");

            List<String> testcases = new ArrayList<>();
            List<MethodDeclaration> l = Collections.singletonList(applicationInformation.getServiceClassMethods().get(1));

            // analyse each method of modified sources service class
            for (MethodDeclaration md : l) {//applicationInformation.getServiceClassMethods()) {
                // build method-call-string
                StringBuilder symbolicMethodCall =
                        new StringBuilder(applicationInformation.getServiceClassPackage() + "." + md.getNameAsString() + "(");
                symbolicMethodCall.append("sym#".repeat(md.getParameters().size()));
                if (md.getParameters().size() > 0) {
                    symbolicMethodCall.deleteCharAt(symbolicMethodCall.length() - 1);
                }
                symbolicMethodCall.append(")");

                testcases.add(
                        analyseMethod(
                                applicationInformation.getApplicationName(),
                                symbolicMethodCall.toString()));
            }

            for (String s : testcases) {
                System.out.println(s);
            }
        } catch (IOException e) {
            System.out.println("Input sourcecode couldn't be parsed");
            e.printStackTrace();
        }

//        StringBuilder symbolicMethods = new StringBuilder();
//
//        for (MethodDeclaration md : applicationInformation.getServiceClassMethods()) {
//            StringBuilder symbolicMethodCall = new StringBuilder(applicationInformation.getServiceClassPackage() + "." + md.getNameAsString() + "(");
//
//            for (int i = 0; i < md.getParameters().size(); i++) {
//                symbolicMethodCall.append("sym#");
//            }
//
//            if (md.getParameters().size() > 0) {
//                symbolicMethodCall.deleteCharAt(symbolicMethodCall.length()-1);
//            }
//
//            symbolicMethodCall.append(")/\n\t,");
//            symbolicMethods.append(symbolicMethodCall);
//        }
//
//        if (applicationInformation.getServiceClassMethods().size() > 0 ) {
//            symbolicMethods.delete(symbolicMethods.length()-4, symbolicMethods.length());
//        }
//
//        try (PrintWriter writer = new PrintWriter("Analysis.jpf")) {
//            writer.println("@using=jpf-symbc");
//
//            writer.println("classpath=./output_build/");
//            writer.println("sourcepath=./output_src/");
//
//            writer.println("target=" + analysisTarget);
//            writer.println("listener=" + listener);
//
//            writer.println("search.multiple_errors=true");
//            writer.println("search.depth_limit = 10");
//
//            writer.println("symbolic.method=" + symbolicMethods);
//            writer.println("symbolic.min_int=-100");
//            writer.println("symbolic.max_int=100");
//            writer.println("symbolic.min_long=-100");
//            writer.println("symbolic.max_long=100");
//
//            writer.println("symbolic.lazy=on");
//            writer.println("symbolic.strings=true");
//            writer.println("symbolic.string_dp=ABC");
//            writer.println("symbolic.string_dp_timeout_ms=3000");
//            writer.println("symbolic.arrays=true");
//            writer.println("symbolic.debug=true");
//
//        }
    }

    public static boolean createAndValidateDirectories(Path inputPath, Path outputPath) {
        try {
            if (!Files.exists(inputPath)) {
                Files.createDirectory(inputPath);
            }
            if (!Files.exists(outputPath)) {
                Files.createDirectory(outputPath);
            }

            try (Stream<Path> entries = Files.list(inputPath)) {
                if (!entries.findFirst().isPresent()) {
                    System.out.println("input_src directory mustn't be empty");
                    return false;
                }
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ApplicationInformation modifySources(SourceRoot sourceRoot) {
        List<ImportDeclaration> fileImportDeclarations = new ArrayList<>();
        ApplicationInformation applicationInformation = new ApplicationInformation();

        // modify sources except main class
        for (CompilationUnit cu : sourceRoot.getCompilationUnits()) {

            ModifierVisitor<?> primitivesVisitor = new PrimitivesVisitor();
            ModifierVisitor<?> stringConcatVisitor = new StringConcatVisitor();
            ModifierVisitor<?> entityVisitor = new EntityVisitor();
            ModifierVisitor<?> repositoryVisitor = new RepositoryVisitor();
            ModifierVisitor<ApplicationInformation> serviceVisitor = new ServiceVisitor();
            ModifierVisitor<?> controllerVisitor = new ControllerVisitor();

            primitivesVisitor.visit(cu, null);
            stringConcatVisitor.visit(cu, null);
            entityVisitor.visit(cu, null);
            repositoryVisitor.visit(cu, null);
            serviceVisitor.visit(cu, applicationInformation);
            controllerVisitor.visit(cu, null);
        }

        // modify main method/class
        for (CompilationUnit cu : sourceRoot.getCompilationUnits()) {

            if (cu.getPackageDeclaration().isPresent()) {
                var packageDeclaration = cu.getPackageDeclaration().get();

                for (TypeDeclaration<?> type : cu.getTypes()) {
                    fileImportDeclarations.add(new ImportDeclaration(packageDeclaration.getNameAsString() + "." + type.getNameAsString(), false, false));
                }
            }
        }

        // add imports of each file to other file for easier compilation-process
        for (CompilationUnit cu : sourceRoot.getCompilationUnits()) {

            for (ImportDeclaration id : fileImportDeclarations) {
                cu.addImport(id);
            }

            ModifierVisitor<ApplicationInformation> applicationVisitor = new ApplicationVisitor();
            applicationVisitor.visit(cu, applicationInformation);
        }

        return applicationInformation;
    }

    public static void buildModifiedSources(String buildFileName) {
        File buildFile = new File(buildFileName);

        DefaultLogger consoleLogger = new DefaultLogger();
        consoleLogger.setErrorPrintStream(System.err);
        consoleLogger.setOutputPrintStream(System.out);
        consoleLogger.setMessageOutputLevel(Project.MSG_INFO);

        Project modifiedProject = new Project();
        modifiedProject.setUserProperty("ant.file", buildFile.getAbsolutePath());
        modifiedProject.addBuildListener(consoleLogger);
        modifiedProject.init();

        ProjectHelper helper = ProjectHelper.getProjectHelper();
        modifiedProject.addReference("ant.projectHelper", helper);
        helper.parse(modifiedProject, buildFile);

        modifiedProject.executeTarget(modifiedProject.getDefaultTarget());
    }

    public static String analyseMethod(String analysisTarget, String symbolicMethodCall) {
        try {
            String[] options = {
                    "+@using=jpf-symbc"
                    , "+classpath=./output_build"
                    , "+sourcepath=./output_src"

                    , "+search.multiple_errors=true"
                    , "+search.depth_limit=30"

                    , "+jvm.insn_factory.class=gov.nasa.jpf.symbc.SymbolicInstructionFactory"
                    , "+symbolic.method=" + symbolicMethodCall
                    , "+symbolic.lazy=on"
                    , "+symbolic.strings=true"
                    , "+symbolic.string_dp=z3str"
                    , "+symbolic.string_dp_timeout_ms=3000"

                    , "+symbolic.min_int=-100"
                    , "+symbolic.max_int=100"
                    , "+symbolic.min_long=-100"
                    , "+symbolic.max_long=100"

                    //,"+symbolic.debug=true"

                    , analysisTarget
            };
            Config conf = JPF.createConfig(options);

            conf.printEntries();

            JPF jpf = new JPF(conf);
            new SymbolicInstructionFactory(conf);
            //SpringToSPFListener listener = new SpringToSPFListener(conf, jpf);
            HeapSymbolicListener listener = new HeapSymbolicListener(conf, jpf);
            jpf.addListener(listener);

            jpf.run();

            return "";//listener.getCurrentTestCases();
        } catch (JPFConfigException cx) {
            System.out.println("Config Exception: ");
            cx.printStackTrace();
        } catch (JPFException jx) {
            System.out.println("JPF Exception: ");
            jx.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "No testcases for " + symbolicMethodCall;
    }
}
