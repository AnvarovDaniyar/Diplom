package com.da.authservice.service;

import com.da.authservice.domain.Token;
import com.da.authservice.dto.AccountView;
import com.da.authservice.dto.LoginRequest;
import com.da.authservice.dto.SignUpRequest;

import reactor.core.publisher.Mono;

public interface AuthService {

  public Mono<Token> signup(SignUpRequest signUpRequest);

  public Mono<Token> login(LoginRequest loginRequest);

  public Mono<AccountView> currentUser();

  public Mono<Token> refreshToken(Token token);
}
