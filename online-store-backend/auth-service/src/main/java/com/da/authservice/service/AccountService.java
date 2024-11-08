package com.da.authservice.service;

import com.da.authservice.dto.AccountPayload;
import com.da.authservice.dto.AccountView;
import com.da.authservice.dto.PasswordUpdateDto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService {

  public Mono<String> persist(AccountPayload accountPayload);

  public Mono<Void> updatePassword(PasswordUpdateDto password);

  public Mono<Void> delete(Long userId);

  public Flux<AccountView> getAll();
}
