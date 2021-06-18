package server;

import com.sun.net.httpserver.HttpExchange;

public interface LightHandler {
    default LightResponse get(HttpExchange exchange) {
        throw LightException.methodNotAllowed("GET");
    }

    default LightResponse post(HttpExchange exchange) {
        throw LightException.methodNotAllowed("POST");
    }

    default LightResponse head(HttpExchange exchange) {
        throw LightException.methodNotAllowed("HEAD");
    }

    default LightResponse patch(HttpExchange exchange) {
        throw LightException.methodNotAllowed("PATCH");
    }

    default LightResponse delete(HttpExchange exchange) {
        throw LightException.methodNotAllowed("DELETE");
    }
}
