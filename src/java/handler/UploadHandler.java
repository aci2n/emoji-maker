package handler;

import com.sun.net.httpserver.HttpExchange;
import server.LightHandler;
import server.LightResponse;

public record UploadHandler() implements LightHandler {
    @Override
    public LightResponse get(HttpExchange exchange) {
        return LightResponse.builder()
                .statusCode(200)
                .result(getClass().getResourceAsStream("/upload.html"))
                .header("Content-Type", "text/html")
                .build();
    }
}
