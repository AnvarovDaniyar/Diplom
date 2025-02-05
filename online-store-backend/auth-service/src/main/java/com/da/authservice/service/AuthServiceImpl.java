package com.da.authservice.service;

import java.util.UUID;

import com.da.authservice.domain.Token;
import com.da.authservice.domain.User;
import com.da.authservice.dto.AccountView;
import com.da.authservice.dto.LoginRequest;
import com.da.authservice.dto.SignUpRequest;
import com.da.authservice.mapper.UserMapper;
import com.da.authservice.repository.UserRepository;
import com.da.authservice.security.JwtProvider;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements ReactiveUserDetailsService, AuthService {

  private static final String USER_NOT_FOUND = "User Not Found!!";
  private static final String WRONG_PASSWORD = "Wrong Password!!";
  private static final String INVALID_TOKEN = "Invalid Token!!";

  private final TokenService tokenService;
  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final JwtProvider jwtProvider;
  private final PasswordEncoder passwordEncoder;

  @Override
  public Mono<UserDetails> findByUsername(String username) {
    return userRepository.findByUserName(username)
           .switchIfEmpty(getMonoError(HttpStatus.NOT_FOUND, USER_NOT_FOUND))
           .cast(UserDetails.class);
  }

  @Transactional
  @Override
  public Mono<Token> signup(SignUpRequest signUpRequest) {
    return userRepository.save(userMapper.signUpRequestToUser(signUpRequest))
           .flatMap(user -> persistToken(user, UUID.randomUUID().toString()));
  }

  @Override
  public Mono<Token> login(LoginRequest loginRequest) {
    String password = loginRequest.getPassword();

    return findByUsername(loginRequest.getUserName()).cast(User.class)
           .flatMap(user -> verifyPassword(user, password))
           .flatMap(user -> persistToken(user, UUID.randomUUID().toString()));
  }

  @Override
  public Mono<AccountView> currentUser() {
    return ReactiveSecurityContextHolder.getContext()
          .map(sc -> sc.getAuthentication().getName())
          .flatMap(name -> findByUsername(name))
          .cast(User.class)
          .map(user -> userMapper.userToAccountView(user));
  }

  @Override
  public Mono<Token> refreshToken(Token token) {
    return tokenService.getByRefreshToken(token.getRefreshToken())
            .flatMap(dbToken -> jwtProvider.validateToken(token, dbToken.getAccessToken()))
            .switchIfEmpty(getMonoError(HttpStatus.NOT_ACCEPTABLE, INVALID_TOKEN))
            .map(accessToken -> jwtProvider.getUsernameFromJwt(accessToken))
            .flatMap(userName -> findByUsername(userName)
              .cast(User.class)
              .flatMap(userDb -> tokenService.delete(token.getRefreshToken())
                                .then(persistToken(userDb, UUID.randomUUID().toString())))
            );
  }


  private Mono<Token> persistToken(User user, String issuer) {
    return Mono.just(buildTokenEntity(user, issuer))
          .flatMap(token -> tokenService.persist(token));
  }

  private Token buildTokenEntity(User user, String issuer) {
  return Token.builder()
        .accessToken(jwtProvider.generateAccessToken(user, issuer))
        .refreshToken(jwtProvider.generateRefreshToken(user.getUsername(), issuer))
        .build();
  }

  private Mono<User> verifyPassword(User user, String password) {
    return passwordEncoder.matches(password, user.getPassword()) ? Mono.just(user)
                                                                 : getMonoError(HttpStatus.BAD_REQUEST, WRONG_PASSWORD);
  }

  private <T> Mono<T> getMonoError(HttpStatus status, String message) {
    return Mono.error(new ResponseStatusException(status, message));
  }
}
