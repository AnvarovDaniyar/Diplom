package com.da.authservice.service;

import com.da.authservice.domain.Token;

import reactor.core.publisher.Mono;

public interface TokenService {

  public Mono<Token> persist(Token token);

  public Mono<Token> getByRefreshToken(String token);

  public Mono<Void> delete(String token);
}
