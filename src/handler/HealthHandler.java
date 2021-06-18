package handler;

import com.sun.net.httpserver.HttpExchange;
import server.LightHandler;
import server.LightResponse;

public record HealthHandler() implements LightHandler {
	@Override
	public LightResponse get(HttpExchange exchange) {
	    return LightResponse.of(200, "OK!%n");
	}
}
