package com.fleet.domain.rule.ast;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OperatorTest {

    @Test
    void shouldResolveFromSymbol() {
        assertEquals(Operator.GT, Operator.fromSymbol(">"));
        assertEquals(Operator.LT, Operator.fromSymbol("<"));
        assertEquals(Operator.GTE, Operator.fromSymbol(">="));
        assertEquals(Operator.LTE, Operator.fromSymbol("<="));
        assertEquals(Operator.EQ, Operator.fromSymbol("=="));
        assertEquals(Operator.NEQ, Operator.fromSymbol("!="));
        assertEquals(Operator.IN, Operator.fromSymbol("IN"));
        assertEquals(Operator.NOT_IN, Operator.fromSymbol("NOT_IN"));
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
        assertEquals(">", Operator.GT.getSymbol());
        assertEquals("!=", Operator.NEQ.getSymbol());
        assertEquals("NOT_IN", Operator.NOT_IN.getSymbol());
    }
}
