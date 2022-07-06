package wox;

import java.util.ArrayList;
import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private Environment environment = new Environment();

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

    private void assertNumOperand(Token operator, Object operand) {
        if (operand instanceof Double)
            return;
        throw new Exception(operator, "Operand must be a number!");
    }

    private void assertNumOperands(Token operator, Object... operands) {
        for (Object operand : operands) {
            assertNumOperand(operator, operand);
        }
    }

    private void assertBoolOperand(Token operator, Object operand) {
        if (operand instanceof Boolean)
            return;
        throw new Exception(operator, "Operand must be a boolean!");
    }

    private String stringify(Object object) {
        if (object == null)
            return "()";

        if (object instanceof Double) {
            String text = object.toString();
            // remove decimals for otherwise-integer-like numbers
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    void interpret(List<Stmt> stmts) {
        try {
            for (Stmt stmt : stmts) {
                execute(stmt);
            }
        } catch (Exception error) {
            Wox.runtimeException(error);
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    ///////////// VISITING STATEMENTS

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        return null;
    }

    @Override
    public Void visitLoopStmt(Stmt.Loop stmt) {
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.text, value);
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        return null;
    }

    ///////////// VISITING EXPRESSIONS

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
        Token operator = expr.operator;

        switch (expr.operator.type) {
            case MINUS:
                assertNumOperand(operator, right);
                return -(double) right;
            case BANG:
                assertBoolOperand(operator, right);
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
                assertNumOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                assertNumOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                assertNumOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                assertNumOperands(expr.operator, left, right);
                return (double) left <= (double) right;

            case MINUS:
                assertNumOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case SLASH:
                assertNumOperands(expr.operator, left, right);
                return (double) left / (double) right;
            case STAR:
                assertNumOperands(expr.operator, left, right);
                return (double) left * (double) right;
            case PLUS:
                assertNumOperands(expr.operator, left, right);
                return (double) left + (double) right;
            case PLUS_PLUS:
                if (left instanceof String && right instanceof String)
                    return (String) left + (String) right;
                throw new Exception(expr.operator, "Append `++` only currently defined for strings!");

            // Unreachable
            default:
                return null;
        }

    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        return null;
    }

    @Override
    public Object visitDoExpr(Expr.Do expr) {
        Object value = null;
        Environment envr = new Environment(environment);
        Environment old = environment;
        try {
            this.environment = envr;
            for (Expr ex : expr.body) {
                value = evaluate(ex);
            }
        } finally {
            this.environment = old;
        }
        return value;
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        return null;
    }

    @Override
    public Object visitIfExpr(Expr.If expr) {
        if (isTruthy(evaluate(expr.condition))) {
            return evaluate(expr.when_true);
        }
        return evaluate(expr.when_false);
    }

    @Override
    public Object visitLetExpr(Expr.Let expr) {
        Environment curr = this.environment;
        Environment envr = environment.extend();
        Object value = null;
        try {
            this.environment = envr;
            envr.define(expr.variable.text, evaluate(expr.definition));
            value = evaluate(expr.body);
        } finally {
            this.environment = curr;
        }
        return value;
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
        List<Object> tuple = new ArrayList<>();
        for (Expr ex : expr.elements) {
            tuple.add(evaluate(ex));
        }
        return tuple;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    @Override
    public Object visitVectorExpr(Expr.Vector expr) {
        List<Object> vec = new ArrayList<>();
        for (Expr ex : expr.elements) {
            vec.add(evaluate(ex));
        }
        return vec;
    }
}
