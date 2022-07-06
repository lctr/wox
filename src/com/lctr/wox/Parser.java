package com.lctr.wox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Parser {
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private boolean isDone() {
        return peek().isEOF();
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token advance() {
        if (!isDone())
            current++;
        return previous();
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type) {
        if (isDone())
            return false;
        return peek().type == type;
    }

    private Token eat(TokenType type) {
        return eat(type, "expected `" + type + "`, but found `" + peek().text + "` instead!");
    }

    private Token eat(TokenType type, String message) {
        if (check(type))
            return advance();

        throw error(peek(), message);
    }

    private Token ignore(TokenType type) {
        if (check(type))
            return advance();
        return peek();
    }

    private <X> List<X> delimited(TokenType start, TokenType sep, TokenType end, Function<Parser, X> f) {
        List<X> nodes = new ArrayList<>();
        eat(start);
        boolean first = true;
        while (!isDone()) {
            if (match(end)) {
                break;
            }
            if (first) {
                first = false;
            } else {
                eat(sep);
            }
            if (match(end)) {
                break;
            }
            nodes.add(f.apply(this));
        }
        eat(end);
        return nodes;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isDone()) {
            statements.add(declaration());
        }
        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(TokenType.CLASS))
                return classDecl();
            if (match(TokenType.FN))
                return function("function");
            if (match(TokenType.VAR))
                return varDecl();
            return statement();
        } catch (ParseError error) {
            sync();
            return null;
        }
    }

    private Stmt classDecl() {
        Token name = eat(TokenType.IDENT, "Expected class name after `class` keyword.");

        Expr.Variable parent = null;
        if (match(TokenType.LESS)) {
            eat(TokenType.IDENT, "Expected superclass name after subclass operator `<`.");
            parent = new Expr.Variable(previous());
        }

        eat(TokenType.CURLY_L, "Expected `{` after class header and before class body.");
        List<Stmt.Function> methods = new ArrayList<>();
        while (!check(TokenType.CURLY_R) && !isDone()) {
            methods.add(function("method"));
        }

        eat(TokenType.CURLY_R, "Unbalanced braces! Expected `}` after class body");

        return new Stmt.Class(name, parent, methods);
    }

    private Stmt varDecl() {
        Token name = eat(TokenType.IDENT, "Expected variable identifier, but found `" + peek().text + "` instead");
        Expr initializer = null;

        if (match(TokenType.EQUAL))
            initializer = expression();
        eat(TokenType.SEMICOLON);
        return new Stmt.Var(name, initializer);
    }

    private Stmt.Function function(String kind) {
        Token name = eat(TokenType.IDENT, "Expected " + kind + " name.");
        eat(TokenType.PAREN_L, "Expected `(` after " + kind + " name.");
        List<Token> params = new ArrayList<>();
        if (!check(TokenType.PAREN_R)) {
            do {
                if (params.size() >= 255) {
                    error(peek(), "maximum function parameter size exceeded! Cannot have more than 255 parameters");
                }

                params.add(eat(TokenType.IDENT, "Expected parameter name within function definition."));
            } while (match(TokenType.COMMA));
        }
        eat(TokenType.PAREN_R, "Unbalanced parentheses! Expected `)` after parameters in function definition.");

        eat(TokenType.CURLY_L, "Expected '{' before " + kind + " body.");
        List<Stmt> body = block();
        return new Stmt.Function(name, params, body);
    }

    private Stmt statement() {
        if (match(TokenType.FOR))
            return forStmt();
        if (match(TokenType.PRINT))
            return printStmt();
        if (match(TokenType.RETURN))
            return retStmt();
        if (match(TokenType.WHILE))
            return whileStmt();
        if (match(TokenType.LOOP))
            return loopStmt();
        if (match(TokenType.CURLY_L))
            return new Stmt.Block(block());
        return exprStmt();
    }

    private Stmt loopStmt() {
        return null;
    }

    private Stmt forStmt() {
        return null;
    }

    private Stmt printStmt() {
        return null;
    }

    private Stmt retStmt() {
        return null;
    }

    private Stmt whileStmt() {
        return null;
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(TokenType.CURLY_R) && !isDone()) {
            statements.add(declaration());
        }

        eat(TokenType.CURLY_R, "Expect '}' after block.");
        return statements;
    }

    private Stmt exprStmt() {
        Expr expr = expression();
        ignore(TokenType.SEMICOLON);
        return new Stmt.Expression(expr);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();

        if (match(TokenType.EQUAL)) {
            Token eq = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.Get) {
                Expr.Get get = (Expr.Get) expr;
                return new Expr.Set(get.object, get.name, value);
            }

            error(eq, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while (match(TokenType.OR)) {
            Token op = previous();
            Expr right = and();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while (match(TokenType.AND)) {
            Token op = previous();
            Expr right = equality();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr equality() {
        Expr left = comparison();

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            left = new Expr.Binary(left, operator, right);
        }

        return left;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(TokenType.STAR, TokenType.SLASH)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return call();
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(TokenType.PAREN_R)) {
            do {
                if (arguments.size() >= 255) {
                    error(peek(), "Max argument size exceeded! Cannot have more than 255 arguments.");
                }
                arguments.add(expression());
            } while (match(TokenType.COMMA));
        }
        eat(TokenType.PAREN_R, "expected `)` after function call arguments.");
        return new Expr.Call(callee, arguments);
    }

    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(TokenType.PAREN_L)) {
                expr = finishCall(expr);
            } else if (match(TokenType.DOT)) {
                Token name = eat(TokenType.IDENT, "expected property name after `.`.");
                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr primary() {
        if (match(TokenType.FALSE))
            return new Expr.Literal(false);
        if (match(TokenType.TRUE))
            return new Expr.Literal(true);
        if (match(TokenType.NIL))
            return new Expr.Literal(null);

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(TokenType.LET)) {
            return let();
        }

        if (match(TokenType.DO))
            return doExpr();

        if (match(TokenType.SUPER)) {
            Token kw = previous();
            eat(TokenType.DOT, "Expected `.` after `super` keyword.");
            Token method = eat(TokenType.IDENT, "Expected identifier for superclass method name.");
            return new Expr.Super(kw, method);
        }

        if (match(TokenType.THIS))
            return new Expr.This(previous());

        if (match(TokenType.IDENT))
            return new Expr.Variable(previous());

        if (match(TokenType.PAREN_L)) {
            Expr expr = expression();
            if (match(TokenType.COMMA)) {
                return tupleTail(expr);
            } else {
                eat(TokenType.PAREN_R, "unmatched parentheses! Expected ')' after expression");
                return new Expr.Grouping(expr);
            }
        }

        throw error(peek(), "Expected expression");
    }

    private Expr doExpr() {
        List<Expr> exprs = delimited(TokenType.CURLY_L, TokenType.SEMICOLON, TokenType.CURLY_R, (t) -> t.expression());
        return new Expr.Do(exprs);
    }

    private Expr tupleTail(Expr head) {
        List<Expr> parts = new ArrayList<>();
        parts.add(head);
        if (!check(TokenType.PAREN_R)) {
            do {
                if (parts.size() >= 255) {
                    error(peek(), "Max tuple size exceeded! Tuples may only have up to 255 elements");
                }
                parts.add(expression());
            } while (match(TokenType.COMMA));
        }
        eat(TokenType.PAREN_R, "unbalanced parentheses! Expected `)` after tuple but found `" + peek().text + "`");
        return new Expr.Tuple(parts);
    }

    private Expr let() {
        Token varname = advance();
        eat(TokenType.EQUAL, "expected `=` after let-expression identifier!");
        Expr definition = expression();
        eat(TokenType.IN, "expected keyword `in` after let-expression definition and before scoped body!");
        Expr body = expression();
        return new Expr.Let(varname, definition, body);
    }

    private ParseError error(Token token, String message) {
        Wox.error(token, message);
        return new ParseError();
    }

    private void sync() {
        advance();

        while (!isDone()) {
            if (previous().type == TokenType.SEMICOLON)
                return;

            if (peek().type.beginsDecl())
                return;

            advance();
        }
    }
}
