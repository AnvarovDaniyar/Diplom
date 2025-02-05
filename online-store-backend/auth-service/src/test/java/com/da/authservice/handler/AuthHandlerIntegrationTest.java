package com.da.authservice.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.da.authservice.domain.Token;
import com.da.authservice.domain.User;
import com.da.authservice.dto.AccountView;
import com.da.authservice.dto.LoginRequest;
import com.da.authservice.dto.SignUpRequest;
import com.da.authservice.mapper.UserMapper;
import com.da.authservice.service.AuthServiceImpl;
import com.da.authservice.service.TokenServiceImpl;
import com.da.authservice.util.Provider;
import com.da.authservice.configuration.Router;
import com.da.authservice.configuration.SecurityConfig;
import com.da.authservice.repository.TokenRepository;
import com.da.authservice.repository.UserRepository;
import com.da.authservice.security.AuthenticationManager;
import com.da.authservice.security.JwtProvider;
import com.da.authservice.security.SecurityContextRepository;
import com.da.authservice.service.AccountServiceImpl;
import com.da.authservice.validator.ConstraintValidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.gateway.handler.predicate.HostRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;

@WebFluxTest
@Import({ AuthServiceImpl.class, AccountServiceImpl.class, TokenServiceImpl.class, ConstraintValidator.class })
@ContextConfiguration(classes = { SecurityConfig.class, AuthenticationManager.class, SecurityContextRepository.class,
                                  Router.class, RouteLocatorBuilder.class, PathRoutePredicateFactory.class, HostRoutePredicateFactory.class,
                                  AuthHandler.class, AccountHandler.class })
class AuthHandlerIntegrationTest {

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private UserMapper userMapper;

  @MockBean
  private TokenRepository tokenRepository;

  @MockBean
  private PasswordEncoder passwordEncoder;

  @MockBean
  private JwtProvider jwtProvider;

  @Autowired
  private ApplicationContext applicationContext;

  private WebTestClient webTestClient;

  private static final User staticUser = Provider.generateUserStaticValuesForIT();
  private static final LoginRequest staticLoginRequest = Provider.generateLoginRequest();
  private static final AccountView staticAccountView = new AccountView(1L, Provider.TEST, Provider.TEST_EMAIL, Provider.ADMIN);
  private static final Token staticToken = Provider.generateTokenEntity();

  @BeforeEach
  public void setUp() {
    webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();

    Mono<User> user = Mono.just(staticUser);

    Mono<Token> token = Mono.just(staticToken);

    Mono<String> accessToken = Mono.just(staticToken.getAccessToken());

    BDDMockito.when(userRepository.findByUserName(anyString())).thenReturn(user);

    BDDMockito.when(userRepository.save(any(User.class))).thenReturn(user);

    BDDMockito.when(userMapper.signUpRequestToUser(any(SignUpRequest.class))).thenReturn(staticUser);

    BDDMockito.when(userMapper.userToAccountView(any(User.class))).thenReturn(staticAccountView);

    BDDMockito.when(passwordEncoder.encode(anyString())).thenReturn(Provider.PASSWORD);

    BDDMockito.when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

    BDDMockito.when(tokenRepository.save(any(Token.class))).thenReturn(token);

    BDDMockito.when(tokenRepository.findByRefreshToken(anyString())).thenReturn(token);

    BDDMockito.when(tokenRepository.deleteByRefreshToken(anyString())).thenReturn(Mono.just(1));

    BDDMockito.when(jwtProvider.validateToken(any(Token.class), anyString())).thenReturn(accessToken);

    BDDMockito.when(jwtProvider.getUsernameFromJwt(anyString())).thenReturn(Provider.TEST);

    BDDMockito.when(jwtProvider.generateAccessToken(any(User.class), anyString())).thenReturn(staticToken.getAccessToken());

    BDDMockito.when(jwtProvider.generateRefreshToken(anyString(), anyString())).thenReturn(staticToken.getRefreshToken());
  }

