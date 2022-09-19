package com.lb.util;

public class DefaultValues {

    public static String getDefaultValue (String typeName) {

        switch (typeName) {
            case "byte" ->      { return "(byte) 0"; }
            case "short" ->     { return "(short) 0"; }
            case "int" ->       { return "0"; }
            case "long" ->      { return "0L"; }
            case "float" ->     { return "0.0f"; }
            case "double" ->    { return "0.0d"; }
            case "char" ->      { return "''"; }
            case "String" ->    { return "\"\""; }
            case "boolean" ->   { return "false"; }
            default ->          { return "new " + typeName + "()"; }
        }

    }
}
