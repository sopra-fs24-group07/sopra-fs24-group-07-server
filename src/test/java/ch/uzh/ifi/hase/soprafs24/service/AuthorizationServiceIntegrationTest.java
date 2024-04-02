package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

@WebAppConfiguration
@SpringBootTest
public class AuthorizationServiceIntegrationTest {
  @Qualifier("userRepository") @Autowired private UserRepository userRepository;

  @Autowired private AuthorizationService authorizationService;
  @Autowired private UserService userService;

  @BeforeEach
  public void setup() {
    userRepository.deleteAll();
  }

  /**
   * test if the login works with valid credentials
   */
  @Test
  public void login_validCredentials() {
    // given
    User testUser = new User();
    testUser.setName("Bruce Wayne");
    testUser.setUsername("batman");
    testUser.setPassword("alfred123");

    // if create user works, is tested in other tests
    User createdUser = userService.createUser(testUser);

    // when
    String token = authorizationService.login(testUser.getUsername(), testUser.getPassword());

    // then
    assertEquals(token, testUser.getToken());
  }

  /**
   * test if the login fails with invalid credentials
   */
  @Test
  public void login_invalidCredentials() {
    // given no user exists
    assertNull(userRepository.findByUsername("batman"));

    // when
    String token = authorizationService.login("batman", "alfred123");

    // then
    assertNull(token);
  }

  /**
   * test if the token is valid
   */
  @Test
  public void isAuthorized_validToken() {
    // given
    User testUser = new User();
    testUser.setName("Bruce Wayne");
    testUser.setUsername("batman");
    testUser.setPassword("alfred123");

    // if create user works, is tested in other tests
    User createdUser = userService.createUser(testUser);

    // when try auth -> then no exception
    assertDoesNotThrow(() -> authorizationService.isAuthorized(testUser.getToken()));
  }

  /**
   * test if the token is invalid (regardless if user exists)
   */
  @Test
  public void isAuthorized_invalidToken() {
    // given no user exists
    assertNull(userRepository.findByToken("invalid token"));

    // when try auth -> then exception
    assertThrows(
        ResponseStatusException.class, () -> authorizationService.isAuthorized("invalid token"));
  }

  /**
   * test if the token is valid and maps to existing user
   */
  @Test
  public void isAuthorized_tokenMapsToUser_success() {
    // given
    User testUser = new User();
    testUser.setName("Bruce Wayne");
    testUser.setUsername("batman");
    testUser.setPassword("alfred123");

    // if create user works, is tested in other tests
    User createdUser = userService.createUser(testUser);

    // when try auth -> then exception
    User authorizedUser =
        authorizationService.isAuthorized(testUser.getToken(), testUser.getUsername());

    // then
    assertEquals(createdUser.getUsername(), authorizedUser.getUsername());
    assertEquals(createdUser.getToken(), authorizedUser.getToken());
  }

  /**
   * test if the token is invalid, but username does exist
   */
  @Test
  public void isAuthorized_tokenMapsToUser_invalidUserToken() {
    // given
    User testUser = new User();
    testUser.setName("Bruce Wayne");
    testUser.setUsername("batman");
    testUser.setPassword("alfred123");

    // if create user works, is tested in other tests
    User createdUser = userService.createUser(testUser);

    // when try auth -> then exception
    assertThrows(ResponseStatusException.class,
        () -> authorizationService.isAuthorized("invalid token", testUser.getUsername()));
  }

  /**
   * test if the token is invalid, but username does exist
   */
  @Test
  public void isAuthorized_tokenMapsToUser_userDoesNotExist() {
    // given no user exists
    assertNull(userRepository.findByUsername("batman"));

    // when try auth -> then exception
    assertThrows(
        ResponseStatusException.class, () -> authorizationService.isAuthorized("token", "batman"));
  }
}
