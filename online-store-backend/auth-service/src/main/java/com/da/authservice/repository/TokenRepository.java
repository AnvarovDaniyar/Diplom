package com.da.authservice.repository;

import com.da.authservice.domain.Token;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public interface TokenRepository extends ReactiveCrudRepository<Token, Long>{

  public Mono<Token> findByRefreshToken(String token);

  public Mono<Integer> deleteByRefreshToken(String token);
}
