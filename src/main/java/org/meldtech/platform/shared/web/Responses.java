package org.meldtech.platform.shared.web;

import org.meldtech.platform.shared.persistence.PaginatedResponse;
import org.meldtech.platform.shared.persistence.Response;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 *
 */
public final class Responses {

    private Responses() {}

    public static Mono<ServerResponse> ok(Object body) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Response.ok(body));
    }

    public static Mono<ServerResponse> created(Object body) {
        return ServerResponse.status(201)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Response.ok(body));
    }

    public static Mono<ServerResponse> error(int httpStatus, String message) {
        return ServerResponse.status(httpStatus)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Response.error(message));
    }

    /**
     * Build a paginated JSON response, wrapping the page in the common Response envelope.
     * Note: The source Flux is consumed twice (once for count, once for page data).
     * For large datasets, prefer repository-level count APIs.
     */
    public static <T> Mono<ServerResponse> paginated(ServerRequest req, Flux<T> source) {
        Pagination.Page p = Pagination.from(req);
        Mono<Long> totalMono = source.count();
        Mono<List<T>> pageDataMono = source.skip(p.offset()).take(p.size()).collectList();
        return Mono.zip(totalMono, pageDataMono)
                .flatMap(tuple -> {
                    long total = tuple.getT1();
                    List<T> data = tuple.getT2();
                    PaginatedResponse page = PaginatedResponse.of(p.page(), p.size(), total, data);
                    return ok(page);
                });
    }

    /**
     * Build a paginated JSON response, wrapping the page in the common Response envelope.
     * Note: The source Flux is consumed twice (once for count, once for page data).
     * For large datasets, prefer repository-level count APIs.
     */
    public static <T> Mono<ServerResponse> paginated(ServerRequest req, Flux<T> source, Mono<Long> totalMono) {
        Pagination.Page p = Pagination.from(req);
        Mono<List<T>> pageDataMono = source.collectList();
        return Mono.zip(totalMono, pageDataMono)
                .flatMap(tuple -> {
                    long total = tuple.getT1();
                    List<T> data = tuple.getT2();
                    PaginatedResponse page = PaginatedResponse.of(p.page(), p.size(), total, data);
                    return ok(page);
                });
    }
}
