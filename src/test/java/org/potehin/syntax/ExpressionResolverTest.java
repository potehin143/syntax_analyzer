package org.potehin.syntax;

import org.junit.Test;

import static java.lang.Math.sqrt;
import static org.junit.Assert.assertEquals;
import static org.potehin.syntax.analyzer.ExpressionResolver.evaluate;

public class ExpressionResolverTest {

    @Test
    public void evaluationTest(){
        assertEquals(10+20, evaluate("10+20"),0);
        assertEquals(10*2+20, evaluate("10*2+20"),0);
        assertEquals(10*(2+20)-4, evaluate("10*(2+20)-4"),0);
        assertEquals(10*(-2+20)-4, evaluate("10*(-2+20)-4"),0);
        assertEquals(10*(-2+20)-sqrt(4), evaluate("10*(-2+20)-sqrt(4)"),0);
    }
}
