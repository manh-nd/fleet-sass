package com.fleet.domain.rule.ast;

/**
 * Supported comparison operators for {@link ConditionNode}.
 * Using an enum eliminates stringly-typed operators and prevents
 * invalid operator values from being constructed.
 */
public enum Operator {
    GT("gt"),
    LT("lt"),
    GTE("gte"),
    LTE("lte"),
    EQ("eq"),
    NEQ("neq"),
    IN("in"),
    NOT_IN("not_in");

    private final String symbol;

    Operator(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    /**
     * Resolves an {@link Operator} from its string symbol (e.g. "&gt;", "IN").
     *
     * @param symbol the operator string as stored in JSON / DB
     * @return matching {@link Operator}
     * @throws IllegalArgumentException if the symbol is not supported
     */
    public static Operator fromSymbol(String symbol) {
        if (symbol == null) {
            throw new IllegalArgumentException("Operator symbol must not be null");
        }
        for (Operator op : values()) {
            if (op.symbol.equalsIgnoreCase(symbol)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Unsupported operator symbol: " + symbol);
    }
}
