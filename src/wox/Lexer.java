package wox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// static imports are considered bad style by many. comment out if not wanting
// to preface token types with the enum name all over the lexer and parser...
// import static com.lctr.wox.TokenType.*

public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private static final Map<String, TokenType> keywords = TokenType.keywords();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int column = 0;

    Lexer(String source) {
        this.source = source;
    }

    List<Token> lex() {
        while (!isDone()) {
            start = current;
            nextToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line, column));
        return tokens;
    }

    private void nextToken() {
        char c = advance();
        switch (c) {
            case '(':
                addToken(TokenType.PAREN_L);
                break;
            case ')':
                addToken(TokenType.PAREN_R);
                break;
            case '{':
                addToken(TokenType.CURLY_L);
                break;
            case '}':
                addToken(TokenType.CURLY_R);
            case '[':
                addToken(TokenType.BRACK_L);
                break;
            case ']':
                addToken(TokenType.BRACK_R);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case '.':
                addToken(TokenType.DOT);
                break;
            case '-':
                addToken(TokenType.MINUS);
                break;
            case '+':
                addToken(match('+') ? TokenType.PLUS_PLUS : TokenType.PLUS);
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;
            case '*':
                addToken(TokenType.STAR);
                break;
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '/':
                if (match('/')) {
                    // line comment
                    while (peek() != '\n' && !isDone())
                        advance();
                } else {
                    addToken(TokenType.SLASH);
                }
                break;

            case ' ':
            case '\r':
            case '\t':
                column++;
                // ignoring whitespace
                break;

            case '\n':
                line++;
                column = 0;
                break;

            case '"':
                string();
                break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Wox.error(line, column, "Unexpected character.");
                }
                break;
        }
    }

    private boolean isDone() {
        return current >= source.length();
    }

    private char peek() {
        if (isDone())
            return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length())
            return '\0';
        return source.charAt(current + 1);
    }

    private char advance() {
        return source.charAt(current++);
    }

    private boolean match(char expected) {
        if (isDone())
            return false;
        if (source.charAt(current) != expected)
            return false;

        current++;
        return true;
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String txt = source.substring(start, current);
        tokens.add(new Token(type, txt, literal, line, column));
    }

    private void string() {
        while (peek() != '"' && !isDone()) {
            if (peek() == '\n') {
                column = 0;
                line++;
            }
            advance();
        }

        if (isDone()) {
            Wox.error(line, "Unterminated string.");
            return;
        }

        // the closing quotation mark
        advance();

        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void number() {
        while (isDigit(peek()))
            advance();

        // look for fractional part, if it exists
        if (peek() == '.' && isDigit(peekNext())) {
            // skip the `.`
            advance();

            while (isDigit(peek()))
                advance();
        }

        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c < 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void identifier() {
        while (isAlphaNumeric(peek()))
            advance();

        String txt = source.substring(start, current);
        TokenType type = keywords.get(txt);
        if (type == null)
            type = TokenType.IDENT;
        addToken(type);
    }

}
