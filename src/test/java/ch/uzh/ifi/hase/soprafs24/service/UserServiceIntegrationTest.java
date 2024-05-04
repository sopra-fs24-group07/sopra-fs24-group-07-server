package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs24.constant.TaskStatus;
import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.repository.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
  @Qualifier("teamRepository") @Autowired private TeamRepository teamRepository;
  @Qualifier("taskRepository") @Autowired private TaskRepository taskRepository;
  @Qualifier("commentRepository") @Autowired private CommentRepository commentRepository;

  @Autowired private UserService userService;
  @Autowired private TeamUserService teamUserService;
  @Autowired private TeamService teamService;
  @MockBean private PusherService pusherService;

  @BeforeEach
  public void setup() {
    // delete all links between teams and users, because of error: ERROR: update or delete on table
    // "users" violates foreign key constraint "fk4bpysmsga1jvt3v3tsn8o6hc9" on table "team_user"
    // Foreign-key constraints:
    //     "fk4bpysmsga1jvt3v3tsn8o6hc9" FOREIGN KEY (user_id) REFERENCES users(user_id)
    //     "fkiuwi96twuthgvhnarqj34mnjv" FOREIGN KEY (team_id) REFERENCES team(team_id)
    commentRepository.deleteAll();
    taskRepository.deleteAll();
    teamUserRepository.deleteAll();
    teamRepository.deleteAll();
    userRepository.deleteAll();

    // mock pusher service
    Mockito.doNothing().when(pusherService).updateTeam(Mockito.anyString());
  }

  // region create user

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

  @ParameterizedTest
  @MethodSource("lengthTests")
  void createUser_testInputLength(String name, String username, String psw, boolean shouldThrow) {
    User testUser = new User();
    testUser.setName(name);
    testUser.setUsername(username);
    testUser.setPassword(psw);

    if (shouldThrow) {
      assertThrows(ResponseStatusException.class, () -> { userService.createUser(testUser); });
      assertNull(userRepository.findByUsername(username));
    } else {
      assertDoesNotThrow(() -> { userService.createUser(testUser); });
    }
  }

  private static Stream<Arguments> lengthTests() {
    return Stream.of(
        // name tests
        Arguments.of("some-short-name-123", "username", "password", false), // valid length
        Arguments.of("HSahjMpTGRhgpDnKdkpUKgjjgyKGMYMGzMBpyazeeCkGYhqnGzd", "username", "password",
            true), // exceeds length
        // username tests
        Arguments.of("name", "username", "password", false), // valid length
        Arguments.of("name", "djTGUwYLtQvuQpdAquSvxKkPTBxdHbq", "password", true), // exceeds length
        // psw tests
        Arguments.of("name", "username", "password", false), // valid length
        Arguments.of("name", "username", "HSahjMpTGRhgpDnKdkpUKgjjgyKGMYMGzMBpyazeeCkGYhqnGzd",
            true) // exceeds length
    );
  }

  // endregion

  // region update user

  // Alihan: PUT with valid input, happy path
  @Test
  public void updateUser_validInputs_success() {
    // given user in db to update
    User testUser = new User();
    testUser.setName("testName");
    testUser.setUsername("testUsername");
    testUser.setPassword("1234");
    User createdUser = userService.createUser(testUser);

    // update user
    createdUser.setName("updatedName");
    createdUser.setUsername("updatedUsername");
    createdUser.setPassword("updatedPassword");

    // update service call
    User updatedUser = userService.updateUser(createdUser);

    // check if user is updated
    assertEquals(createdUser.getUserId(), updatedUser.getUserId());
    assertEquals(createdUser.getName(), updatedUser.getName());
    assertEquals(createdUser.getUsername(), updatedUser.getUsername());
  }

  /**
   * Test if the user is updated with valid inputs, but another user with the same username already
   * exists -> throws error
   */
  @Test
  public void updateUser_duplicateUsername_throwsException() {
    String originalTestUsername = "testUsername";

    // given another user in the db with the same username
    User anotherUser = new User();
    anotherUser.setName("anotherName");
    anotherUser.setUsername("UNIQUE_USERNAME");
    anotherUser.setPassword("anotherPassword");
    userService.createUser(anotherUser);

    // given user in db to update
    User testUser = new User();
    testUser.setName(originalTestUsername);
    testUser.setUsername("testUsername");
    testUser.setPassword("1234");
    User createdUser = userService.createUser(testUser);

    // update user
    createdUser.setName("updatedName");
    createdUser.setUsername("UNIQUE_USERNAME");
    createdUser.setPassword("updatedPassword");

    // update service call
    assertThrows(ResponseStatusException.class, () -> userService.updateUser(createdUser));

    // check that username update did not get through
    User found = userRepository.findById(testUser.getUserId()).orElse(null);
    assertNotNull(found);
    assertEquals(originalTestUsername, found.getUsername());
    assertNotEquals(anotherUser.getUsername(), found.getUsername());
  }

  // Alihan: PUT with non-existing user, unhappy path
  @Test
  public void updateUser_nonExistingUser_throwsException() {
    User nonExistingUser = new User();
    nonExistingUser.setUserId(99L);
    nonExistingUser.setName("nonExistingName");
    nonExistingUser.setUsername("nonExistingUsername");
    nonExistingUser.setPassword("nonExistingPassword");

    // check if user really does not exist
    assertTrue(userRepository.findById(99L).isEmpty());

    assertThrows(ResponseStatusException.class, () -> userService.updateUser(nonExistingUser));
  }

  // endregion

  // region delete user

  // Alihan: DELETE as happy path
  @Test
  public void deleteUser_existingUser_success() {
    // given user to delete which is in the db
    User testUser = new User();
    testUser.setName("testName");
    testUser.setUsername("testUsername");
    testUser.setPassword("1234");
    User createdUser = userService.createUser(testUser);

    // execute delete action
    userService.deleteUser(createdUser.getUserId());

    // check if user really does not exist anymore
    assertTrue(userRepository.findById(createdUser.getUserId()).isEmpty());
  }

  // Alihan: DELETE with non-existing user; unhappy path
  @Test
  public void deleteUser_nonExistingUser_throwsException() {
    // check if user really does not exist (need to do because of integration test)
    assertTrue(userRepository.findById(99L).isEmpty());

    // assertion to throw error
    assertThrows(ResponseStatusException.class, () -> userService.deleteUser(99L));
  }

  /* delete user successfully, which also has teams */
  @Test
  public void deleteUser_existingUserWithTeams_success() {
    // given user to delete which is in the db
    User testUser = new User();
    testUser.setName("testName");
    testUser.setUsername("testUsername");
    testUser.setPassword("1234");
    User createdUser = userService.createUser(testUser);

    // given a team
    Team testTeam = new Team();
    testTeam.setName("the A-team");
    testTeam.setDescription("there is no plan B");
    testTeam.setTeamUUID("team-uuid");
    Team createdTeam = teamRepository.saveAndFlush(testTeam);

    // given user is in the team
    TeamUser testTeamUser = new TeamUser(testTeam, testUser);
    teamUserRepository.saveAndFlush(testTeamUser);

    // execute delete action
    userService.deleteUser(createdUser.getUserId());

    // check if user really does not exist anymore
    assertTrue(userRepository.findById(createdUser.getUserId()).isEmpty());
  }

  /* delete user successfully, which also has comments */
  @Test
  public void deleteUser_existingUserWithComments_success() {
    // given user to delete which is in the db
    User testUser = new User();
    testUser.setName("testName");
    testUser.setUsername("testUsername");
    testUser.setPassword("1234");
    User createdUser = userService.createUser(testUser);

    // given team
    Team testTeam = new Team();
    testTeam.setName("the A-team");
    testTeam.setDescription("there is no plan B");
    testTeam.setTeamUUID("team-uuid");
    Team createdTeam = teamRepository.saveAndFlush(testTeam);

    // given task
    Task testTask = new Task();
    testTask.setTitle("testTitle");
    testTask.setDescription("testDescription");
    testTask.setStatus(TaskStatus.TODO);
    testTask.setTeam(createdTeam);
    Task createdTask = taskRepository.saveAndFlush(testTask);

    // given a comment
    Comment testComment = new Comment();
    testComment.setText("testText");
    testComment.setTask(createdTask);
    testComment.setUser(createdUser);
    testComment.setCreationDate(LocalDateTime.now());
    Comment createdComment = commentRepository.saveAndFlush(testComment);

    // execute delete action
    userService.deleteUser(createdUser.getUserId());

    // check if user really does not exist anymore
    assertTrue(userRepository.findById(createdUser.getUserId()).isEmpty());
    assertTrue(commentRepository.findById(createdComment.getCommentId()).isEmpty());
    assertTrue(taskRepository.findById(createdTask.getTaskId()).isPresent());
    assertTrue(teamRepository.findById(createdTeam.getTeamId()).isPresent());
  }
  // endregion
}
