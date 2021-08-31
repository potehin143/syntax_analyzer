package org.potehin.syntax.analyzer;

public class RegexPatterns {

    public static String NUMBER = "\\d+(\\.\\d*)?";
    public static String SIGNED_NUMBER = "-?" + NUMBER;
    public static String OPS = "[\\+\\-\\*\\/\\%]";
    public static String SUB_FORMULA = "\\([^()]*\\)";
    public static String HIGH_PRIORITY_EXPRESSION  =  NUMBER + "([\\*\\/]" + NUMBER + ")+";
    public static String FUNCTION_NAME = "[a-z]+[\\(]";

}
