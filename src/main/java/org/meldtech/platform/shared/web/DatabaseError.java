package org.meldtech.platform.shared.web;

public final class DatabaseError {
    private DatabaseError() {}
    private static final String UNIQUE = "Unique";
    private static final String NULL_VALUE = "not-null";
    private static final String INVALID_VALUE = "java.lang.IllegalArgumentException: ";
    private static final String DUPLICATE_MSG = "Record already exists. Please check your entries and resend again!";
    private static final String NULL_VALUE_MSG = "Mandatory field cannot be null!";
    private static final String BAD_REQUEST = "400 BAD_REQUEST";
    private static final String NOT_FOUND = "404 NOT_FOUND";
    private static final String DEFAULT_MSG = "Could not process request at this time. Please contact the admin";

    public static String massage(String error) {
        if(error.toLowerCase().contains(UNIQUE.toLowerCase())) return DUPLICATE_MSG;
        if(error.toLowerCase().contains(NULL_VALUE.toLowerCase())) return NULL_VALUE_MSG;
        if(error.toLowerCase().contains(INVALID_VALUE.toLowerCase())) return error.replace(INVALID_VALUE, "");
        if(error.toLowerCase().contains(BAD_REQUEST.toLowerCase())) return error.replace(BAD_REQUEST, "")
                .replace("\"", "");
        if(error.toLowerCase().contains(NOT_FOUND.toLowerCase())) return error.replace(NOT_FOUND, "")
                .replace("\"", "");
        else return DEFAULT_MSG;
    }

}
