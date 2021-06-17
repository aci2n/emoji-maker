package server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public record LightServer(HttpServer httpServer) {
	private static final Logger LOG = Logger.getLogger(LightServer.class.getName());
	private static final int SHUTDOWN_DELAY_SECONDS = 60;
	private static final int THREAD_POOL_SIZE = 10;

	public static LightServer create(int port) throws IOException {
		InetSocketAddress address = new InetSocketAddress("127.0.0.1", port);
		return new LightServer(HttpServer.create(address, 0));
	}

	public void addHandler(String path, HttpHandler handler) {
		httpServer.createContext(path, new HandlerWrapper(handler));
	}

	public void start() {
		httpServer.setExecutor(Executors.newFixedThreadPool(THREAD_POOL_SIZE));
		httpServer.start();
	}

	public void stop() {
		httpServer.stop(SHUTDOWN_DELAY_SECONDS);
	}

	private record HandlerWrapper(HttpHandler handler) implements HttpHandler {
		public void handle(HttpExchange exchange) throws IOException {
			try {
				LOG.fine(() -> {
					HttpContext context = exchange.getHttpContext();
					return String.format(
							"got request: [path=%s,remote_host:%s]",
							context.getPath(),
							exchange.getRemoteAddress().getHostString());
				});
				Headers headers = exchange.getResponseHeaders();
				headers.add("Server", "LightServer");
				headers.add("Content-Type", "text/plain;charset=UTF-8");
				handler.handle(exchange);
			} catch (Throwable t) {
			    LOG.severe(() -> {
					HttpContext context = exchange.getHttpContext();
					return String.format(
							"error: %s [path=%s,remote_host:%s]",
							t.getMessage(),
							context.getPath(),
							exchange.getRemoteAddress().getHostString());
				});
			    byte[] message = t.getMessage().getBytes(StandardCharsets.UTF_8);
			    exchange.sendResponseHeaders(500, message.length);
			    exchange.getResponseBody().write(message);
			    throw t;
			} finally {
				exchange.close();
			}
		}
	}
}
