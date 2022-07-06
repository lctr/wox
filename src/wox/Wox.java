package wox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Wox {
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jwox [script]");
            // exit using relevant error code from UNIX "sysexits.h" header
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        // exit using relevant error code from UNIX "sysexits.h" header
        if (hadError)
            System.exit(65);
        if (hadRuntimeError)
            System.exit(70);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null)
                break;
            if (line.startsWith(":Q") || line.startsWith(":q")) {
                System.out.println("Quitting...");
                System.exit(64);
            }
            run(line);
            // if the user makes a mistake, it shouldn't *kill* the interactive
            // loop
            hadError = false;
        }
    }

    private static void run(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.lex();

        Parser parser = new Parser(tokens);
        List<Stmt> stmts = parser.parse();

        if (hadError)
            return;

        interpreter.interpret(stmts);
    }

    static void error(int line, String msg) {
        report(line, "", msg);
    }

    static void error(int line, int column, String msg) {
        report(line, column, "", msg);
    }

    static void error(Token token, String message) {
        int line = token.line;
        int column = token.column;
        if (token.isEOF()) {
            report(line, column, " at end", message);
        } else {
            report(line, column, " at '" + token.text + "'", message);
        }
    }

    private static void report(int line, String where, String msg) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + msg);
        hadError = true;
    }

    private static void report(int line, int column, String where, String msg) {
        System.err.println("[line " + line + ", column " + column + "] Error"
                + where + ": " + msg);
        hadError = true;
    }

    static void runtimeException(Exception error) {
        System.err.println(error.getMessage()
                + "\n["
                + error.token.lnColString()
                + "]");
        hadRuntimeError = true;
    }

}