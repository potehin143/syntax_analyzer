package org.potehin.syntax;

import org.junit.Assert;
import org.junit.Test;
import org.potehin.syntax.analyzer.RegexPatterns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTests {

    @Test
    public void matchNumberTest(){
        Pattern pattern = Pattern.compile(RegexPatterns.NUMBER);
        Matcher matcher = pattern.matcher("(1234.678 + 10)");
        Assert.assertTrue(matcher.find());
        Assert.assertEquals(matcher.start(),1);
        Assert.assertEquals(matcher.end(),9);
    }

    @Test
    public void matchSignedNumberTest(){
        Pattern pattern = Pattern.compile(RegexPatterns.SIGNED_NUMBER);
        Matcher matcher = pattern.matcher("(-1234.678 + 10)");
        Assert.assertTrue(matcher.find());
        Assert.assertEquals(matcher.start(),1);
        Assert.assertEquals(matcher.end(),10);
    }

    @Test
    public void matchOperationsTest(){
        Pattern pattern = Pattern.compile(RegexPatterns.OPS);
        Matcher matcher = pattern.matcher("(1234.678 + 10)");
        Assert.assertTrue(matcher.find());
        Assert.assertEquals(matcher.start(),10);
        Assert.assertEquals(matcher.end(),11);
    }


    @Test
    public void matchSubFormulaTest(){
        Pattern pattern = Pattern.compile(RegexPatterns.SUB_FORMULA);
        Matcher matcher = pattern.matcher("1234.678 + (10.2+20) + 10");
        Assert.assertTrue(matcher.find());
        Assert.assertEquals(matcher.start(),11);
        Assert.assertEquals(matcher.end(),20);

        matcher = pattern.matcher("1234.678 + (10.2+20.10-40.1*20.1/33.85) + 10");
        Assert.assertTrue(matcher.find());
        Assert.assertEquals(matcher.start(),11);
        Assert.assertEquals(matcher.end(),39);
    }

    @Test
    public void matchHighPriorityExpressionTest(){
        Pattern pattern = Pattern.compile(RegexPatterns.HIGH_PRIORITY_EXPRESSION);
        Matcher matcher = pattern.matcher("20+1234.678*10+14");
        Assert.assertTrue(matcher.find());
        Assert.assertEquals(3, matcher.start());
        Assert.assertEquals(14,matcher.end());

        matcher = pattern.matcher("1234.678*20.1/33.85");
        Assert.assertTrue(matcher.find());
        Assert.assertEquals(matcher.start(),0);
        Assert.assertEquals(matcher.end(),19);
    }

    @Test
    public void matchFunctionNameTest(){
        Pattern pattern = Pattern.compile(RegexPatterns.FUNCTION_NAME);
        Matcher matcher = pattern.matcher("sqrt(4)");
        Assert.assertTrue(matcher.find());
        Assert.assertEquals(matcher.start(),0);
        Assert.assertEquals(matcher.end(),5);
    }
}
