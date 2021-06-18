package server;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record LightResponse(int statusCode, long contentLength, InputStream result, Map<String, String> headers) {
    public static Builder builder() {
        return new Builder();
    }

    public static LightResponse of(int statusCode, Throwable throwable) {
        return of(statusCode, throwable.getMessage());
    }

    public static LightResponse of(int statusCode, String result) {
        byte[] bytes = result.getBytes(StandardCharsets.UTF_8);
        return new LightResponse(
                statusCode,
                bytes.length,
                new ByteArrayInputStream(bytes),
                Map.of("Content-Type", "text/plain;charset=UTF-8"));
    }

    public static class Builder {
        private int statusCode;
        private long contentLength;
        private InputStream result;
        private final Map<String, String> headers;

        public Builder() {
            this.statusCode = 200;
            this.contentLength = 0;
            this.result = null;
            this.headers = new HashMap<>();
        }

        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder contentLength(long contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        public Builder result(InputStream result) {
            this.result = result;
            return this;
        }

        public Builder header(String key, String value) {
            headers.put(key, value);
            return this;
        }

        public LightResponse build() {
            Objects.requireNonNull(result);
            return new LightResponse(statusCode, contentLength, result, Collections.unmodifiableMap(headers));
        }
    }
}
