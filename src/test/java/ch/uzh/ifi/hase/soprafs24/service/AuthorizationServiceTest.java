package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AuthorizationServiceTest {
  @Mock private UserRepository userRepository;

  @Mock private TeamUserService teamUserService;

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
    testUser.setUserId(1L);
    testUser.setUsername("batman");
    testUser.setPassword(("i am"));
    testUser.setToken(testToken);

    // when -> find user by username in repository -> return the dummy
    Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(testUser);

    // then -> attempt to login user
    User user = authorizationService.login(testUser.getUsername(), testUser.getPassword());

    // then
    assertEquals(user.getToken(), testToken);
    assertEquals(user.getUserId(), testUser.getUserId());
  }

  /**
   * verify that when user logins and the username does not exist, null is returned
   */
  @Test
  public void login_invalidCredentials_WrongUsername() {
    String testToken = "1";

    // given
    User testUser = new User();
    testUser.setUserId(1L);
    testUser.setUsername("batman");
    testUser.setPassword(("i am"));
    testUser.setToken(testToken);

    // when -> find user by username in repository -> username does not exist
    Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

    // then -> attempt to login user -> wrong username
    User user = authorizationService.login(testUser.getUsername(), testUser.getPassword());

    // then
    assertNull(user);
  }

  /**
   * verify that when user logins and the username does exist, but wrong password, null is returned
   */
  @Test
  public void login_invalidCredentials_WrongPassword() {
    String testToken = "1";

    // given
    User testUser = new User();
    testUser.setUserId(1L);
    testUser.setUsername("batman");
    testUser.setPassword(("i am"));
    testUser.setToken(testToken);

    // when -> find user by username in repository -> username does not exist
    Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(testUser);

    // then -> attempt to login user -> wrong password
    User user = authorizationService.login(testUser.getUsername(), "No i am batman");

    // then
    assertNull(user);
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

  // endregion

  // region isExistingAndAuthorized tests

  /**
   * test that if token and username correspond, true is returned
   */
  @Test
  public void isExistingAndAuthorized_tokenAndUsername_valid() {
    // given valid token
    String token = "batman's token";

    // given no user which can be found by token
    User testUser = new User();
    testUser.setUserId(1L);
    testUser.setToken(token);

    // when -> find user by token -> user is returned
    Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(testUser));

    // then (does not throw exception)
    authorizationService.isExistingAndAuthorized(token, testUser.getUserId());
  }

  /**
   * test that if token cannot be found, false is returned
   */
  @Test
  public void isExistingAndAuthorized_tokenAndUsername_invalidToken() {
    // given no user which can be found by token
    User testUser = new User();
    testUser.setUserId(1L);

    // when -> find user by token -> no user found because token is invalid
    Mockito.when(userRepository.findByToken(Mockito.anyString())).thenReturn(null);

    // then
    assertThrows(ResponseStatusException.class,
        () -> authorizationService.isExistingAndAuthorized("invalid token", testUser.getUserId()));
  }

  /**
   * test that if token and username do not correspond, false is returned
   */
  @Test
  public void isAuthorized_tokenAndUsername_tokenAndUsernameNotCorresponding() {
    // given no user which can be found by token
    User testUser = new User();
    testUser.setToken("valid token");

    // given user that is returned
    User invaliduser = new User();
    invaliduser.setToken("invalid token");
    invaliduser.setUserId(1L);

    // when -> find user by token -> no user found because token is invalid
    Mockito.when(userRepository.findByToken(Mockito.anyString())).thenReturn(invaliduser);

    // then
    assertThrows(ResponseStatusException.class,
        () -> authorizationService.isExistingAndAuthorized("batman's token", 2L));
  }
  // endregion

  // region isAuthorizedAndBelongsToTeam tests

  @Test
  public void isAuthorizedAndBelongsToTeam_validToken_doesBelong_success() {
    // given
    User testUser = new User();
    testUser.setUserId(1L);
    testUser.setToken("batman's token");

    // when -> call to isAuthorized -> mock return
    AuthorizationService tempAuthService = Mockito.spy(authorizationService);
    Mockito.doReturn(testUser).when(tempAuthService).isAuthorized(Mockito.anyString());

    // when -> call to getUsersOfTeam -> user found
    Mockito.when(teamUserService.getUsersOfTeam(Mockito.anyLong()))
        .thenReturn(java.util.List.of(testUser));

    // then (does not throw exception)
    User authUser = tempAuthService.isAuthorizedAndBelongsToTeam("batman's token", 1L);

    assertEquals(authUser.getUserId(), testUser.getUserId());
  }

  @Test
  public void isAuthorizedAndBelongsToTeam_validToken_doesNotBelong_throwsUnauthorized() {
    // given
    User testUser = new User();
    testUser.setUserId(1L);
    testUser.setToken("batman's token");

    // when -> call to isAuthorized -> mock return
    AuthorizationService tempAuthService = Mockito.spy(authorizationService);
    Mockito.doReturn(testUser).when(tempAuthService).isAuthorized(Mockito.anyString());

    // when -> call to getUsersOfTeam -> no user found
    Mockito.when(teamUserService.getUsersOfTeam(Mockito.anyLong())).thenReturn(java.util.List.of());

    // then
    assertThrows(ResponseStatusException.class,
        () -> tempAuthService.isAuthorizedAndBelongsToTeam("batman's token", 1L));
  }

  @Test
  public void isAuthorizedAndBelongsToTeam_validToken_teamDoesNotExist_throwsNotFound() {
    // given
    User testUser = new User();
    testUser.setUserId(1L);
    testUser.setToken("batman's token");

    // when -> call to isAuthorized -> mock return
    AuthorizationService tempAuthService = Mockito.spy(authorizationService);
    Mockito.doReturn(testUser).when(tempAuthService).isAuthorized(Mockito.anyString());

    Mockito.when(teamUserService.getUsersOfTeam(Mockito.anyLong()))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));

    // then -> unauthorized
    assertThrows(ResponseStatusException.class,
        () -> tempAuthService.isAuthorizedAndBelongsToTeam("invalid token", 1L));
  }

  @Test
  public void isAuthorizedAndBelongsToTeam_invalidToken_throwsUnauthorized() {
    // given
    User testUser = new User();
    testUser.setUserId(1L);
    testUser.setToken("batman's token");

    // when -> call to isAuthorized -> mock return
    AuthorizationService tempAuthService = Mockito.spy(authorizationService);
    Mockito.doReturn(testUser).when(tempAuthService).isAuthorized(Mockito.anyString());

    // then -> unauthorized
    assertThrows(ResponseStatusException.class,
        () -> tempAuthService.isAuthorizedAndBelongsToTeam("invalid token", 1L));
  }
  // endregion

  // region isAuthorizedAndBelongsToTeam with userId
  // because we verify (with a spy mock) that the other methods were called, we do not need to test
  // the other methods, because they are already tested
  @Test
  public void isAuthorizedAndBelongsToTeam_validToken_doesBelong_success_withUserId() {
    // given
    User testUser = new User();
    testUser.setUserId(1L);
    testUser.setToken("batman's token");

    // when -> call to isExistingAndAuthorized -> mock return
    AuthorizationService tempAuthService = Mockito.spy(authorizationService);
    Mockito.doReturn(testUser)
        .when(tempAuthService)
        .isExistingAndAuthorized(Mockito.anyString(), Mockito.anyLong());

    // when -> call to isAuthorizedAndBelongsToTeam -> user found
    Mockito.doReturn(testUser)
        .when(tempAuthService)
        .isAuthorizedAndBelongsToTeam(Mockito.anyString(), Mockito.anyLong());

    // then (does not throw exception)
    User authUser = tempAuthService.isAuthorizedAndBelongsToTeam("batman's token", 1L, 1L);

    // verify the other methods were called
    Mockito.verify(tempAuthService, Mockito.times(1))
        .isExistingAndAuthorized(Mockito.anyString(), Mockito.anyLong());
    Mockito.verify(tempAuthService, Mockito.times(1))
        .isAuthorizedAndBelongsToTeam(Mockito.anyString(), Mockito.anyLong());

    assertEquals(authUser.getUserId(), testUser.getUserId());
  }

  // endregion

  // region deprecated
  // /**
  //  * test that if token and userId correspond, true is returned
  //  */
  // @Test
  // public void isAuthorized_tokenAndUserId_valid() {
  //   // given no user which can be found by token
  //   User testUser = new User();
  //   testUser.setUserId(1L);
  //
  //   // when -> find user by token -> user is returned
  //   Mockito.when(userRepository.findByToken(Mockito.anyString())).thenReturn(testUser);
  //
  //   // then (does not throw exception)
  //   authorizationService.isAuthorized("batman's token", 1L);
  // }
  //
  // /**
  //  * test that if token cannot be found, false is returned
  //  */
  // @Test
  // public void isAuthorized_tokenAndUserId_invalidToken() {
  //   // when -> find user by token -> no user found because token is invalid
  //   Mockito.when(userRepository.findByToken(Mockito.anyString())).thenReturn(null);
  //
  //   // then
  //   assertThrows(ResponseStatusException.class,
  //       () -> authorizationService.isAuthorized("invalid token", 1L));
  // }
  //
  // /**
  //  * test that if token and userId do not correspond, false is returned
  //  */
  // @Test
  // public void isAuthorized_tokenAndUserId_tokenAndUserIdNotCorresponding() {
  //   String username = "batman";
  //   // given no user which can be found by token
  //   User testUser = new User();
  //   testUser.setUserId(1L);
  //
  //   // when -> find user by token -> no user found because token is invalid
  //   Mockito.when(userRepository.findByToken(Mockito.anyString())).thenReturn(testUser);
  //
  //   // then
  //   assertThrows(ResponseStatusException.class,
  //       () -> authorizationService.isAuthorized("batman's token", 2L));
  // }
  // endregion
}
