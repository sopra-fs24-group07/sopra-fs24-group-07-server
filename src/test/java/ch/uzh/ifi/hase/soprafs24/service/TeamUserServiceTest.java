package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.entity.TeamUser;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.TeamRepository;
import ch.uzh.ifi.hase.soprafs24.repository.TeamUserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import java.util.List;
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

  // region get teams of user tests

  /**
   * Test for getting all teams of a user if user exists but has no linked teams
   */
  @Test
  public void getTeamsOfUser_userExists_noTeams() {
    // when -> try to find userId in the repository -> return dummy user
    Mockito.when(userRepository.findById(Mockito.any()))
        .thenReturn(java.util.Optional.of(testUser));

    // when -> try to find teams by user -> return empty list
    Mockito.when(teamUserRepository.findByUser(Mockito.any())).thenReturn(java.util.List.of());

    // then
    List<Team> foundTeams = teamUserService.getTeamsOfUser(testUser.getUserId());

    // assert found empty list
    assertEquals(0, foundTeams.size());
  }

  /**
   * Test for getting all teams of a user if user exists and has a linked team
   */
  @Test
  public void getTeamsOfUser_userExists_withTeams() {
    // when -> try to find userId in the repository -> return dummy user
    Mockito.when(userRepository.findById(Mockito.any()))
        .thenReturn(java.util.Optional.of(testUser));

    // when -> try to find teams by user -> return list with dummy teamUser
    Mockito.when(teamUserRepository.findByUser(Mockito.any()))
        .thenReturn(java.util.List.of(testTeamUser));

    // then
    List<Team> foundTeams = teamUserService.getTeamsOfUser(testUser.getUserId());

    // assert found list with one team
    assertEquals(1, foundTeams.size());
    assertEquals(testTeam, foundTeams.get(0));
    assertEquals(testTeam.getTeamId(), foundTeams.get(0).getTeamId());
  }

  /**
   * Test for getting all teams of a user if user exists and has more than one linked team
   */
  @Test
  public void getTeamsOfUser_userExists_withMultipleTeams() {
    // when -> try to find userId in the repository -> return dummy user
    Mockito.when(userRepository.findById(Mockito.any()))
        .thenReturn(java.util.Optional.of(testUser));

    // given 2nd link of testUser
    Team testTeam2 = new Team();
    testTeam2.setTeamId(2L);
    testTeam2.setName("productiveTeam2");
    testTeam2.setDescription("We are a productive team!");
    TeamUser testTeamUser2 = new TeamUser(testTeam2, testUser);
    // when -> try to find userId in the repository -> return dummy teamUser list (testTeamUser and
    // testTeamUser2)
    Mockito.when(teamUserRepository.findByUser(Mockito.any()))
        .thenReturn(java.util.List.of(testTeamUser, testTeamUser2));

    // then
    List<Team> foundTeams = teamUserService.getTeamsOfUser(testUser.getUserId());

    // assert found list with two teams
    assertEquals(2, foundTeams.size());
    assertEquals(testTeam, foundTeams.get(0));
    assertEquals(testTeam2, foundTeams.get(1));
  }

  /**
   * Test throwing error if user does not exist
   */
  @Test
  public void getTeamsOfUser_userDoesNotExist_throwsException() {
    // when -> try to find userId in the repository -> no user found
    Mockito.when(userRepository.findById(Mockito.any())).thenReturn(java.util.Optional.empty());

    // expects 404 if user id cannot be found
    assertThrows(
        ResponseStatusException.class, () -> teamUserService.getTeamsOfUser(testUser.getUserId()));
  }
  // endregion
}
