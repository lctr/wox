package wox;

import java.util.HashMap;
import java.util.Map;

public enum TokenType {
    // Single-character tokens.
    PAREN_L, PAREN_R,
    BRACK_L, BRACK_R,
    CURLY_L, CURLY_R,
    COMMA, DOT, MINUS, PLUS, PLUS_PLUS, SEMICOLON, SLASH, STAR,

    // One or two character tokens.
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Literals.
    UNERLINE, IDENT, STRING, NUMBER,

    // Keywords.
    CLASS, THIS, SUPER,
    FN, IF, THEN, ELSE,
    DO, LOOP,
    FOR, WHILE, CASE, OF,
    LET, IN, VAR,
    FALSE, TRUE, NIL,
    AND, OR,
    PRINT,
    RETURN,

    EOF;

    public boolean isLiteral() {
        return this == IDENT || this == STRING || this == NUMBER;
    }

    public boolean isWildcard() {
        return this == UNERLINE;
    }

    public boolean isOperator(TokenType type) {
        switch (this) {
            case BANG:
            case BANG_EQUAL:
            case EQUAL:
            case EQUAL_EQUAL:
            case GREATER:
            case GREATER_EQUAL:
            case LESS:
            case LESS_EQUAL:
            case MINUS:
            case PLUS:
            case PLUS_PLUS:
            case STAR:
            case SLASH:
                return true;
            default:
                return false;
        }
    }

    public boolean isKeyword() {
        switch (this) {
            case CLASS:
            case THIS:
            case SUPER:
            case FN:
            case IF:
            case THEN:
            case ELSE:
            case FOR:
            case DO:
            case LOOP:
            case WHILE:
            case CASE:
            case OF:
            case LET:
            case IN:
            case VAR:
            case FALSE:
            case TRUE:
            case NIL:
            case AND:
            case OR:
            case PRINT:
            case RETURN:
                return true;
            default:
                return false;
        }
    }

    public boolean isTrue() {
        return this == TRUE;
    }

    public boolean isFalse() {
        return this == FALSE;
    }

    public boolean isNil() {
        return this == NIL;
    }

    public boolean isEOF() {
        return this == EOF;
    }

    public boolean beginsExpr() {
        switch (this) {
            case PAREN_L:
            case IDENT:
            case STRING:
            case NUMBER:
            case TRUE:
            case FALSE:
            case NIL:
            case DO:
            case LET:
            case CASE:
            case IF:
            case BANG:
            case THIS:
            case SUPER:
            case MINUS:
                return true;
            default:
                return false;
        }
    }

    public boolean beginsDecl() {
        switch (this) {
            case CLASS:
            case FN:
            case VAR:
            case FOR:
            case LOOP:
            case WHILE:
            case PRINT:
            case RETURN:
                return true;
            default:
                return false;
        }
    }

    static TokenType[] reserved() {
        return new TokenType[] { CLASS, THIS, SUPER,
                FN, IF, THEN, ELSE,
                DO, LOOP,
                FOR, WHILE, CASE, OF,
                LET, IN, VAR,
                FALSE, TRUE, NIL,
                AND, OR,
                PRINT,
                RETURN };
    }

    // so that we don't have to manually insert every keyword
    public static Map<String, TokenType> keywords() {
        TokenType[] kws = reserved();
        Map<String, TokenType> map = new HashMap<>();
        for (TokenType kw : kws) {
            String s = kw.toString().toLowerCase();
            map.put(s, kw);
        }
        return map;
    }
}
