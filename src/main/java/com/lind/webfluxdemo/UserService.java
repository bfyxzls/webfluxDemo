package com.lind.webfluxdemo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserService {
  private final Map<String, User> data = new ConcurrentHashMap<>();

  public Flux<User> list() {
    return Flux.fromIterable(this.data.values());
  }

  public Flux<User> getById(final Flux<String> ids) {
    return ids.flatMap(id -> Mono.justOrEmpty(this.data.get(id)));
  }

  public Mono<User> getById(final String id) {
    return Mono.justOrEmpty(this.data.get(id))
        .switchIfEmpty(Mono.error(new IllegalArgumentException()));
  }

  public Mono<User> createOrUpdate(final User user) {
    this.data.put(user.getId(), user);
    return Mono.just(user);
  }

  public Mono<User> delete(final String id) {
    return Mono.justOrEmpty(this.data.remove(id));
  }
}