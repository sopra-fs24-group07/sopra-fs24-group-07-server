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

public class UserServiceTest {
  @Mock private UserRepository userRepository;

  @InjectMocks private UserService userService;

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
}
