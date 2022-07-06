package com.lctr.wox;

class Keyword {
    final String text;

    public Keyword(String text) {
        this.text = text;
    }

    public static boolean isKeyword(TokenType type) {
        return type.isKeyword();
    }

    public static boolean isKeyword(Token token) {
        return token.type.isKeyword();
    }
}

public class Token {
    final TokenType type;
    final String text;
    final Object literal;
    final int line;
    final int column;

    Token(TokenType type, String text, Object literal, int line, int column) {
        this.type = type;
        this.text = text;
        this.literal = literal;
        this.line = line;
        this.column = column;
    }

    public boolean isEOF() {
        return this.type.isEOF();
    }

    public String toString() {
        String buf = new String("Token(");
        buf += type;
        if (type == TokenType.EOF) {
            buf += ")";
            return buf;
        } else {
            buf += " ";
        }
        buf += text;
        if (literal != null) {
            buf += " " + literal;
        }
        buf += " @ " + line + ":" + column + ")";
        return buf;
    }
}
