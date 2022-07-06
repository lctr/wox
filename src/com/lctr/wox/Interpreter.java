package com.lctr.wox;

public class Interpreter implements Expr.Visitor<Object> {

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;
        return true;
    }

    private boolean isEqual(Object left, Object right) {
        // preserve IEEE 754 NaN inequality for doubles; `==` preserves this but
        // `equals` does not!!!
        if (left instanceof Double && right instanceof Double)
            return left == right;
        if (left == null && right == null)
            return true;
        if (left == null)
            return false;
        return left.equals(right);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = expr.right;

        switch (expr.operator.type) {
            case MINUS:
                return -(double) right;
            case BANG:
                return !isTruthy(right);
            // unreachable
            default:
                return null;
        }
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);

            case GREATER:
                return (double) left > (double) right;
            case GREATER_EQUAL:
                return (double) left >= (double) right;
            case LESS:
                return (double) left < (double) right;
            case LESS_EQUAL:
                return (double) left <= (double) right;

            case MINUS:
                return (double) left - (double) right;
            case SLASH:
                return (double) left / (double) right;
            case STAR:
                return (double) left * (double) right;
            case PLUS:
                return (double) left + (double) right;
            case PLUS_PLUS:
                if (left instanceof String && right instanceof String)
                    return (String) left + (String) right;

                // Unreachable
            default:
                return null;
        }

    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        return null;
    }

    @Override
    public Object visitDoExpr(Expr.Do expr) {
        return null;
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        return null;
    }

    @Override
    public Object visitIfExpr(Expr.If expr) {
        return null;
    }

    @Override
    public Object visitLetExpr(Expr.Let expr) {
        return null;
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        return null;
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {
        return null;
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        return null;
    }

    @Override
    public Object visitTupleExpr(Expr.Tuple expr) {
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return null;
    }

    @Override
    public Object visitVectorExpr(Expr.Vector expr) {
        return null;
    }
}
