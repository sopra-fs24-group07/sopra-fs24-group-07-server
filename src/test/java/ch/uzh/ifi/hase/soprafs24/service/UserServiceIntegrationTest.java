package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.TeamUserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

/**
 * Test class for the UserResource REST resource.
 *
 * @see UserService
 */
@WebAppConfiguration
@SpringBootTest
public class UserServiceIntegrationTest {
  @Qualifier("userRepository") @Autowired private UserRepository userRepository;
  @Qualifier("teamUserRepository") @Autowired private TeamUserRepository teamUserRepository;

  @Autowired private UserService userService;

  @BeforeEach
  public void setup() {
    // delete all links between teams and users, because of error: ERROR: update or delete on table
    // "users" violates foreign key constraint "fk4bpysmsga1jvt3v3tsn8o6hc9" on table "team_user"
    // Foreign-key constraints:
    //     "fk4bpysmsga1jvt3v3tsn8o6hc9" FOREIGN KEY (user_id) REFERENCES users(user_id)
    //     "fkiuwi96twuthgvhnarqj34mnjv" FOREIGN KEY (team_id) REFERENCES team(team_id)
    teamUserRepository.deleteAll();

    userRepository.deleteAll();
  }

  @Test
  public void createUser_validInputs_success() {
    // given
    assertNull(userRepository.findByUsername("testUsername"));

    User testUser = new User();
    testUser.setName("testName");
    testUser.setUsername("testUsername");
    testUser.setPassword("1234");

    // when
    User createdUser = userService.createUser(testUser);

    // then
    assertEquals(testUser.getUserId(), createdUser.getUserId());
    assertEquals(testUser.getName(), createdUser.getName());
    assertEquals(testUser.getUsername(), createdUser.getUsername());
    assertNotNull(createdUser.getToken());
  }

  @Test
  public void createUser_duplicateUsername_throwsException() {
    assertNull(userRepository.findByUsername("testUsername"));

    User testUser = new User();
    testUser.setName("testName");
    testUser.setUsername("testUsername");
    testUser.setPassword("1234");
    User createdUser = userService.createUser(testUser);

    // attempt to create second user with same username
    User testUser2 = new User();

    // change the name but forget about the username
    testUser2.setName("testName2");
    testUser2.setUsername("testUsername");
    testUser2.setPassword("1234");

    // check that an error is thrown
    assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser2));
  }
  @Test
  public void createUser_duplicateName_successful() {
    assertNull(userRepository.findByUsername("testUsername"));

    User testUser = new User();
    testUser.setName("testName");
    testUser.setUsername("testUsername");
    testUser.setPassword("1234");
    User createdUser = userService.createUser(testUser);

    // attempt to create second user with same username
    User testUser2 = new User();

    // change the name but forget about the username
    testUser2.setName("testName");
    testUser2.setUsername("testUsername2");
    testUser2.setPassword("1234");
    User createdUser2 = userService.createUser(testUser2);

    // then
    assertEquals(testUser.getName(), testUser2.getName()); // same name
    assertNotNull(testUser2.getToken());
    // not same name
    assertNotEquals(testUser.getUsername(), testUser2.getUsername());
  }
}
