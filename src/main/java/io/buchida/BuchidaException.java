package io.buchida;

public class BuchidaException extends RuntimeException {
    private final int statusCode;
    private final String errorCode;

    public BuchidaException(String message, int statusCode, String errorCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public BuchidaException(String message, int statusCode) {
        this(message, statusCode, null);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public static class AuthenticationException extends BuchidaException {
        public AuthenticationException(String message) {
            super(message, 401, "authentication_error");
        }
    }

    public static class RateLimitException extends BuchidaException {
        public RateLimitException(String message) {
            super(message, 429, "rate_limit_error");
        }
    }

    public static class NotFoundException extends BuchidaException {
        public NotFoundException(String message) {
            super(message, 404, "not_found");
        }
    }

    public static class ValidationException extends BuchidaException {
        public ValidationException(String message) {
            super(message, 422, "validation_error");
        }
    }
}