  @Test
  public void signup_Return201StatusCode_WhenSuccesful() {
    SignUpRequest signUpRequest = Provider.generateSignUpRequest();

    webTestClient.post()
                  .uri("/auth/signup")
                  .contentType(Provider.JSON)
                  .accept(Provider.JSON)
                  .body(Mono.just(signUpRequest), SignUpRequest.class)
                  .exchange()
                  .expectStatus().isCreated()
                  .expectBody(Void.class);

    verify(userRepository, times(1)).save(any(User.class));
    verify(tokenRepository, times(1)).save(any(Token.class));
    verify(userMapper, times(1)).signUpRequestToUser(any(SignUpRequest.class));
  }

  @Test
  public void signup_Return400StatusCode_WhenSignUpRequestHasInvalidFields() {
    SignUpRequest invalidSignUpRequest = SignUpRequest.builder().userName("").email("").password("").build();

    webTestClient.post()
                  .uri("/auth/signup")
                  .contentType(Provider.JSON)
                  .accept(Provider.JSON)
                  .body(Mono.just(invalidSignUpRequest), SignUpRequest.class)
                  .exchange()
                  .expectStatus().isBadRequest()
                  .expectBody(Void.class);

    verify(userRepository, times(0)).save(any(User.class));
    verify(tokenRepository, times(0)).save(any(Token.class));
    verify(passwordEncoder, times(0)).encode(anyString());
  }

  @Test
  public void login_Return200StatusCode_WhenSuccessful() {
    webTestClient.post()
                  .uri("/auth/login")
                  .contentType(Provider.JSON)
                  .accept(Provider.JSON)
                  .body(Mono.just(staticLoginRequest), LoginRequest.class)
                  .exchange()
                  .expectStatus().isOk()
                  .expectBody(Void.class);

    verify(tokenRepository, times(1)).save(any(Token.class));
    verify(userRepository, times(1)).findByUserName(anyString());
    verify(passwordEncoder, times(1)).matches(anyString(), anyString());
  }

  @Test
  public void login_Return404StatusCode_WhenUserWasNotFoundInTheRegistry() {
    BDDMockito.when(userRepository.findByUserName(anyString())).thenReturn(Mono.empty());

    webTestClient.post()
                  .uri("/auth/login")
                  .contentType(Provider.JSON)
                  .accept(Provider.JSON)
                  .body(Mono.just(staticLoginRequest), LoginRequest.class)
                  .exchange()
                  .expectStatus().isNotFound()
                  .expectBody(Void.class);

    verify(userRepository, times(1)).findByUserName(anyString());
    verify(tokenRepository, times(0)).save(any(Token.class));
    verify(passwordEncoder, times(0)).matches(anyString(), anyString());
  }

  @Test
  public void login_Return400StatusCode_WhenPasswordsDoesNotMatch() {
    BDDMockito.when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

    webTestClient.post()
                  .uri("/auth/login")
                  .contentType(Provider.JSON)
                  .accept(Provider.JSON)
                  .body(Mono.just(staticLoginRequest), LoginRequest.class)
                  .exchange()
                  .expectStatus().isBadRequest()
                  .expectBody(Void.class);

    verify(userRepository, times(1)).findByUserName(anyString());
    verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    verify(tokenRepository, times(0)).save(any(Token.class));
  }

  @Test
  public void login_Return400StatusCode_WhenLoginRequestHasInvalidFields() {
    LoginRequest invalidLoginRequest = new LoginRequest("", "");
    webTestClient.post()
                  .uri("/auth/login")
                  .contentType(Provider.JSON)
                  .accept(Provider.JSON)
                  .body(Mono.just(invalidLoginRequest), LoginRequest.class)
                  .exchange()
                  .expectStatus().isBadRequest()
                  .expectBody(Void.class);

    verify(userRepository, times(0)).findByUserName(anyString());
    verify(passwordEncoder, times(0)).matches(anyString(), anyString());
    verify(tokenRepository, times(0)).save(any(Token.class));
  }

  @Test
  @WithMockUser(username = Provider.TEST, password = Provider.TEST, authorities = Provider.ADMIN)
  public void getCurrentUser_Return200StatusCodeAndMonoAccountView_WhenSuccessful() {
    webTestClient.get()
                  .uri("/auth/users")
                  .accept(Provider.JSON)
                  .exchange()
                  .expectStatus().isOk()
                  .expectHeader().contentType(Provider.JSON)
                  .expectBody(AccountView.class)
                  .value(ac -> {
                    assertThat(ac).isNotNull();
                    assertThat(ac.getRole()).isEqualTo(Provider.ADMIN);
                  });

    verify(userRepository, times(1)).findByUserName(anyString());
  }

