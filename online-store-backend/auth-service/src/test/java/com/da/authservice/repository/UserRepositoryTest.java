package com.da.authservice.repository;

import static com.da.authservice.util.Provider.generateUserRandomValues;
import static com.da.authservice.util.Provider.PASSWORD;
import static com.da.authservice.util.Provider.NEW_PASSWORD;
import static com.da.authservice.util.Provider.DEFAULT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.da.authservice.domain.Role;
import com.da.authservice.domain.User;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;

import io.r2dbc.spi.ConnectionFactory;
import reactor.test.StepVerifier;

@DataR2dbcTest
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ConnectionFactory connectionFactory;

  private static final User staticUser = generateUserRandomValues(Role.EMPLOYEE);

  @BeforeEach
  public void init(){
    final String dropSql = "DROP TABLE IF EXISTS users";

    final String initSql = "CREATE TABLE IF NOT EXISTS users (" +
              "id identity NOT NULL," +
              "user_name character varying(60) NOT NULL UNIQUE," +
              "password character varying(60) NOT NULL," +
              "email character varying(60) NOT NULL UNIQUE," +
              "role character varying(10) NOT NULL," +
              "check (role in ('CUSTOMER', 'EMPLOYEE', 'ADMIN')))";

    var template = new R2dbcEntityTemplate(connectionFactory);
    var dbClient = template.getDatabaseClient();

    StepVerifier.create(dbClient.sql(dropSql).then())
                .expectSubscription()
                .verifyComplete();

    StepVerifier.create(dbClient.sql(initSql).fetch().rowsUpdated())
                .expectNextCount(1)
                .verifyComplete();

    StepVerifier.create(template.insert(User.class)
                                      .using(staticUser)
                                      .then())
                .verifyComplete();
  }

  @Test
  public void save_SavePersistANewUser_WhenSuccessful() {
    StepVerifier.create(userRepository.save(generateUserRandomValues(Role.EMPLOYEE)))
                .assertNext(user -> {
                  assertThat(user).isNotNull();
                  assertThat(user.getUserId()).isNotNull();
                })
                .verifyComplete();
  }

  @Test
  public void save_ThrowIllegalArgumentException_WhenUserIsNull(){
    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> userRepository.save(null).subscribe());
  }

  @Test
  public void save_ThrowIllegalArgumentException_WhenUserIsEmpty(){
    StepVerifier.create(userRepository.save(User.builder().build()))
                .expectError(DataIntegrityViolationException.class)
                .verify();
  }

  @Test
  public void save_ThrowDataIntegrityViolationException_WhenThereIsAlreadyAUserSavedWithThatName(){
    User user = User.builder().userName(staticUser.getUsername()).password(PASSWORD).email("fake@email.com").role(Role.ADMIN).build();

    StepVerifier.create(userRepository.save(user))
                .expectError(DataIntegrityViolationException.class)
                .verify();
  }

  @Test
  public void save_ThrowDataIntegrityViolationException_WhenThereIsAlreadyAUserSavedWithThatEmail(){
    User user = User.builder().userName("name").password(PASSWORD).email(staticUser.getEmail()).role(Role.ADMIN).build();

    StepVerifier.create(userRepository.save(user))
                .expectError(DataIntegrityViolationException.class)
                .verify();
  }

  @Test
  public void findAll_ReturnAUsersFlux_WhenSuccesful() {
    StepVerifier.create(userRepository.findAll())
                .expectNextCount(1)
                .expectComplete()
                .verify();
  }

  @Test
  public void findAll_ReturnAFluxEmpty_WhenSuccesful_() {
    StepVerifier.create(userRepository.deleteAll())
                .expectSubscription()
                .verifyComplete();

    StepVerifier.create(userRepository.findAll())
                .expectSubscription()
                .expectNextCount(0)
                .expectComplete()
                .verify();
  }

  @Test
  public void findByUserName_ReturnAUser_WhenSuccesfful(){
    String name = staticUser.getUsername();
    StepVerifier.create(userRepository.findByUserName(name))
                .expectNextMatches(user -> user.getUsername().equals(name))
                .expectComplete()
                .verify();
  }

  @Test
  public void findByUserName_DoesNotThrowException_WhenNameParameterIsNull(){
    StepVerifier.create(userRepository.findByUserName(null))
                .expectSubscription()
                .expectComplete()
                .verify();
  }

  @Test
  public void findByUserName_ReturnADefaultUser_WhenTheFetchOperationReturnAnEmptyEntity(){
    String name = staticUser.getUsername();
    User defaultUser = User.builder().userId(0L).userName(DEFAULT).email(DEFAULT).role(Role.CUSTOMER).build();

    StepVerifier.create(userRepository.deleteAll())
                .expectSubscription()
                .verifyComplete();

    StepVerifier.create(userRepository.findByUserName(name).defaultIfEmpty(defaultUser))
                .expectSubscription()
                .assertNext(defaultU -> {
                    assertThat(defaultU).isEqualTo(defaultUser);
                    assertThat(defaultU.getUserId()).isEqualTo(0L);
                    assertThat(defaultU.getUsername()).isEqualTo(defaultUser.getUsername());
                })
                .verifyComplete();
  }

  @Test
  public void deleteByUserId_DeleteRemoveAnUserLog_WhenSuccessful(){
    Long userId = userRepository.save(generateUserRandomValues(Role.EMPLOYEE)).map(User::getUserId).block();

    StepVerifier.create(userRepository.deleteByUserId(userId))
                .expectNextMatches(num -> num.equals(1))
                .expectComplete()
                .verify();
  }

  @Test
  public void deleteByUserId_ReturnZeroAndDoesNotThrowException_WhenUserIdParameterIsNull(){
    StepVerifier.create(userRepository.deleteByUserId(null))
                .expectSubscription()
                .assertNext(num -> Assertions.assertThat(num).isGreaterThan(-1))
                .verifyComplete();
  }

  @Test
  public void deleteByUserId_ReturnZero_WhenThereIsNoUsersInTheRegistry(){
    StepVerifier.create(userRepository.deleteAll())
                .expectSubscription()
                .verifyComplete();

    StepVerifier.create(userRepository.deleteByUserId(1L))
                .expectSubscription()
                .expectNextMatches(num -> num.equals(0))
                .verifyComplete();
  }

  @Test
  public void updatePasswordById_ReturnAIntegerEqualsToOneAndUpdateThePasswordOfAUserLog_WhenSuccessful(){
    User userDB = userRepository.findByUserName(staticUser.getUsername()).block();
    Long id = userDB.getUserId();
    String oldPassword = userDB.getPassword();

    StepVerifier.create(userRepository.updatePasswordById(id, NEW_PASSWORD))
                .expectNextMatches(num -> num.equals(1))
                .expectComplete()
                .verify();

    StepVerifier.create(userRepository.findByUserName(userDB.getUsername()))
                .expectNextMatches(user -> user.getPassword().equals(NEW_PASSWORD) && !user.getPassword().equals(oldPassword))
                .expectComplete()
                .verify();
  }

  @Test
  public void updatePasswordById_ReturnZeroAndDoesNotThrowException_WhenParametersAreNull(){
    StepVerifier.create(userRepository.updatePasswordById(null, null))
                .expectNextMatches(num -> num.equals(0))
                .expectComplete()
                .verify();
  }

  @Test
  public void updatePasswordById_ReturnZero_WhenThereIsNoUsersInTheRegistry(){
    StepVerifier.create(userRepository.deleteAll())
                .expectSubscription()
                .verifyComplete();

    StepVerifier.create(userRepository.updatePasswordById(1L, PASSWORD))
                .expectSubscription()
                .expectNextMatches(num -> num.equals(0))
                .verifyComplete();
  }
}
