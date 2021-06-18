package server;

import com.sun.net.httpserver.HttpExchange;

@FunctionalInterface
public interface LightHandler {
    LightResponse handle(HttpExchange exchange);
}
