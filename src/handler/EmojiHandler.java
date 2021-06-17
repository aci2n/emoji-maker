package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Logger;

public record EmojiHandler() implements HttpHandler {
    private static final Logger LOG = Logger.getLogger(EmojiHandler.class.getName());

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        File tgs = writeTgs(exchange.getRequestBody());
        File gif = convertToGif(tgs);
        File optimized = optimizeGif(gif);
        long contentLength = optimized.length();
        exchange.getResponseHeaders().set("Content-Type", "image/gif");
        exchange.sendResponseHeaders(200, contentLength);
        new FileInputStream(optimized).transferTo(exchange.getResponseBody());
    }

    private File writeTgs(InputStream input) throws IOException {
        File tgs = Files.createTempFile("sticker", ".tgs").toFile();
        tgs.deleteOnExit();
        input.transferTo(new FileOutputStream(tgs));
        LOG.info(() -> String.format("wrote tgs: %s (%d bytes)", tgs, tgs.length()));
        return tgs;
    }

    private File convertToGif(File tgs) throws IOException {
        String[] tgsToGifCmd = new String[]{"node", "./deps/tgs-to-gif/cli.js", tgs.toString()};
        exec(tgsToGifCmd, new String[]{"USE_SANDBOX=false"});
        File gif = Path.of(tgs.getPath() + ".gif").toFile();
        if (!gif.exists()) {
            throw new RuntimeException(String.format("could not create file %s", gif));
        }
        gif.deleteOnExit();
        LOG.info(() -> String.format("converted to gif: %s (%d bytes)", gif, gif.length()));
        return gif;
    }

    private File optimizeGif(File gif) throws IOException {
        int fps = 24;
        while (fps >= 10) {
            int f = fps;
            LOG.info(() -> String.format("optimizing %s with %d fps", gif, f));
            File optimized = optimizeGif(gif, fps);
            if (optimized.length() > 1024 * 256) {
                fps -= 2;
            } else {
                return optimized;
            }
        }
        throw new RuntimeException("cannot optimize gif, too chunky");
    }

    private File optimizeGif(File gif, int fps) throws IOException {
        Path optimizedGifPath = Path.of(gif.getPath() + ".optimized.gif");
        String[] ffmpegCommand = new String[]{
                "ffmpeg",
                "-i",
                gif.toString(),
                "-filter_complex",
                "[0:v] scale=96:-1,fps=" + fps + ":round=zero,split [a][b];" +
                        "[a] palettegen=reserve_transparent=on:transparency_color=ffffff [p];" +
                        "[b][p] paletteuse",
                "-y",
                optimizedGifPath.toString()
        };
        exec(ffmpegCommand, null);
        File optimized = optimizedGifPath.toFile();
        optimized.deleteOnExit();
        return optimized;
    }

    private void exec(String[] cmd, String[] env) throws IOException {
        LOG.info(() -> String.format("running cmd %s (env: %s)", Arrays.toString(cmd), Arrays.toString(env)));
        Process proc = Runtime.getRuntime().exec(cmd, env);
        try {
            int result = proc.waitFor();
            String stdout = new String(proc.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String stderr = new String(proc.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            LOG.info(() -> String.format("[stdout:%s,stderr:%s]", stdout, stderr));
            if (result != 0) {
                throw new RuntimeException(stderr);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
