package org.potehin.syntax.analyzer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionResolver {

    private static final Pattern numberPattern = Pattern.compile(RegexPatterns.NUMBER);
    private static final Pattern signedNumberPattern = Pattern.compile(RegexPatterns.SIGNED_NUMBER);
    private static final Pattern operationPattern = Pattern.compile(RegexPatterns.OPS);
    private static final Pattern subExpressionPattern = Pattern.compile(RegexPatterns.HIGH_PRIORITY_EXPRESSION);
    private static final Pattern subExpressionInBracketsPattern = Pattern.compile(RegexPatterns.SUB_FORMULA);
    private static final Pattern functionNamePattern = Pattern.compile(RegexPatterns.FUNCTION_NAME);

    private static final Set<String> validFunctionNames = new HashSet<>(Arrays.asList(
            "sin", "cos", "tan", "asin", "acos", "atan",
            "toRadians", "toDegrees", "exp", "log", "log10", "sqrt", "cbrt",
            "ceil", "floor", "rint", "atan2", "pow", "round",
            "sinh", "cosh", "tanh"));

    public static Double evaluate(String formula) {
        String trimmedFormula = formula.trim();
        Double result = 0D;
        int offset = 0;
        Stack<String> ops = new Stack<>();
        Stack<Double> values = new Stack<>();
        while (offset < formula.length()) {
            ExpressionElement nextElement = getNextElement(trimmedFormula.substring(offset), offset==0);
            switch (nextElement.elementType) {
                case NUMBER:
                case SIGNED_NUMBER:
                    if (!ops.empty()) {
                        compute(ops, values, Double.parseDouble(nextElement.text));
                    } else {
                        values.push(Double.parseDouble(nextElement.text));
                    }
                    break;
                case OPERATOR:
                    ops.push(nextElement.text);
                    break;
                case SUB_EXPRESSION:
                    if (!ops.empty()) {
                        compute(ops, values, evaluate(nextElement.text));
                    } else {
                        values.push(evaluate(nextElement.text));
                    }
                    break;
                case SUB_EXPRESSION_IN_BRACKETS:
                    String subExpression = nextElement.text.substring(1,nextElement.text.length()-1);
                    if (!ops.empty()) {
                        compute(ops, values, evaluate(subExpression));
                    } else {
                        values.push(evaluate(subExpression));
                    }
                break;
                case FUNCTION:
                    String functionName = nextElement.text;
                    if(validFunctionNames.contains(functionName)){
                        ops.push(functionName);
                    } else {
                        throw new IllegalArgumentException("Unexpected text "
                                + functionName + "at position" + offset);
                    }

                    break;
                default:
                    throw new IllegalArgumentException("unexpected value "
                            + nextElement.text + "in position " + offset);
            }
            offset = offset + nextElement.text.length();
        }
        if (!values.empty()) {
            result = values.pop();
        }
        return result;
    }

    private static void compute(Stack<String> ops, Stack<Double> values, Double nextValue) {
        String operation = ops.pop();
        switch (operation) {
            case "+":
                values.push(values.pop() + nextValue);
                break;
            case "-":
                values.push(values.pop() - nextValue);
                break;
            case "*":
                values.push(values.pop() * nextValue);
                break;
            case "/":
                values.push(values.pop() / nextValue);
                break;
            case "%":
                values.push(values.pop() % nextValue);
            break;
            default:
                if(validFunctionNames.contains(operation)){

                    try {
                        Method method =  Math.class.getDeclaredMethod(operation, double.class);
                        method.setAccessible(true);
                        double value = (double)method.invoke(null, nextValue);
                        if(!ops.empty()){
                            compute(ops,values,value);
                        } else {
                            values.push(value);
                        }
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
        }
    }

    private static ExpressionElement getNextElement(String expression, boolean matchSignedNumber) {
        MatchingResult matchingResult;
        matchingResult = tryPattern(subExpressionInBracketsPattern, expression);
        if (matchingResult.match) {
            return ExpressionElement.of(ElementType.SUB_EXPRESSION_IN_BRACKETS, matchingResult);
        }
        matchingResult = tryPattern(subExpressionPattern, expression);
        if (matchingResult.match &&
                matchingResult.endPosition-matchingResult.startPosition < expression.length()) {
            return ExpressionElement.of(ElementType.SUB_EXPRESSION, matchingResult);
        }
        if(matchSignedNumber){
            matchingResult = tryPattern(signedNumberPattern, expression);
            if (matchingResult.match) {
                return ExpressionElement.of(ElementType.SIGNED_NUMBER, matchingResult);
            }
        }
        matchingResult = tryPattern(numberPattern, expression);
        if (matchingResult.match) {
            return ExpressionElement.of(ElementType.NUMBER, matchingResult);
        }
        matchingResult = tryPattern(operationPattern, expression);
        if (matchingResult.match) {
            return ExpressionElement.of(ElementType.OPERATOR, matchingResult);
        }

        matchingResult = tryPattern(functionNamePattern, expression);
        if (matchingResult.match) {
            return new ExpressionElement(ElementType.FUNCTION,
                    matchingResult.text.substring(matchingResult.startPosition, matchingResult.endPosition-1));
        }

        throw new IllegalArgumentException("Unexpected text " + expression);
    }

    private static MatchingResult tryPattern(Pattern pattern, String expression) {
        Matcher matcher = pattern.matcher(expression);
        if (matcher.find() && matcher.start() == 0) {
            return new MatchingResult(true, expression, matcher.start(), matcher.end());
        } else {
            return new MatchingResult(false, expression, -1, -1);
        }
    }

    static class MatchingResult {
        private final boolean match;
        private final String text;
        private final int startPosition;
        private final int endPosition;

        public MatchingResult(boolean match, String text, int startPosition, int endPosition) {
            this.match = match;
            this.text = text;
            this.startPosition = startPosition;
            this.endPosition = endPosition;
        }
    }

    static class ExpressionElement {
        private final ElementType elementType;
        private final String text;

        ExpressionElement(ElementType elementType, String text) {
            this.elementType = elementType;
            this.text = text;
        }

        public static ExpressionElement of(ElementType elementType, MatchingResult matchingResult) {
            return new ExpressionElement(elementType,
                    matchingResult.text.substring(matchingResult.startPosition, matchingResult.endPosition));
        }
    }
}
