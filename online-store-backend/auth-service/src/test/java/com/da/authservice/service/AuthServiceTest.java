package com.da.authservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import com.da.authservice.domain.Token;
import com.da.authservice.domain.User;
import com.da.authservice.dto.LoginRequest;
import com.da.authservice.dto.SignUpRequest;
import com.da.authservice.mapper.UserMapper;
import com.da.authservice.util.Provider;
import com.da.authservice.repository.UserRepository;
import com.da.authservice.security.JwtProvider;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
class AuthServiceTest {

  @Mock
  private TokenService tokenService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  @Mock
  private JwtProvider jwtProvider;

  @Mock
  private PasswordEncoder passwordEncoder;

  private AuthService authService;


  private static final User staticUser = Provider.generateUserForSignUpTest();
  private static final Token staticToken = Provider.generateTokenEntity();
  private static final LoginRequest staticLoginRequest = Provider.generateLoginRequest();
  private static final SignUpRequest staticSignUpRequest = Provider.generateSignUpRequest();

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    authService = new AuthServiceImpl(tokenService, userRepository, userMapper, jwtProvider, passwordEncoder);

    Mono<User> monoUser = Mono.just(staticUser);

    Mono<Token> token = Mono.just(staticToken);

    Mono<String> accessToken = Mono.just(staticToken.getAccessToken());

    BDDMockito.when(userMapper.signUpRequestToUser(any(SignUpRequest.class))).thenReturn(staticUser);

    BDDMockito.when(userRepository.save(any(User.class))).thenReturn(monoUser);

    BDDMockito.when(userRepository.findByUserName(anyString())).thenReturn(monoUser);

    BDDMockito.when(tokenService.persist(any(Token.class))).thenReturn(token);

    BDDMockito.when(tokenService.getByRefreshToken(anyString())).thenReturn(token);

    BDDMockito.when(tokenService.delete(anyString())).thenReturn(Mono.empty());

    BDDMockito.when(jwtProvider.generateAccessToken(any(User.class), anyString())).thenReturn(staticToken.getAccessToken());

    BDDMockito.when(jwtProvider.getJwtExpirationTime()).thenReturn(Provider.getJwtExpirationTime());

    BDDMockito.when(jwtProvider.validateToken(any(Token.class), anyString())).thenReturn(accessToken);

    BDDMockito.when(jwtProvider.getUsernameFromJwt(anyString())).thenReturn(Provider.TEST);

    BDDMockito.when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

    BDDMockito.when(passwordEncoder.encode(anyString())).thenReturn(Provider.PASSWORD);
  }

  @Test
  public void signup_ReturnAMonoTokenEntity_WhenSuccessful(){
    StepVerifier.create(authService.signup(staticSignUpRequest))
                .expectSubscription()
                .assertNext(token -> {
                  assertThat(token.getClass()).isEqualTo(Token.class);
                  Assertions.assertThat(token.getAccessToken()).isEqualTo(staticToken.getAccessToken());
                  Assertions.assertThat(token.getRefreshToken()).isEqualTo(staticToken.getRefreshToken());
                }).verifyComplete();
  }

  @Test
  public void login_ReturnAMonoTokenEntity_WhenSuccessful(){
    StepVerifier.create(authService.login(staticLoginRequest))
                .expectSubscription()
                .assertNext(token -> {
                  assertThat(token.getClass()).isEqualTo(Token.class);
                  Assertions.assertThat(token.getAccessToken()).isEqualTo(staticToken.getAccessToken());
                  Assertions.assertThat(token.getRefreshToken()).isEqualTo(staticToken.getRefreshToken());
                }).verifyComplete();
  }

  @Test
  public void login_ReturnAMonoErrorResponseStatusException_WhenTheRepositoryReturnsAnMonoEmpty(){
    BDDMockito.when(userRepository.findByUserName(anyString())).thenReturn(Mono.empty());

    StepVerifier.create(authService.login(staticLoginRequest))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
  }

  @Test
  public void login_ReturnAMonoErrorResponseStatusException_WhenPasswordsDoNotMatch(){
    BDDMockito.when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

    StepVerifier.create(authService.login(staticLoginRequest))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
  }

  @Test
  public void refreshToken_ReturnAMonoToken_WhenSuccessful() {
    StepVerifier.create(authService.refreshToken(staticToken))
                .expectSubscription()
                .assertNext(token -> {
                  assertThat(token.getClass()).isEqualTo(Token.class);
                  Assertions.assertThat(token.getAccessToken()).isEqualTo(staticToken.getAccessToken());
                  Assertions.assertThat(token.getRefreshToken()).isEqualTo(staticToken.getRefreshToken());
                }).verifyComplete();
  }
}