  @Test
  public void getCurrentUser_Return403StatusCode_WhenUserIsNotAuthenticated() {
    webTestClient.get()
                  .uri("/auth/users")
                  .accept(Provider.JSON)
                  .exchange()
                  .expectStatus().isForbidden()
                  .expectBody(Void.class);

    verify(userRepository, times(0)).findByUserName(anyString());
  }

  @Test
  @WithMockUser(username = Provider.TEST, password = Provider.TEST, authorities = Provider.ADMIN)
  public void refreshToken_Return200StatusCode_WhenSuccessful() {
    webTestClient.post()
                  .uri("/auth/refresh-token")
                  .cookie("JWT", staticToken.getAccessToken())
                  .cookie("RT", staticToken.getRefreshToken())
                  .exchange()
                  .expectStatus().isOk()
                  .expectBody(Void.class);

    verify(tokenRepository, times(1)).findByRefreshToken(anyString());
    verify(userRepository, times(1)).findByUserName(anyString());
  }

  @Test
  public void refreshToken_Return403StatusCode_WhenUserIsNotAuthenticated() {
    webTestClient.post()
                  .uri("/auth/refresh-token")
                  .cookie("JWT", staticToken.getAccessToken())
                  .cookie("RT", staticToken.getRefreshToken())
                  .exchange()
                  .expectStatus().isForbidden()
                  .expectBody(Void.class);

    verify(tokenRepository, times(0)).save(any(Token.class));
    verify(tokenRepository, times(0)).findByRefreshToken(anyString());
    verify(userRepository, times(0)).findByUserName(anyString());
    verify(passwordEncoder, times(0)).matches(anyString(), anyString());
  }

  @Test
  @WithMockUser(username = Provider.TEST, password = Provider.TEST, authorities = Provider.ADMIN)
  public void refreshToken_Return400StatusCode_WhenTokenRequestHasInvalidFields() {
    webTestClient.post()
                  .uri("/auth/refresh-token")
                  .cookie("JWT", "")
                  .cookie("RT", "")
                  .exchange()
                  .expectStatus().isBadRequest()
                  .expectBody(Void.class);

    verify(tokenRepository, times(0)).findByRefreshToken(anyString());
    verify(userRepository, times(0)).findByUserName(anyString());
  }

  @Test
  @WithMockUser(username = Provider.TEST, password = Provider.TEST, authorities = Provider.ADMIN)
  public void logout_Return204StatusCode_WhenSuccessful() {
    webTestClient.post()
                  .uri("/auth/logout")
                  .cookie("JWT", staticToken.getAccessToken())
                  .cookie("RT", staticToken.getRefreshToken())
                  .exchange()
                  .expectStatus().isNoContent()
                  .expectBody(Void.class);

    verify(tokenRepository, times(1)).deleteByRefreshToken(anyString());
  }

  @Test
  @WithMockUser(username = Provider.TEST, password = Provider.TEST, authorities = Provider.ADMIN)
  public void logout_Return404StatusCode_WhenTokenWasNotFound() {
    BDDMockito.when(tokenRepository.deleteByRefreshToken(anyString())).thenReturn(Mono.just(0));

    webTestClient.post()
                  .uri("/auth/logout")
                  .cookie("JWT", staticToken.getAccessToken())
                  .cookie("RT", staticToken.getRefreshToken())
                  .exchange()
                  .expectStatus().isNotFound()
                  .expectBody(Void.class);

    verify(tokenRepository, times(1)).deleteByRefreshToken(anyString());
  }

  @Test
  public void logout_Return403StatusCode_WhenUserIsNotAuthenticated() {
    webTestClient.post()
                  .uri("/auth/logout")
                  .cookie("JWT", staticToken.getAccessToken())
                  .cookie("RT", staticToken.getRefreshToken())
                  .exchange()
                  .expectStatus().isForbidden()
                  .expectBody(Void.class);

    verify(tokenRepository, times(0)).deleteByRefreshToken(anyString());
  }
}
