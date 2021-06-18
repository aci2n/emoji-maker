package server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public record LightServer(HttpServer httpServer, Executor executor) {
	public static LightServer create(int port, int threadPoolSize) throws IOException {
		InetSocketAddress address = new InetSocketAddress("0.0.0.0", port);
		return new LightServer(HttpServer.create(address, 0), Executors.newFixedThreadPool(threadPoolSize));
	}

	public void addHandler(String path, LightHandler handler) {
		httpServer.createContext(path, exchange -> {
			try (exchange) {
				sendResponse(exchange, handle(exchange, handler));
			}
		});
	}

	public void start() {
		httpServer.setExecutor(executor);
		httpServer.start();
	}

	public void stop(int delay) {
		httpServer.stop(delay);
	}

	private LightResponse handle(HttpExchange exchange, LightHandler handler) {
		try {
			return handler.handle(exchange);
		} catch (LightException e) {
			return LightResponse.of(e.getStatusCode(), e);
		} catch (Throwable t) {
			return LightResponse.of(500, t);
		}
	}

	private void sendResponse(HttpExchange exchange, LightResponse response) throws IOException {
		Headers headers = exchange.getResponseHeaders();
		headers.add("Server", "LightServer");
		response.headers().forEach(headers::add);
		exchange.sendResponseHeaders(response.statusCode(), response.contentLength());
		response.result().transferTo(exchange.getResponseBody());
	}
}
