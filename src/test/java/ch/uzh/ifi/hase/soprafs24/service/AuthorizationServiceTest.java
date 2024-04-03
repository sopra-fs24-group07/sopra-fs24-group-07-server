package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

public class AuthorizationServiceTest {
  @Mock private UserRepository userRepository;

  @InjectMocks private AuthorizationService authorizationService;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
  }

  // region login tests

  /**
   * verify that when user logins the username and password are correctly checked
   */
  @Test
  public void login_validCredentials() {
    String testToken = "1";

    // given
    User testUser = new User();
    testUser.setUsername("batman");
    testUser.setPassword(("i am"));
    testUser.setToken(testToken);

    // when -> find user by username in repository -> return the dummy
    Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(testUser);

    // then -> attempt to login user
    String token = authorizationService.login(testUser.getUsername(), testUser.getPassword());

    // then
    assertEquals(token, testToken);
  }

  /**
   * verify that when user logins and the username does not exist, null is returned
   */
  @Test
  public void login_invalidCredentials_WrongUsername() {
    String testToken = "1";

    // given
    User testUser = new User();
    testUser.setUsername("batman");
    testUser.setPassword(("i am"));
    testUser.setToken(testToken);

    // when -> find user by username in repository -> username does not exist
    Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

    // then -> attempt to login user -> wrong username
    String token = authorizationService.login(testUser.getUsername(), testUser.getPassword());

    // then
    assertNull(token);
  }

  /**
   * verify that when user logins and the username does exist, but wrong password, null is returned
   */
  @Test
  public void login_invalidCredentials_WrongPassword() {
    String testToken = "1";

    // given
    User testUser = new User();
    testUser.setUsername("batman");
    testUser.setPassword(("i am"));
    testUser.setToken(testToken);

    // when -> find user by username in repository -> username does not exist
    Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(testUser);

    // then -> attempt to login user -> wrong password
    String token = authorizationService.login(testUser.getUsername(), "No i am batman");

    // then
    assertNull(token);
  }

  // endregion

  // region isAuthorized tests

  /**
   * Test that if token is valid, true is returned
   */
  @Test
  public void isAuthorized_validToken() {
    // given which would be found by the token
    User testUser = new User();

    // when -> find user by token -> user is returned
    Mockito.when(userRepository.findByToken(Mockito.anyString())).thenReturn(testUser);

    // then -> no exception is thrown
    authorizationService.isAuthorized("some valid token");
  }

  /**
   * Test that if token is invalid, false is returned
   */
  @Test
  public void isAuthorized_invalidToken() {
    // given no user which can be found by token

    // when -> find user by token -> no user is returned because token is invalid
    Mockito.when(userRepository.findByToken(Mockito.anyString())).thenReturn(null);

    // then -> exception is thrown
    assertThrows(ResponseStatusException.class,
        () -> authorizationService.isAuthorized("some invalid token"));
  }

  /**
   * test that if token and username correspond, true is returned
   */
  @Test
  public void isAuthorized_tokenAndUsername_valid() {
    String username = "batman";
    // given no user which can be found by token
    User testUser = new User();
    testUser.setUsername(username);

    // when -> find user by token -> user is returned
    Mockito.when(userRepository.findByToken(Mockito.anyString())).thenReturn(testUser);

    // then (does not throw exception)
    authorizationService.isAuthorized("batman's token", username);
  }

  /**
   * test that if token cannot be found, false is returned
   */
  @Test
  public void isAuthorized_tokenAndUsername_invalidToken() {
    String username = "batman";
    // given no user which can be found by token
    User testUser = new User();
    testUser.setUsername(username);

    // when -> find user by token -> no user found because token is invalid
    Mockito.when(userRepository.findByToken(Mockito.anyString())).thenReturn(null);

    // then
    assertThrows(ResponseStatusException.class,
        () -> authorizationService.isAuthorized("invalid token", username));
  }

  /**
   * test that if token and username do not correspond, false is returned
   */
  @Test
  public void isAuthorized_tokenAndUsername_tokenAndUsernameNotCorresponding() {
    String username = "batman";
    // given no user which can be found by token
    User testUser = new User();
    testUser.setUsername(username);

    // when -> find user by token -> no user found because token is invalid
    Mockito.when(userRepository.findByToken(Mockito.anyString())).thenReturn(testUser);

    // then
    assertThrows(ResponseStatusException.class,
        () -> authorizationService.isAuthorized("batman's token", "robin"));
  }
  /**
   * test that if token and userId correspond, true is returned
   */
  @Test
  public void isAuthorized_tokenAndUserId_valid() {
    // given no user which can be found by token
    User testUser = new User();
    testUser.setUserId(1L);

    // when -> find user by token -> user is returned
    Mockito.when(userRepository.findByToken(Mockito.anyString())).thenReturn(testUser);

    // then (does not throw exception)
    authorizationService.isAuthorized("batman's token", 1L);
  }

  /**
   * test that if token cannot be found, false is returned
   */
  @Test
  public void isAuthorized_tokenAndUserId_invalidToken() {
    // when -> find user by token -> no user found because token is invalid
    Mockito.when(userRepository.findByToken(Mockito.anyString())).thenReturn(null);

    // then
    assertThrows(ResponseStatusException.class,
        () -> authorizationService.isAuthorized("invalid token", 1L));
  }

  /**
   * test that if token and userId do not correspond, false is returned
   */
  @Test
  public void isAuthorized_tokenAndUserId_tokenAndUserIdNotCorresponding() {
    String username = "batman";
    // given no user which can be found by token
    User testUser = new User();
    testUser.setUserId(1L);

    // when -> find user by token -> no user found because token is invalid
    Mockito.when(userRepository.findByToken(Mockito.anyString())).thenReturn(testUser);

    // then
    assertThrows(ResponseStatusException.class,
        () -> authorizationService.isAuthorized("batman's token", 2L));
  }

  // endregion
}
