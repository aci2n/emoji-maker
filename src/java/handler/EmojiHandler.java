package handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import server.LightException;
import server.LightHandler;
import server.LightResponse;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Logger;

public record EmojiHandler() implements LightHandler {
    private static final Logger LOG = Logger.getLogger(EmojiHandler.class.getName());

    @Override
    public LightResponse post(HttpExchange exchange) {
        try {
            File tgs = writeTgs(exchange.getRequestBody());
            Headers headers = exchange.getRequestHeaders();
            int size = getIntHeader(headers, "GIF-Size", 96, 128);
            int fps = getIntHeader(headers, "GIF-FPS", 24, 30);
            int quality = getIntHeader(headers, "GIF-Quality", 60, 100);
            File gif = convertToGifCapped(tgs, size, fps, quality);
            return LightResponse.builder()
                    .statusCode(200)
                    .contentLength(gif.length())
                    .result(new FileInputStream(gif))
                    .header("Content-Type", "image/gif")
                    .build();
        } catch (IOException e) {
            throw LightException.internalServerError(e);
        }
    }

    private int getIntHeader(Headers headers, String key, int fallback, int max) {
        try {
            return Math.min(max, Integer.parseInt(headers.getFirst(key)));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private File writeTgs(InputStream input) throws IOException {
        File tgs = Files.createTempFile("sticker", ".tgs").toFile();
        tgs.deleteOnExit();
        input.transferTo(new FileOutputStream(tgs));
        LOG.info(() -> String.format("wrote tgs: %s (%d bytes)", tgs, tgs.length()));
        if (tgs.length() < 10) {
            throw LightException.badRequest("looks like a very small sticker, ignoring");
        }
        return tgs;
    }

    private File convertToGifCapped(File tgs, int size, int fps, int quality) throws IOException {
        while (fps >= 10) {
            File gif = convertToGif(tgs, size, fps, quality);
            if (gif.length() > 256 * 1024) {
                fps -= 2;
            } else {
                return gif;
            }
        }
        throw LightException.badRequest("animation too long, cannot render");
    }

    private File convertToGif(File tgs, int size, int fps, int quality) throws IOException {
        String[] tgsToGifCmd = new String[]{
                "node",
                "deps/renderer/cli.js",
                "--width", Integer.toString(size),
                "--height", Integer.toString(size),
                "--fps", Integer.toString(fps),
                "--quality", Integer.toString(quality),
                tgs.toString()};
        exec(tgsToGifCmd);
        File gif = Path.of(tgs.getPath() + ".gif").toFile();
        if (gif.length() < 10) {
            throw LightException.badRequest(String.format("could not convert to gif %s", gif));
        }
        gif.deleteOnExit();
        LOG.info(() -> String.format("converted to gif: %s (%d bytes)", gif, gif.length()));
        return gif;
    }

    private void exec(String[] cmd) throws IOException {
        LOG.info(() -> String.format("running cmd %s", Arrays.toString(cmd)));
        Process proc = Runtime.getRuntime().exec(cmd);
        try {
            int result = proc.waitFor();
            String stdout = new String(proc.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String stderr = new String(proc.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            LOG.info(() -> String.format("[stdout:%s,stderr:%s]", stdout, stderr));
            if (result != 0) {
                throw LightException.internalServerError(stderr);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw LightException.internalServerError(e);
        }
    }
}
