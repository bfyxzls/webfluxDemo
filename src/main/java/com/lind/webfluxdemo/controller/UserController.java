package com.lind.webfluxdemo.controller;

import com.lind.webfluxdemo.User;
import com.lind.webfluxdemo.UserService;
import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/user")
public class UserController {
  private final UserService userService;

  @Autowired
  public UserController(final UserService userService) {
    this.userService = userService;
  }

  @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Resource not found")
  @ExceptionHandler(IllegalArgumentException.class)
  public void notFound() {
  }

  @GetMapping("")
  public Flux<User> list() {
    return this.userService.list();
  }

  @GetMapping("/{id}")
  public Mono<User> getById(@PathVariable("id") final String id) {
    return this.userService.getById(id);
  }

  @PostMapping("")
  public Mono<User> create(@RequestBody final User user) {
    return this.userService.createOrUpdate(user);
  }

  @PutMapping("/{id}")
  public Mono<User> update(@PathVariable("id") final String id, @RequestBody final User user) {
    Objects.requireNonNull(user);
    user.setId(id);
    return this.userService.createOrUpdate(user);
  }

  @DeleteMapping("/{id}")
  public Mono<User> delete(@PathVariable("id") final String id) {
    return this.userService.delete(id);
  }

  @GetMapping("/client")
  public void client() {
    final WebClient client = WebClient.create();
    client.get()
        .uri("http://localhost:8080/sse/randomNumbers")
        .accept(MediaType.TEXT_EVENT_STREAM)
        .exchange()
        .flatMapMany(response -> response.body(BodyExtractors.toFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {
        })))
        .filter(sse -> Objects.nonNull(sse.data()))
        .map(ServerSentEvent::data)
        .buffer(10)
        .doOnNext(System.out::println)
        .blockFirst();
  }

  @GetMapping("/ws")
  public void ws() {
    final WebSocketClient client = new ReactorNettyWebSocketClient();
    client.execute(URI.create("ws://localhost:8080/echo"), session ->
        session.send(Flux.just(session.textMessage("Hello")))
            .thenMany(session.receive().take(1).map(WebSocketMessage::getPayloadAsText))
            .doOnNext(System.out::println)
            .then())
        .block(Duration.ofMillis(5000));
  }
}