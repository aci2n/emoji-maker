package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javax.swing.text.IconView;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public record EmojiHandler() implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        File tgs = writeTgs(exchange.getRequestBody());
        File gif = convertToGif(tgs);
        File optimized = optimizeGif(gif);
        long contentLength = optimized.length();
        exchange.sendResponseHeaders(200, contentLength);
        pipe(new FileInputStream(optimized), exchange.getResponseBody());
    }

    private File writeTgs(InputStream input) throws IOException {
        File stickerFile = Files.createTempFile("sticker", ".tgs").toFile();
        stickerFile.deleteOnExit();
        pipe(input, new FileOutputStream(stickerFile));
        return stickerFile;
    }

    private File convertToGif(File tgs) throws IOException {
        String[] tgsToGifCmd = new String[]{"node", "./deps/tgs-to-gif/cli.js", tgs.toString()};
        run(tgsToGifCmd, new String[]{"USE_SANDBOX=false"});
        File gifFile = Path.of(tgs.getPath() + ".gif").toFile();
        gifFile.deleteOnExit();
        return gifFile;
    }

    private File optimizeGif(File gif) throws IOException {
        int fps = 24;
        while (fps >= 10) {
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
        run(ffmpegCommand, null);
        File optimized = optimizedGifPath.toFile();
        optimized.deleteOnExit();
        return optimized;
    }

    private void pipe(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int n;
        while ((n = in.read(buffer)) > 0) {
            out.write(buffer, 0, n);
        }
    }

    private void run(String[] cmd, String[] env) throws IOException {
        Process proc = Runtime.getRuntime().exec(cmd, env);
        try {
            if (proc.waitFor() != 0) {
                String message = new String(proc.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                throw new RuntimeException(message);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
