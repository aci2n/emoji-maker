package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public record HealthHandler() implements HttpHandler {
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		byte[] response = String.format("OK!%n").getBytes(StandardCharsets.UTF_8);
		exchange.sendResponseHeaders(200, response.length);
		exchange.getResponseBody().write(response);
	}
}
