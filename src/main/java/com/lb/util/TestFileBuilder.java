package com.lb.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class TestFileBuilder {

    public static void buildTestFile(HashMap<String, Set<Vector>> allTestCases, ApplicationInformation applicationInformation) {

        allTestCases.forEach((className, testCases) -> {

            if (className.equals("")) {
                return;
            }

            try {
                String cn = className.substring(className.lastIndexOf('.') + 1);
                FileWriter fw = new FileWriter(cn + "Test.java");

                String appName = applicationInformation.getApplicationName();
                String servicePackage = applicationInformation.getServiceClassPackage();

                fw.write("package " + servicePackage.substring(0, servicePackage.lastIndexOf('.')) + ";\n");
                fw.write("\n");
                fw.write("import " + appName + ";\n");
                fw.write("import org.junit.Before;\n");
                fw.write("import org.junit.Test;\n");
                fw.write("import org.springframework.boot.test.context.SpringBootTest;\n");
                fw.write("import org.springframework.test.context.junit4.SpringRunner;\n");
                fw.write("import org.junit.runner.RunWith;\n");
                fw.write("import org.mockito.InjectMocks;\n");
                fw.write("\n");
                fw.write("@RunWith(SpringRunner.class)\n");
                fw.write("@SpringBootTest(classes=" + appName.substring(appName.lastIndexOf('.') + 1) + ".class)\n");
                fw.write("public class " + cn + "Test {\n"); // test class
                fw.write("\n");
                fw.write("  @InjectMocks\n");
                fw.write("	private " + cn + " " + cn.toLowerCase() + ";\n"); // CUT object to be tested
                fw.write("\n");
                fw.write("	@Before\n"); // setUp method annotation
                fw.write("	public void setUp() throws Exception {\n"); // setUp method
                fw.write("		" + cn.toLowerCase() + " = new " + cn + "();\n"); // create object for CUT
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
                            fw.write("		" + cn.toLowerCase() + "." + invokedMethod + ";\n"); // invoke a method in the sequence
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
