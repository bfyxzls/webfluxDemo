package com.lind.webfluxdemo;

import static java.time.Duration.ofMillis;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_STREAM_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class QuoteHandler {

  private final Flux<Quote> quoteStream;

  public QuoteHandler(QuoteGenerator quoteGenerator) {
    this.quoteStream = quoteGenerator.fetchQuoteStream(ofMillis(1000)).share();
  }

  public Mono<ServerResponse> hello(ServerRequest request) {
    return ok().contentType(TEXT_PLAIN)
        .body(BodyInserters.fromObject("Hello Spring!"));
  }

  public Mono<ServerResponse> echo(ServerRequest request) {
    return ok().contentType(TEXT_PLAIN)
        .body(request.bodyToMono(String.class), String.class);
  }

  public Mono<ServerResponse> streamQuotes(ServerRequest request) {
    return ok()
        .contentType(APPLICATION_STREAM_JSON)
        .body(this.quoteStream, Quote.class);
  }

  public Mono<ServerResponse> fetchQuotes(ServerRequest request) {
    int size = Integer.parseInt(request.queryParam("size").orElse("10"));
    return ok()
        .contentType(APPLICATION_JSON)
        .body(this.quoteStream.take(size), Quote.class);
  }
}