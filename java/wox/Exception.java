package wox;

public class Exception extends RuntimeException {
    final Token token;

    Exception(Token token, String message) {
        super(message);
        this.token = token;
    }
}
