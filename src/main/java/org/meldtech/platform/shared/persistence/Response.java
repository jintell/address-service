package org.meldtech.platform.shared.persistence;

/**
 * Common response envelope for the application.
 * <p>
 * Fields:
 * - status: overall outcome flag (true for success, false for failure)
 * - message: human-readable message
 * - data: payload object (maybe null)
 */
public record Response(
        boolean status,
        String message,
        Object data
) {

    /**
     * Convenience factory for a successful response with payload.
     */
    public static Response ok(Object data) {
        return new Response(true, "success", data);
    }

    /**
     * Convenience factory for a successful response with a custom message and payload.
     */
    public static Response ok(String message, Object data) {
        return new Response(true, message, data);
    }

    /**
     * Convenience factory for an error response with a message.
     */
    public static Response error(String message) {
        return new Response(false, message, null);
    }

    /**
     * Convenience factory for an error response with a message and payload.
     */
    public static Response error(String message, Object data) {
        return new Response(false, message, data);
    }
}
