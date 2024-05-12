package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

public class UserServiceTest {
  @Mock private UserRepository userRepository;

  @InjectMocks private UserService userService;
  @Mock private TeamUserService teamUserService;

  private User testUser;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);

    // given
    testUser = new User();
    testUser.setUserId(1L);
    testUser.setName("testName");
    testUser.setUsername("testUsername");
    testUser.setPassword("1234");

    // when -> any object is being save in the userRepository -> return the dummy
    // testUser
    Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
  }

  // region helper findByUsername tests
  // ALIHAN TEST:
  @Test
  public void findByUsername_existingUsername_returnsUser() {
    // given
    User user = new User();
    user.setUserId(1L);
    user.setName("Test User");
    user.setUsername("testUsername");

    // when
    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(user);

    // then
    User foundUser = userRepository.findByUsername(user.getUsername());

    assertEquals(user.getUserId(), foundUser.getUserId());
    assertEquals(user.getName(), foundUser.getName());
    assertEquals(user.getUsername(), foundUser.getUsername());
  }

  // ALIHAN TEST:
  @Test
  public void findByUsername_nonExistingUsername_returnsNull() {
    // given
    String username = "nonExistingUsername";

    // when
    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);

    // then
    User foundUser = userRepository.findByUsername(username);

    assertNull(foundUser);
  }
  // endregion

  // region create user
  @Test
  public void createUser_validInputs_success() {
    // when -> any object is being save in the userRepository -> return the dummy
    // testUser
    User createdUser = userService.createUser(testUser);

    // then
    Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

    assertEquals(testUser.getUserId(), createdUser.getUserId());
    assertEquals(testUser.getName(), createdUser.getName());
    assertEquals(testUser.getUsername(), createdUser.getUsername());
    assertNotNull(createdUser.getToken());
  }

  @Test
  public void createUser_duplicateName_success() {
    // given -> a first user has already been created
    userService.createUser(testUser);

    // when -> no user with same username
    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);

    // then -> attempt to create second user with same user -> check that an error
    User createdUser = userService.createUser(testUser);

    // is thrown
    assertEquals(testUser.getUserId(), createdUser.getUserId());
    assertEquals(testUser.getName(), createdUser.getName());
    assertEquals(testUser.getUsername(), createdUser.getUsername());
    assertNotNull(createdUser.getToken());
  }

  @Test
  public void createUser_duplicateInputs_throwsException() {
    // given -> a first user has already been created
    userService.createUser(testUser);

    // when -> setup additional mocks for UserRepository
    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

    // then -> attempt to create second user with same user -> check that an error
    // is thrown
    assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
  }

  // ALIHAN TEST:
  @Test
  public void createUser_duplicateUsername_throwsException() {
    // given -> a first user has already been created
    userService.createUser(testUser);

    // when -> setup additional mocks for UserRepository
    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

    // then -> attempt to create second user with same user -> check that an error
    // is thrown
    assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
  }
  // endregion

  // region update user
  @Test
  public void updateUser_validInputs_success() {
    // given
    Mockito.when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));

    User updatedUser = new User();
    updatedUser.setUserId(testUser.getUserId());
    updatedUser.setName("updatedName");
    updatedUser.setUsername("updatedUsername");
    updatedUser.setPassword("updatedPassword");

    // when
    User returnedUser = userService.updateUser(updatedUser);

    // then
    Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
    assertEquals(updatedUser.getUserId(), returnedUser.getUserId());
    assertEquals(updatedUser.getName(), returnedUser.getName());
    assertEquals(updatedUser.getUsername(), returnedUser.getUsername());
    assertEquals(updatedUser.getPassword(), returnedUser.getPassword());
  }

  @Test
  public void updateUser_nonExistingUser_throwsException() {
    // given
    User nonExistingUser = new User();
    nonExistingUser.setUserId(2L);
    nonExistingUser.setName("nonExistingName");
    nonExistingUser.setUsername("nonExistingUsername");
    nonExistingUser.setPassword("nonExistingPassword");

    Mockito.when(userRepository.findById(nonExistingUser.getUserId())).thenReturn(Optional.empty());

    // then
    assertThrows(ResponseStatusException.class, () -> userService.updateUser(nonExistingUser));
  }
  @Test
  public void updateUser_invalidInputs_throwsException() {
    // given
    Mockito.when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));

    User updatedUser = new User();
    updatedUser.setUserId(testUser.getUserId());
    updatedUser.setName(""); // invalid field
    updatedUser.setUsername("updatedUsername");
    updatedUser.setPassword("updatedPassword");

    // when
    assertThrows(ResponseStatusException.class, () -> userService.updateUser(updatedUser));

    // then
    Mockito.verify(userRepository, Mockito.times(0)).save(Mockito.any());
  }
  // endregion

  // region delete user

  @Test
  public void deleteUser_existingUser_success() {
    // given
    Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(testUser));

    // when -> get all teams of user and delete all teams of user
    Mockito.when(teamUserService.getTeamsOfUser(Mockito.anyLong())).thenReturn(List.of());

    // when
    userService.deleteUser(testUser.getUserId());

    // then
    Mockito.verify(userRepository, Mockito.times(1)).delete(Mockito.any());
  }

  /* if user has teams */
  @Test
  public void deleteUser_existingUserWithTeams_success() {
    // given
    Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(testUser));

    // when -> get all teams of user and delete all teams of user
    Mockito.when(teamUserService.getTeamsOfUser(Mockito.any())).thenReturn(List.of(new Team()));
    Mockito.when(teamUserService.deleteUserOfTeam(Mockito.any(), Mockito.any())).thenReturn(null);

    // when
    userService.deleteUser(testUser.getUserId());

    // then
    Mockito.verify(userRepository, Mockito.times(1)).delete(Mockito.any());
    Mockito.verify(teamUserService, Mockito.times(1))
        .deleteUserOfTeam(Mockito.any(), Mockito.any());
  }

  @Test
  public void deleteUser_nonExistingUser_throwsException() {
    // given
    Long nonExistingUserId = 2L;
    Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());

    // then
    assertThrows(ResponseStatusException.class, () -> userService.deleteUser(nonExistingUserId));
  }
  // endregion
}
