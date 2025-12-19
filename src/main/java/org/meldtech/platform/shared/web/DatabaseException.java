package org.meldtech.platform.shared.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exception thrown when an operation related to Database execution fails.
 */
public class DatabaseException extends ResponseStatusException {
    /**
     * Constructs an ApiClientException with the specified detail message.
     *
     * @param message the detail message
     */
    public DatabaseException(String message) {
        super(HttpStatus.BAD_REQUEST, DatabaseError.massage(message));
    }

}
