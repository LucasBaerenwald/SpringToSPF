package com.lb.util;

public class DefaultValues {

    public static String getDefaultValue (String typeName) {

        switch (typeName) {
            case "Byte", "byte" ->          { return "(byte) 0"; }
            case "Short", "short" ->        { return "(short) 0"; }
            case "Integer", "int" ->        { return "0"; }
            case "Long", "long" ->          { return "0L"; }
            case "Float", "float" ->        { return "0.0f"; }
            case "Double", "double" ->      { return "0.0d"; }
            case "Char", "char" ->          { return "''"; }
            case "Boolean", "boolean" ->    { return "false"; }
            case "String" ->                { return "\"\""; }
            default ->                      { return "new " + typeName + "()"; }
        }

    }
}
