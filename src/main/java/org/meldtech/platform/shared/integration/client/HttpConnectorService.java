package org.meldtech.platform.shared.integration.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.meldtech.platform.shared.integration.exception.AppException;
import org.meldtech.platform.shared.integration.exception.ClientError;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.Map;


@Slf4j
@Service
public class HttpConnectorService {
    private final WebClient webClient;
    private final ObjectMapper mapper;

    public static final String APPLICATION_JSON = "application/json";

    public HttpConnectorService(ObjectMapper mapper) {
        this.webClient = customWebClient();
        this.mapper = mapper;
    }

    public <T> Mono<T> post(String endpoint, Object requestBody, Map<String, String> headers, Class<T> returnType) {
        log.info("{}", endpoint);
        return webClient.post()
                .uri(endpoint)
                .headers(httpHeaders -> headers.forEach(httpHeaders::set)  )
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleClientError)
                .bodyToMono(returnType);
    }


    public <T> Mono<T> postForm(String endpoint,
                                MultiValueMap<String, String> requestBody,
                                Map<String, String> headers,
                                Class<T> returnType) {
        log.info("{}", endpoint);
        log.info("{}", headers);
        return webClient.post()
                .uri(endpoint)
                .headers(httpHeaders -> headers.forEach(httpHeaders::set)  )
                .body(BodyInserters.fromFormData( requestBody))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleClientError)
                .bodyToMono(returnType);
    }

    public <T> Mono<T> customPatch(String endpoint, Object requestBody, Class<T> returnType) {
        log.info("Custom URL: {}", endpoint);
        return customWebClient().patch()
                .uri(endpoint)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleClientError)
                .bodyToMono(returnType);
    }

    public <T> Mono<T> patch(String endpoint, Object requestBody, Class<T> returnType) {
        return webClient.patch()
                .uri(endpoint)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleClientError)
                .bodyToMono(returnType);
    }

    public <T> Mono<T> get(String endpoint, Class<T> returnType) {
        return webClient.get()
                .uri(endpoint)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleClientError)
                .bodyToMono(returnType);
    }

    public <T> Mono<T> get(String endpoint, Map<String, String> headers, Class<T> returnType) {
        return webClient.get()
                .uri(endpoint)
                .headers(httpHeaders -> headers.forEach(httpHeaders::set)  )
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleClientError)
                .bodyToMono(returnType);
    }

    public <T> Mono<T> get(String endpoint, Object pathVariable, Class<T> returnType) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(endpoint).build(pathVariable))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleClientError)
                .bodyToMono(returnType);
    }

    private WebClient customWebClient() {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .wiretap(true)
                        .followRedirect(true)))
                .defaultHeader("Accept", APPLICATION_JSON)
                .defaultHeader("Content-Type", APPLICATION_JSON)
                .build();
    }

    private Mono<Throwable> handleClientError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap( s -> {
                    log.error("Error from client {}", s);
                    ClientError clientError;
                    try {
                        clientError = mapper.readValue(s, ClientError.class);
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException(e));
                    }
                    return Mono.error(new AppException(clientError.getErrorDescription()));
                });
    }

}
