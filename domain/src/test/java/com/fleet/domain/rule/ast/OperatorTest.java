package com.fleet.domain.rule.ast;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OperatorTest {

    @Test
    void shouldResolveFromSymbol() {
        assertEquals(Operator.GT, Operator.fromSymbol("gt"));
        assertEquals(Operator.LT, Operator.fromSymbol("lt"));
        assertEquals(Operator.GTE, Operator.fromSymbol("gte"));
        assertEquals(Operator.LTE, Operator.fromSymbol("lte"));
        assertEquals(Operator.EQ, Operator.fromSymbol("eq"));
        assertEquals(Operator.NEQ, Operator.fromSymbol("neq"));
        assertEquals(Operator.IN, Operator.fromSymbol("in"));
        assertEquals(Operator.NOT_IN, Operator.fromSymbol("not_in"));
    }

    @Test
    void shouldResolveFromSymbolCaseInsensitive() {
        assertEquals(Operator.IN, Operator.fromSymbol("in"));
        assertEquals(Operator.NOT_IN, Operator.fromSymbol("not_in"));
    }

    @Test
    void shouldThrowForUnknownSymbol() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Operator.fromSymbol("!"));
        assertTrue(ex.getMessage().contains("Unsupported operator symbol"));
    }

    @Test
    void shouldThrowForNullSymbol() {
        assertThrows(IllegalArgumentException.class, () -> Operator.fromSymbol(null));
    }

    @Test
    void shouldExposeSymbol() {
        assertEquals("gt", Operator.GT.getSymbol());
        assertEquals("neq", Operator.NEQ.getSymbol());
        assertEquals("not_in", Operator.NOT_IN.getSymbol());
    }
}
