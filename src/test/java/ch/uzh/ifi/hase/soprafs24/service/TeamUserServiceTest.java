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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class TeamUserServiceTest {
  @Mock private TeamUserRepository teamUserRepository;
  @Mock private TeamRepository teamRepository;
  @Mock private UserRepository userRepository;

  @InjectMocks private TeamUserService teamUserService;

  private Team testTeam;
  private User testUser;
  private TeamUser testTeamUser;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);

    // given
    testTeam = new Team();
    testTeam.setTeamId(1L);
    testTeam.setName("productiviTeam");
    testTeam.setDescription("We are a productive team!");

    testUser = new User();
    testUser.setUserId(1L);
    testUser.setUsername("batman");
    testUser.setPassword("alfred123");
    testUser.setToken("1");

    testTeamUser = new TeamUser(testTeam, testUser);

    // when -> any object is being save in the teamUserRepository -> return the dummy testTeamUser
    Mockito.when(teamUserRepository.save(Mockito.any())).thenReturn(testTeamUser);
  }

  /**
   * Test for creating a new link between an existing user and an existing team
   */
  @Test
  public void createTeamUser_validInputs_success() {
    // when -> try to find teamId/userId in the repository -> return dummy team/user
    Mockito.when(teamRepository.findById(Mockito.any()))
        .thenReturn(java.util.Optional.of(testTeam));
    Mockito.when(userRepository.findById(Mockito.any()))
        .thenReturn(java.util.Optional.of(testUser));

    // when -> any object is being saved in the teamUserRepository -> return the dummy testTeamUser
    TeamUser createdTeamUser =
        teamUserService.createTeamUser(testTeam.getTeamId(), testUser.getUserId());

    // then
    Mockito.verify(teamUserRepository, Mockito.times(1)).save(Mockito.any());

    // check that team/user objects are expected
    assertEquals(testTeamUser.getUser(), createdTeamUser.getUser());
    assertEquals(testTeamUser.getTeam(), createdTeamUser.getTeam());
  }

  /**
   * Test to throw an error when the user does not exist
   */
  @Test
  public void createTeamUser_invalidInputs_userDoesNotExist_throwsException() {
    // when -> try to find teamId in the repository -> return dummy team
    Mockito.when(teamRepository.findById(Mockito.any()))
        .thenReturn(java.util.Optional.of(testTeam));
    // when -> try to find userId in the repository -> no user found
    Mockito.when(userRepository.findById(Mockito.any())).thenReturn(java.util.Optional.empty());

    assertThrows(ResponseStatusException.class,
        () -> teamUserService.createTeamUser(testTeam.getTeamId(), testUser.getUserId()));
  }

  /**
   * Test to throw an error when the team does not exist
   */
  @Test
  public void createTeamUser_invalidInputs_teamDoesNotExist_throwsException() {
    // when -> try to find teamId in the repository -> no team found
    Mockito.when(teamRepository.findById(Mockito.any())).thenReturn(java.util.Optional.empty());
    // when -> try to find userId in the repository -> return dummy user
    Mockito.when(userRepository.findById(Mockito.any()))
        .thenReturn(java.util.Optional.of(testUser));

    assertThrows(ResponseStatusException.class,
        () -> teamUserService.createTeamUser(testTeam.getTeamId(), testUser.getUserId()));
  }
}
