package server;

import handler.EmojiHandler;
import handler.HealthHandler;

import java.util.logging.Logger;

public record Launcher() {
	private static final Logger LOG = Logger.getLogger(Launcher.class.getName());

	public static void main(String[] args) {
		try {
			LOG.info("initializing");
			LightServer server = LightServer.create(14000);

			server.addHandler("/health", new HealthHandler());
			server.addHandler("/emoji", new EmojiHandler());

			server.start();
			Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

			// this thread terminates here, HttpServer non-daemon thread keeps the process alive
		} catch (Throwable e) {
			LOG.severe(e.getMessage());
			System.exit(1);
		}
	}
}