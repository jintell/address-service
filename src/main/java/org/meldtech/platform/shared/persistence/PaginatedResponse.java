package org.meldtech.platform.shared.persistence;

/**
 * Pagination response envelope used by repositories/handlers to return paged data.
 * <p></p>
 * Fields:
 * - page: current page (zero-based index)
 * - size: page size (number of items per page)
 * - totalPages: total number of pages computed from total and size
 * - total: total number of items across all pages
 * - data: the page payload (list/collection or any object)
 * - previous: previous page index if available, otherwise null
 * - next: next page index if available, otherwise null
 */
public record PaginatedResponse(
        int page,
        int size,
        int totalPages,
        long total,
        Object data,
        Integer previous,
        Integer next
) {

    /**
     * Convenience factory that computes totalPages, previous, and next.
     *
     * @param page  current page (zero-based)
     * @param size  page size (> 0)
     * @param total total number of records (>= 0)
     * @param data  payload for this page
     * @return a populated PaginatedResponse
     */
    public static PaginatedResponse of(int page, int size, long total, Object data) {
        if (size <= 0) size = 1; // prevent division by zero; fallback to 1
        if (page < 0) page = 0;
        if (total < 0) total = 0;

        int totalPages = (total == 0L) ? 0 : (int) ((total + (long) size - 1) / (long) size);

        Integer previous = (page > 0) ? (page - 1) : null;
        Integer next = (page + 1 < totalPages) ? (page + 1) : null;

        return new PaginatedResponse(page, size, totalPages, total, data, previous, next);
    }
}
