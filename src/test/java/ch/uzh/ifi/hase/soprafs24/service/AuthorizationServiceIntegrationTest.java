package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.entity.TeamUser;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.TeamRepository;
import ch.uzh.ifi.hase.soprafs24.repository.TeamUserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

@WebAppConfiguration
@SpringBootTest
public class AuthorizationServiceIntegrationTest {
  @Qualifier("userRepository") @Autowired private UserRepository userRepository;
  @Qualifier("teamUserRepository") @Autowired private TeamUserRepository teamUserRepository;
  @Qualifier("teamRepository") @Autowired private TeamRepository teamRepository;

  @Autowired private AuthorizationService authorizationService;
  @Autowired private UserService userService;
  @Autowired private TeamUserService teamUserService;
  @Autowired private TeamService teamService;

  @BeforeEach
  public void setup() {
    teamUserRepository.deleteAll();
    userRepository.deleteAll();
    teamRepository.deleteAll();
  }

  // region login
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
  // endregion

  // region isAuthorized
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
  // endregion

  // region isExistingAndAuthorized
  /**
   * test if the token is valid and maps to existing user
   */
  @Test
  public void isExistingAndAuthorized_tokenMapsToUser_success() {
    // given
    User testUser = new User();
    testUser.setName("Bruce Wayne");
    testUser.setUsername("batman");
    testUser.setPassword("alfred123");

    // if create user works, is tested in other tests
    User createdUser = userService.createUser(testUser);

    // when try auth -> then exception (need createdUser.getToken/UserId because generated values)
    User authorizedUser = authorizationService.isExistingAndAuthorized(
        createdUser.getToken(), createdUser.getUserId());

    // then
    assertEquals(testUser.getUsername(), authorizedUser.getUsername());
    assertEquals(createdUser.getToken(), authorizedUser.getToken()); // generated value
  }

  /**
   * test if the token is invalid, but user does exist
   */
  @Test
  public void isExistingAndAuthorized_tokenMapsToUser_invalidUserToken() {
    // given
    User testUser = new User();
    testUser.setName("Bruce Wayne");
    testUser.setUsername("batman");
    testUser.setPassword("alfred123");
    testUser.setUserId(1L);

    // if create user works, is tested in other tests
    User createdUser = userService.createUser(testUser);

    // when try auth -> then exception
    assertThrows(ResponseStatusException.class,
        ()
            -> authorizationService.isExistingAndAuthorized(
                "invalid token", createdUser.getUserId()));
  }

  /**
   * test if user does not exist
   */
  @Test
  public void isExistingAndAuthorized_tokenMapsToUser_userDoesNotExist() {
    // given no user exists
    assertTrue(userRepository.findById(1L).isEmpty());

    // when try auth -> then exception
    assertThrows(ResponseStatusException.class,
        () -> authorizationService.isExistingAndAuthorized("token", 1L));
  }
  // endregion

  // region isAuthorizedAndBelongsToTeam
  // integration tests for isAuthorized() call above
  @Test
  public void isAuthorizedAndBelongsToTeam_validToken_doesBelong_success() {
    // given user
    User testUser = new User();
    testUser.setName("Bruce Wayne");
    testUser.setUsername("batman");
    testUser.setPassword("alfred123");
    User createdUser = userService.createUser(testUser);

    // given team
    Team testTeam = new Team();
    testTeam.setName("Justice League");
    testTeam.setDescription("We are the Justice League!");
    Team createdTeam = teamService.createTeam(testTeam);

    // given team user
    teamUserService.createTeamUser(createdTeam.getTeamId(), createdUser.getUserId());

    // when try auth -> success -> returns authUser
    User authorizedUser = authorizationService.isAuthorizedAndBelongsToTeam(
        testUser.getToken(), testTeam.getTeamId());

    // then
    assertEquals(testUser.getUsername(), authorizedUser.getUsername());
    assertEquals(createdUser.getToken(), authorizedUser.getToken()); // generated value
  }
  @Test
  public void isAuthorizedAndBelongsToTeam_invalidToken_doesNotBelong_failure() {
    // given user
    User testUser = new User();
    testUser.setName("Clark Kent");
    testUser.setUsername("superman");
    testUser.setPassword("lois123");
    User createdUser = userService.createUser(testUser);

    // given team
    Team testTeam = new Team();
    testTeam.setName("Justice League");
    testTeam.setDescription("We are the Justice League!");
    Team createdTeam = teamService.createTeam(testTeam);

    // when try auth with invalid token -> failure -> throws ResponseStatusException
    assertThrows(ResponseStatusException.class, () -> {
      authorizationService.isAuthorizedAndBelongsToTeam("invalidToken", createdTeam.getTeamId());
    });
  }

  @Test
  public void isAuthorizedAndBelongsToTeam_validToken_doesNotBelong_failure() {
    // given user
    User testUser = new User();
    testUser.setName("Diana Prince");
    testUser.setUsername("wonderwoman");
    testUser.setPassword("steve123");
    User createdUser = userService.createUser(testUser);

    // given team
    Team testTeam = new Team();
    testTeam.setName("Justice League");
    testTeam.setDescription("We are the Justice League!");
    Team createdTeam = teamService.createTeam(testTeam);

    // when try auth with valid token but user not in team -> failure -> throws
    // ResponseStatusException
    assertThrows(ResponseStatusException.class, () -> {
      authorizationService.isAuthorizedAndBelongsToTeam(
          createdUser.getToken(), createdTeam.getTeamId());
    });
  }
  // endregion
}
