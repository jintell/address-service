package org.meldtech.platform.shared.web;

import org.springframework.web.reactive.function.server.ServerRequest;

public final class Pagination {
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    private Pagination() {}

    public record Page(int page, int size, long offset) {}

    public static Page from(ServerRequest req) {
        int page = parseInt(req.queryParam("page").orElse(null), DEFAULT_PAGE);
        int size = parseInt(req.queryParam("size").orElse(null), DEFAULT_SIZE);
        if (page < 0) page = DEFAULT_PAGE;
        if (size <= 0) size = DEFAULT_SIZE;
        if (size > MAX_SIZE) size = MAX_SIZE;
        long offset = (long) page * (long) size;
        return new Page(page, size, offset);
    }

    private static int parseInt(String s, int def) {
        if (s == null) return def;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
