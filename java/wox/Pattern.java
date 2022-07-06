package wox;

abstract class Pattern {
    interface Visitor<R> {
        R visitUnitPattern(Unit pat);

        R visitVarPattern(Var var);

        R visitLitPattern(Lit lit);

        R visitWildPattern(Wild wild);

        R visitTuplePattern(Tuple tuple);

        R visitVectorPattern(Vector vector);

        R visitAtPattern(At at);
    }

    static class Unit extends Pattern {
        Unit() {
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnitPattern(this);
        }
    }

    static class Var extends Pattern {
        final Token name;

        Var(Token name) {
            this.name = name;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarPattern(this);
        }
    }

    static class Lit extends Pattern {
        final Token value;

        Lit(Token value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLitPattern(this);
        }
    }

    static class Wild extends Pattern {
        Wild() {
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitWildPattern(this);
        }
    }

    static class At extends Pattern {
        final Token name;
        final Pattern pattern;

        At(Token name, Pattern pattern) {
            this.name = name;
            this.pattern = pattern;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitAtPattern(this);
        }
    }

    static class Tuple extends Pattern {
        final Pattern[] items;

        Tuple(Pattern[] items) {
            this.items = items;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitTuplePattern(this);
        }
    }

    static class Vector extends Pattern {
        final Pattern[] elements;

        Vector() {
            this.elements = new Pattern[] {};
        }

        Vector(Pattern[] elements) {
            this.elements = elements;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVectorPattern(this);
        }
    }

    abstract <R> R accept(Visitor<R> visitor);
}
