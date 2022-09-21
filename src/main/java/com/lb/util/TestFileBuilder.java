package com.lb.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class TestFileBuilder {

    public static void buildTestFile(HashMap<String, Set<Vector>> allTestCases) {

        allTestCases.forEach((className, testCases) -> {

            if (className.equals("")) {
                return;
            }

            try {
                FileWriter fw = new FileWriter(className + "Test.java");

                fw.write("import static org.junit.Assert.*;\n");
                fw.write("import org.junit.Before;\n");
                fw.write("import org.junit.Test;\n");
                
                String objectName = (className.toLowerCase()).replace(".", "_");
                
                fw.write("\n");
                fw.write("public class " + className.replace(".", "_") + "Test {\n"); // test class
                fw.write("\n");
                fw.write("	private " + className + " " + objectName + ";\n"); // CUT object to be tested
                fw.write("\n");
                fw.write("	@Before\n"); // setUp method annotation
                fw.write("	public void setUp() throws Exception {\n"); // setUp method
                fw.write("		" + objectName + " = new " + className + "();\n"); // create object for CUT
                fw.write("	}\n"); // setUp method end
                // Create a test method for each sequence
                int testIndex = 0;
                Iterator<Vector> it = testCases.iterator();
                
                Set<String> producedTestCases = new HashSet<>();
                
                while (it.hasNext()){
                    Vector<String> methodSequence = it.next();
                    fw.write("");
                    Iterator<String> it1 = methodSequence.iterator();
                
                    if (producedTestCases.contains(methodSequence.get(0))) {
                        continue;
                    }
                
                    if (it1.hasNext()) {
                        String errAnn = (it1.next());
                
                        if (errAnn.contains("expected")) {
                            fw.write("	@Test"+errAnn+"\n"); // Corina: added @Test annotation with exception expected
                        }
                        else {
                            fw.write("	@Test\n"); // @Test annotation
                            it1 = methodSequence.iterator();
                        }
                    }
                    else
                        it1 = methodSequence.iterator();
                
                    //pw.write("	@Test"); // @Test annotation
                    fw.write("	public void test" + testIndex + "() {\n"); // begin test method
                    //Iterator<String> it1 = methodSequence.iterator();
                
                    while(it1.hasNext()){
                        String invokedMethod = it1.next();
                
                        if (invokedMethod.contains("##EXCEPTION## ")) { // error-string. not a method
                            // add a comment about the exception
                            fw.write("		" + "//should lead to " + invokedMethod + "\n");
                        }
                        else{ // normal method
                            producedTestCases.add(invokedMethod);
                            fw.write("		" + objectName + "." + invokedMethod + ";\n"); // invoke a method in the sequence
                        }
                    }
                    fw.write("	}\n"); // end test method
                    testIndex++;
                }
                fw.write("}\n"); // test class end
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
