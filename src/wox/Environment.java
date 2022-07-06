package wox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    final Environment parent;
    private final Map<String, Object> bindings = new HashMap<>();

    Environment() {
        parent = null;
    }

    Environment(Environment parent) {
        this.parent = parent;
    }

    Object get(Token name) {
        Environment envr = this;
        do {
            if (envr.bindings.containsKey(name.text)) {
                return envr.bindings.get(name.text);
            }
            envr = envr.parent;
        } while (envr.parent != null);

        throw new Exception(name, unbound(name));
    }

    void define(String name, Object value) {
        bindings.put(name, value);
    }

    void assign(Token name, Object value) {
        Environment envr = this;
        do {
            if (envr.bindings.containsKey(name.text)) {
                envr.assign(name, value);
                return;
            }
            envr = envr.parent;
        } while (envr.parent != null);

        throw new Exception(name, "Unbound variable! The identifier '" + name.text + "' is not in scope.");
    }

    Environment extend() {
        Environment envr = new Environment(this);
        return envr;
    }

    private static String unbound(Token token) {
        return "Unbound variable! The identifier '" + token.text + "'defined on " + token.lnColString()
                + " is not in scope.";
    }
}
