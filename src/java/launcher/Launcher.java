package launcher;

import handler.EmojiHandler;
import handler.HealthHandler;
import handler.UploadHandler;
import server.LightServer;

import java.util.logging.Logger;

public record Launcher() {
	private static final Logger LOG = Logger.getLogger(Launcher.class.getName());

	public static void main(String[] args) {
		try {
			LOG.info("initializing");
			LightServer server = LightServer.create(
					args.length >= 1 ? Integer.parseInt(args[0]) : 14000,
					Runtime.getRuntime().availableProcessors());

			server.addHandler("/health", new HealthHandler());
			server.addHandler("/emoji", new EmojiHandler());
			server.addHandler("/upload", new UploadHandler());

			server.start();
			Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(1)));

			// this thread terminates here, HttpServer non-daemon thread keeps the process alive
		} catch (Throwable e) {
			LOG.severe(e.getMessage());
			System.exit(1);
		}
	}
}