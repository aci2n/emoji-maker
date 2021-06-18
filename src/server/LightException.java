package server;

public class LightException extends RuntimeException {
    private final int statusCode;

    public LightException(int statusCode, Throwable cause) {
        this(statusCode, null, cause);
    }

    public LightException(int statusCode, String message) {
        this(statusCode, message, null);
    }

    public LightException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public static LightException internalServerError(Throwable cause) {
        return new LightException(500, cause);
    }

    public static LightException internalServerError(String message) {
        return new LightException(500, message);
    }

    public static LightException badRequest(String message) {
        return new LightException(400, message);
    }

    public static LightException methodNotAllowed(String method) {
        return new LightException(405, String.format("%s not allowed for this path%n", method));
    }
}
