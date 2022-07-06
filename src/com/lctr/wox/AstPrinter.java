package com.lctr.wox;

import java.util.List;

public class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {
    String print(Expr expr) {
        return expr.accept(this);
    }

    String print(Stmt stmt) {
        return stmt.accept(this);
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    private String parenthesize(String name, Token token, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name).append(" (pat ").append(token.text).append(")");

        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    private String parenthesize(String name, Expr head, List<Expr> list) {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(name).append(" ").append(head.accept(this));
        for (Expr expr : list) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");
        return builder.toString();
    }

    private String parenthesize(String name, List<Expr> list) {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(name);
        for (Expr expr : list) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");
        return builder.toString();
    }

    private String parenthesized(String name, Object... objects) {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(name);
        transform(builder, objects);
        builder.append(")");
        return builder.toString();
    }

    private void transform(StringBuilder builder, Object... parts) {
        for (Object part : parts) {
            builder.append(" ");
            if (part instanceof Expr) {
                builder.append(((Expr) part).accept(this));
            } else if (part instanceof Stmt) {
                builder.append(((Stmt) part).accept(this));
            } else if (part instanceof Token) {
                builder.append(((Token) part).text);
            } else if (part instanceof List) {
                transform(builder, ((List) part).toArray());
            } else {
                builder.append(part);
            }
        }
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.text, expr.left, expr.right);
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.text, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("grouping", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null)
            return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitLetExpr(Expr.Let expr) {
        return parenthesize("let", expr.variable, expr.definition, expr.body);
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return parenthesize("assign", expr.name, expr.value);
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        return parenthesize("call", expr.callee, expr.arguments);
    }

    @Override
    public String visitGetExpr(Expr.Get expr) {
        return new StringBuilder()
                .append("get")
                .append(" ")
                .append(expr.object.accept(this))
                .append(" ")
                .append(expr.name).toString();
    }

    @Override
    public String visitIfExpr(Expr.If expr) {
        return parenthesize("if", expr.condition, expr.when_true, expr.when_false);
    }

    @Override
    public String visitSetExpr(Expr.Set expr) {
        return parenthesize("set", expr.name, expr.object, expr.value);
    }

    @Override
    public String visitSuperExpr(Expr.Super expr) {
        return new StringBuilder()
                .append("(")
                .append("super ")
                .append(expr.method)
                .append(")").toString();
    }

    @Override
    public String visitThisExpr(Expr.This expr) {
        return "this";
    }

    @Override
    public String visitTupleExpr(Expr.Tuple tuple) {
        return parenthesize("tuple", tuple.elements);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return parenthesize("variable", expr.name);
    }

    @Override
    public String visitVectorExpr(Expr.Vector expr) {
        return parenthesize("vector", expr.elements);
    }

    @Override
    public String visitDoExpr(Expr.Do expr) {
        return parenthesize("do", expr.body);
    }

    public static void main(String[] args) {
        // (-123)*(45.67)
        Expr expression = new Expr.Binary(
                new Expr.Unary(
                        new Token(TokenType.MINUS, "-", null, 1, 1),
                        new Expr.Literal(123)),
                new Token(TokenType.STAR, "*", null, 1, 5),
                new Expr.Grouping(
                        new Expr.Literal(45.67)));

        System.out.println(new AstPrinter().print(expression));
    }

    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("(block ");

        for (Stmt statement : stmt.statements) {
            builder.append(statement.accept(this));
        }

        builder.append(")");
        return builder.toString();
    }

    @Override
    public String visitClassStmt(Stmt.Class stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("(class " + stmt.name.text);
        // > Inheritance omit

        if (stmt.superclass != null) {
            builder.append(" < " + print(stmt.superclass));
        }
        // < Inheritance omit

        for (Stmt.Function method : stmt.methods) {
            builder.append(" " + print(method));
        }

        builder.append(")");
        return builder.toString();
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression stmt) {
        return parenthesize(";", stmt.expression);
    }

    @Override
    public String visitFunctionStmt(Stmt.Function stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("(fun " + stmt.name.text + "(");

        for (Token param : stmt.params) {
            if (param != stmt.params.get(0))
                builder.append(" ");
            builder.append(param.text);
        }

        builder.append(") ");

        for (Stmt body : stmt.body) {
            builder.append(body.accept(this));
        }

        builder.append(")");
        return builder.toString();
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt) {
        return parenthesize("print", stmt.expression);
    }

    @Override
    public String visitReturnStmt(Stmt.Return stmt) {
        if (stmt.value == null)
            return "(return)";
        return parenthesize("return", stmt.value);
    }

    @Override
    public String visitVarStmt(Stmt.Var stmt) {
        if (stmt.initializer == null) {
            return parenthesized("var", stmt.name);
        }

        return parenthesized("var", stmt.name, "=", stmt.initializer);
    }

    @Override
    public String visitLoopStmt(Stmt.Loop stmt) {
        return parenthesized("loop", stmt.statements);
    }

    @Override
    public String visitWhileStmt(Stmt.While stmt) {
        return parenthesized("while", stmt.condition, stmt.body);
    }
}
