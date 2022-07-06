package com.lctr.tools;

import java.io.IOException;
import java.io.PrintWriter;
// import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AstGenerator {
    static final String pkg = "com.lctr.wox";

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];
        // expressions
        defineAst(outputDir, "Expr", Arrays.asList(
                "Assign : Token name, Expr value",
                "Binary : Expr left, Token operator, Expr right",
                "Call : Expr callee, List<Expr> arguments",
                // like a block, but returns the value of the last expr
                "Do : List<Expr> body",
                "Get : Expr object, Token name",
                "Grouping : Expr expression",
                // conditionals are expressions instead of statements!
                "If : Expr condition, Expr when_true, Expr when_false",
                "Let : Token variable, Expr definition, Expr body",
                "Literal : Object value",
                "Set : Expr object, Token name, Expr value",
                "Super : Token keyword, Token method",
                "This : Token keyword",
                "Tuple : List<Expr> elements",
                "Unary : Token operator, Expr right",
                "Variable: Token name",
                "Vector : List<Expr> elements"));
        // statements
        defineAst(outputDir, "Stmt", Arrays.asList(
                "Block : List<Stmt> statements",
                "Class : Token name, Expr.Variable superclass,"
                        + " List<Stmt.Function> methods",
                "Expression : Expr expression",
                "Function : Token name, List<Token> params," + " List<Stmt> body",
                "Loop : List<Stmt> statements",
                "Print : Expr expression",
                "Return : Token keyword, Expr value",
                "Var : Token name, Expr initializer",
                "While : Expr condition, Stmt body"));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package " + pkg + ";");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        for (String type : types) {
            String[] parts = type.split(":");
            String className = parts[0].trim();
            String fields = parts[1].trim();
            defineType(writer, baseName, className, fields);
            writer.println();
        }

        writer.println();
        writer.println(spaces(2) + "abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        // array for strings of empty spaces for indentation
        String[] spaces = { spaces(2), spaces(4), spaces(6) };

        writer.println(spaces[0] + "static class " + className + " extends " + baseName + " {");

        // constructor
        writer.println(spaces[1] + className + "(" + fieldList + ") {");

        // parameters stored in fields
        for (String field : fieldList.split(", ")) {
            String name = field.split(" ")[1];
            writer.println(spaces[2] + "this." + name + " = " + name + ";");
        }
        writer.println(spaces[1] + "}");

        // Visitor pattern
        writer.println();
        writer.println(spaces[1] + "@Override");
        writer.println(spaces[1] + "<R> R accept(Visitor<R> visitor) {");
        writer.println(spaces[2] + "return visitor.visit" + className + baseName + "(this);");
        writer.println(spaces[1] + "}");

        // fields
        writer.println();
        for (String field : fieldList.split(", ")) {
            writer.println(spaces[1] + "final " + field + ";");
        }

        writer.println(spaces[0] + "}");
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println(spaces(2) + "interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println(spaces(4) + "R visit" + typeName + baseName + "(" + typeName + spaces(1)
                    + baseName.toLowerCase() + ");");
        }

        writer.println(spaces(2) + "}");
    }

    private static String spaces(int width) {
        String buf = new String();
        for (int i = 0; i < width; i++) {
            buf += " ";
        }
        return buf;
    }
}
