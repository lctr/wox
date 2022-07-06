package wox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static wox.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    Parser(String source) {
        this.tokens = new Lexer(source).lex();
    }

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
        return eat(type, "expected `" + type.stringify() + "`.");
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
            if (check(end)) {
                break;
            }
            if (first) {
                first = false;
            } else {
                eat(sep);
            }
            if (check(end)) {
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
            if (match(CLASS))
                return classDecl();
            if (match(FN))
                return function("function");
            if (match(VAR))
                return varDecl();
            return statement();
        } catch (ParseError error) {
            sync();
            return null;
        }
    }

    private Stmt classDecl() {
        Token name = eat(IDENT, "Expected class name after `class` keyword.");

        Expr.Variable parent = null;
        if (match(LESS)) {
            eat(IDENT, "Expected superclass name after subclass operator `<`.");
            parent = new Expr.Variable(previous());
        }

        eat(CURLY_L, "Expected `{` after class header and before class body.");
        List<Stmt.Function> methods = new ArrayList<>();
        while (!check(CURLY_R) && !isDone()) {
            methods.add(function("method"));
        }

        eat(CURLY_R, "Unbalanced braces! Expected `}` after class body");

        return new Stmt.Class(name, parent, methods);
    }

    private Stmt varDecl() {
        Token name = eat(IDENT, "Expected variable identifier, but found `" + peek().text + "` instead");
        Expr initializer = null;

        if (match(EQUAL))
            initializer = expression();
        eat(SEMICOLON);
        return new Stmt.Var(name, initializer);
    }

    private Stmt.Function function(String kind) {
        Token name = eat(IDENT, "Expected " + kind + " name.");
        eat(PAREN_L, "Expected `(` after " + kind + " name.");
        List<Token> params = new ArrayList<>();
        if (!check(PAREN_R)) {
            do {
                if (params.size() >= 255) {
                    error(peek(), "maximum function parameter size exceeded! Cannot have more than 255 parameters");
                }

                params.add(eat(IDENT, "Expected parameter name within function definition."));
            } while (match(COMMA));
        }
        eat(PAREN_R, "Unbalanced parentheses! Expected `)` after parameters in function definition.");

        eat(CURLY_L, "Expected '{' before " + kind + " body.");
        List<Stmt> body = block();
        return new Stmt.Function(name, params, body);
    }

    private Stmt statement() {
        if (match(FOR))
            return forStmt();
        if (match(PRINT))
            return printStmt();
        if (match(RETURN))
            return retStmt();
        if (match(WHILE))
            return whileStmt();
        if (match(LOOP))
            return loopStmt();
        if (match(CURLY_L))
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

        while (!check(CURLY_R) && !isDone()) {
            statements.add(declaration());
        }

        eat(CURLY_R, "Expect '}' after block.");
        return statements;
    }

    private Stmt exprStmt() {
        Expr expr = expression();
        ignore(SEMICOLON);
        return new Stmt.Expression(expr);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();

        if (match(EQUAL)) {
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

        while (match(OR)) {
            Token op = previous();
            Expr right = and();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while (match(AND)) {
            Token op = previous();
            Expr right = equality();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr equality() {
        Expr left = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            left = new Expr.Binary(left, operator, right);
        }

        return left;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(STAR, SLASH)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return call();
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(PAREN_R)) {
            do {
                if (arguments.size() >= 255) {
                    error(peek(), "Max argument size exceeded! Cannot have more than 255 arguments.");
                }
                arguments.add(expression());
            } while (match(COMMA));
        }
        eat(PAREN_R, "expected `)` after function call arguments.");
        return new Expr.Call(callee, arguments);
    }

    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(PAREN_L)) {
                expr = finishCall(expr);
            } else if (match(DOT)) {
                Token name = eat(IDENT, "expected property name after `.`.");
                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr primary() {
        if (match(FALSE))
            return new Expr.Literal(false);
        if (match(TRUE))
            return new Expr.Literal(true);
        if (match(NIL))
            return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(LET)) {
            return let();
        }

        if (match(DO)) {
            return doExpr();
        }

        if (match(SUPER)) {
            Token kw = previous();
            eat(DOT, "Expected `.` after `super` keyword.");
            Token method = eat(IDENT, "Expected identifier for superclass method name.");
            return new Expr.Super(kw, method);
        }

        if (match(THIS))
            return new Expr.This(previous());

        if (match(IDENT))
            return new Expr.Variable(previous());

        if (check(BRACK_L)) {
            return new Expr.Vector(delimited(BRACK_L, COMMA, BRACK_R, Parser::expression));
        }

        if (match(PAREN_L)) {
            Expr expr = expression();
            if (match(COMMA)) {
                return tupleTail(expr);
            } else {
                eat(PAREN_R, "unmatched parentheses! Expected ')' after expression");
                return new Expr.Grouping(expr);
            }
        }

        throw error(peek(), "Expected expression");
    }

    private Expr doExpr() {
        List<Expr> exprs = delimited(
                CURLY_L,
                SEMICOLON,
                CURLY_R,
                (p) -> {
                    return p.expression();
                });
        return new Expr.Do(exprs);
    }

    private Expr tupleTail(Expr head) {
        List<Expr> parts = new ArrayList<>();
        parts.add(head);
        if (!check(PAREN_R)) {
            do {
                if (parts.size() >= 255) {
                    error(peek(), "Max tuple size exceeded! Tuples may only have up to 255 elements");
                }
                parts.add(expression());
            } while (match(COMMA));
        }
        eat(PAREN_R,
                "unbalanced parentheses! Expected `)` after tuple but found `" + peek().text + "` instead.");
        return new Expr.Tuple(parts);
    }

    private Expr let() {
        Token varname = advance();
        eat(EQUAL, "expected `=` after let-expression identifier!");
        Expr definition = expression();
        eat(IN, "expected keyword `in` after let-expression definition and before scoped body!");
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
            if (previous().type == SEMICOLON)
                return;

            if (peek().type.beginsDecl())
                return;

            advance();
        }
    }
}
