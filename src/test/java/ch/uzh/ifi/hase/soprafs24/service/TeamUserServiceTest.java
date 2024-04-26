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

  @Mock private TeamService teamService;
  @InjectMocks private TeamUserService teamUserService;

  @Mock private PusherService pusherService;

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
    testTeam.setTeamUUID("team-uuid");

    testUser = new User();
    testUser.setUserId(1L);
    testUser.setUsername("batman");
    testUser.setPassword("alfred123");
    testUser.setToken("1");

    testTeamUser = new TeamUser(testTeam, testUser);

    // when -> any object is being save in the teamUserRepository -> return the dummy testTeamUser
    Mockito.when(teamUserRepository.save(Mockito.any())).thenReturn(testTeamUser);

    // when pusher call -> mock
    Mockito.doNothing().when(pusherService).updateTeam(Mockito.anyString());
  }

  // region createTeamUser with teamId tests
  /**
   * Test for creating a new link between an existing user and an existing team
   */
  @Test
  public void createTeamUser_validInputs_success() {
    // when -> try to find teamId/userId in the repository -> return dummy team/user
    Mockito.when(teamService.getTeamByTeamId(Mockito.anyLong())).thenReturn(testTeam);
    Mockito.when(userRepository.findById(Mockito.any()))
        .thenReturn(java.util.Optional.of(testUser));

    // when -> any object is being saved in the teamUserRepository -> return the dummy testTeamUser
    TeamUser createdTeamUser =
        teamUserService.createTeamUser(testTeam.getTeamId(), testUser.getUserId());

    // then
    Mockito.verify(teamUserRepository, Mockito.times(1)).save(Mockito.any());
    Mockito.verify(pusherService, Mockito.times(1)).updateTeam(Mockito.anyString());

    // check that team/user objects are expected
    assertEquals(testTeamUser.getUser(), createdTeamUser.getUser());
    assertEquals(testTeamUser.getTeam(), createdTeamUser.getTeam());

    // check if save is called
    Mockito.verify(teamUserRepository, Mockito.times(1)).save(Mockito.any());
  }

  /**
   * Test to throw an error when the user does not exist
   */
  @Test
  public void createTeamUser_invalidInputs_userDoesNotExist_throwsException() {
    // when -> try to find teamId in the repository -> return dummy team
    Mockito.when(teamService.getTeamByTeamId(Mockito.anyLong())).thenReturn(testTeam);
    // when -> try to find userId in the repository -> no user found
    Mockito.when(userRepository.findById(Mockito.any())).thenReturn(java.util.Optional.empty());

    assertThrows(ResponseStatusException.class,
        () -> teamUserService.createTeamUser(testTeam.getTeamId(), testUser.getUserId()));

    Mockito.verify(teamUserRepository, Mockito.never()).save(Mockito.any());
    Mockito.verify(pusherService, Mockito.never()).updateTeam(Mockito.anyString());
  }

  /**
   * Test to throw an error when the team does not exist
   */
  @Test
  public void createTeamUser_invalidInputs_teamDoesNotExist_throwsException() {
    // when -> try to find teamId in the repository -> no team found
    Mockito.when(teamService.getTeamByTeamId(Mockito.anyLong()))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Team id not found"));
    // when -> try to find userId in the repository -> return dummy user
    Mockito.when(userRepository.findById(Mockito.any()))
        .thenReturn(java.util.Optional.of(testUser));

    assertThrows(ResponseStatusException.class,
        () -> teamUserService.createTeamUser(testTeam.getTeamId(), testUser.getUserId()));

    Mockito.verify(teamUserRepository, Mockito.never()).save(Mockito.any());
    Mockito.verify(pusherService, Mockito.never()).updateTeam(Mockito.anyString());
  }
  // endregion

  // region createTeamUser with teamUUID tests

  /**
   * Test for creating a new link between an existing user and an existing team
   */
  @Test
  public void createTeamUser_validInputs_teamUUID_success() {
    // when -> try to find teamUUID/userId in the repository -> return dummy team/user
    Mockito.when(teamService.getTeamByTeamUUID(Mockito.any())).thenReturn(testTeam);
    TeamUserService tempTeamUserService = Mockito.spy(teamUserService);
    Mockito.doReturn(testTeamUser)
        .when(tempTeamUserService)
        .createTeamUser(Mockito.anyLong(), Mockito.anyLong());

    // when
    TeamUser createdTeamUser =
        tempTeamUserService.createTeamUser(testTeam.getTeamUUID(), testUser.getUserId());

    // then
    assertEquals(createdTeamUser.getTeamUserId(), testTeamUser.getTeamUserId());
    assertEquals(createdTeamUser.getTeam(), testTeamUser.getTeam());
    assertEquals(createdTeamUser.getUser(), testTeamUser.getUser());

    // because mocked other teamUser call, not possible to verify save/pusher
  }

  // endregion

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

    Mockito.verify(teamUserRepository, Mockito.never()).save(Mockito.any());
    Mockito.verify(pusherService, Mockito.never()).updateTeam(Mockito.anyString());
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

  // region get users of team tests

  /**
   * Test for getting all users of a team if team exists but has no linked users
   */
  @Test
  public void getUsersOfTeam_teamExists_noUsers() {
    // when -> try to find teamId in the repository -> return dummy team
    Mockito.when(teamRepository.findById(Mockito.any()))
        .thenReturn(java.util.Optional.of(testTeam));

    // when -> try to find users by team -> return empty list
    Mockito.when(teamUserRepository.findByTeam(Mockito.any())).thenReturn(java.util.List.of());

    // then
    List<User> foundUsers = teamUserService.getUsersOfTeam(testTeam.getTeamId());

    // assert found empty list
    assertEquals(0, foundUsers.size());
  }

  /**
   * Test for getting all users of a team if team exists and has a linked user
   */
  @Test
  public void getUsersOfTeam_teamExists_withUsers() {
    // when -> try to find teamId in the repository -> return dummy team
    Mockito.when(teamRepository.findById(Mockito.any()))
        .thenReturn(java.util.Optional.of(testTeam));

    // when -> try to find users by team -> return list with dummy teamUser
    Mockito.when(teamUserRepository.findByTeam(Mockito.any()))
        .thenReturn(java.util.List.of(testTeamUser));

    // then
    List<User> foundUsers = teamUserService.getUsersOfTeam(testTeam.getTeamId());

    // assert found list with one user
    assertEquals(1, foundUsers.size());
    assertEquals(testUser, foundUsers.get(0));
    assertEquals(testUser.getUserId(), foundUsers.get(0).getUserId());
    assertEquals(testUser.getUsername(), foundUsers.get(0).getUsername());
    assertEquals(testUser.getToken(), foundUsers.get(0).getToken());
  }

  /**
   * Test for getting all users of a team if team exists and has more than one linked user
   */
  @Test
  public void getUsersOfTeam_teamExists_withMultipleUsers() {
    // given second user
    User testUser2 = new User();
    testUser2.setUserId(2L);
    testUser2.setUsername("superman");
    testUser2.setPassword("kryptonite123");
    testUser2.setToken("2");

    // given 2nd link of testTeam
    TeamUser testTeamUser2 = new TeamUser(testTeam, testUser2);

    // when -> try to find teamId in the repository -> return dummy team
    Mockito.when(teamRepository.findById(Mockito.any()))
        .thenReturn(java.util.Optional.of(testTeam));

    // when -> try to find users of team -> return list with dummy teamUser and testTeamUser2
    Mockito.when(teamUserRepository.findByTeam(Mockito.any()))
        .thenReturn(java.util.List.of(testTeamUser, testTeamUser2));

    // then
    List<User> foundUsers = teamUserService.getUsersOfTeam(testTeam.getTeamId());

    // assert found list with two users
    assertEquals(2, foundUsers.size());
    assertEquals(testUser, foundUsers.get(0));
    assertEquals(testUser2, foundUsers.get(1));
  }

  /**
   * Test throwing error if team does not exist
   */
  @Test
  public void getUsersOfTeam_teamDoesNotExist_throwsException() {
    // when -> try to find teamId in the repository -> no team found
    Mockito.when(teamRepository.findById(Mockito.any())).thenReturn(java.util.Optional.empty());

    // expects 404 if team id cannot be found
    assertThrows(
        ResponseStatusException.class, () -> teamUserService.getUsersOfTeam(testTeam.getTeamId()));
  }

  // endregion

  // region delete teamUser
  /* delete successful */
  @Test
  public void deleteUserOfTeam_validInput_successful() {
    // when -> try to find teamId in the repository -> return dummy team
    Mockito.when(teamService.getTeamByTeamId(Mockito.anyLong())).thenReturn(testTeam);
    // when -> try to find userId in the repository -> return dummy user
    Mockito.when(userRepository.findById(Mockito.any()))
        .thenReturn(java.util.Optional.of(testUser));
    // when -> try to find the teamUser link -> return dummy team user
    Mockito.when(teamUserRepository.findByTeamAndUser(Mockito.any(), Mockito.any()))
        .thenReturn(testTeamUser);

    // when -> delete user from team -> return dummy teamUser
    TeamUser deletedTeamUser =
        teamUserService.deleteUserOfTeam(testTeam.getTeamId(), testUser.getUserId());

    // then -> ok
    assertEquals(testTeamUser.getTeamUserId(), deletedTeamUser.getTeamUserId());
    assertEquals(testTeamUser.getUser().getUserId(), deletedTeamUser.getUser().getUserId());
    assertEquals(testTeamUser.getTeam().getTeamId(), deletedTeamUser.getTeam().getTeamId());
    // then -> verify delete not called
    Mockito.verify(teamUserRepository, Mockito.times(1)).deleteById(Mockito.any());
    Mockito.verify(pusherService, Mockito.times(1)).updateTeam(Mockito.anyString());
  }

  /* delete unsuccessful: team user does not exist (should be caught from controller) */
  @Test
  public void deleteUserOfTeam_invalidInput_teamUserDoesNotExist_throwsException() {
    // when -> try to find teamId in the repository -> return dummy team
    Mockito.when(teamService.getTeamByTeamId(Mockito.anyLong())).thenReturn(testTeam);
    // when -> try to find userId in the repository -> return dummy user
    Mockito.when(userRepository.findById(Mockito.any()))
        .thenReturn(java.util.Optional.of(testUser));
    // when -> try to find the teamUser link -> return null
    Mockito.when(teamUserRepository.findByTeamAndUser(Mockito.any(), Mockito.any()))
        .thenReturn(null);

    // then -> expect exception
    assertThrows(ResponseStatusException.class,
        () -> teamUserService.deleteUserOfTeam(testTeam.getTeamId(), testUser.getUserId()));
    // then -> verify delete not called
    Mockito.verify(teamUserRepository, Mockito.never()).deleteById(Mockito.any());
    Mockito.verify(pusherService, Mockito.never()).updateTeam(Mockito.anyString());
  }

  /* delete unsuccessful: team does not exist */
  @Test
  public void deleteUserOfTeam_invalidInput_teamDoesNotExist_throwsException() {
    // when -> try to find teamId in the repository -> return null
    Mockito.when(teamService.getTeamByTeamId(Mockito.anyLong())).thenReturn(null);
    // when -> try to find userId in the repository -> return dummy user
    Mockito.when(userRepository.findById(Mockito.any()))
        .thenReturn(java.util.Optional.of(testUser));
    // when -> try to find the teamUser link -> return null
    // Mockito.when(teamUserRepository.findByTeamAndUser(Mockito.any(),
    // Mockito.any())).thenReturn(null);

    // then -> expect exception
    assertThrows(ResponseStatusException.class,
        () -> teamUserService.deleteUserOfTeam(testTeam.getTeamId(), testUser.getUserId()));
    // then -> verify delete not called
    Mockito.verify(teamUserRepository, Mockito.never()).deleteById(Mockito.any());
    Mockito.verify(pusherService, Mockito.never()).updateTeam(Mockito.anyString());
  }

  /* delete unsuccessful: user does not exist */
  @Test
  public void deleteUserOfTeam_invalidInput_userDoesNotExist_throwsException() {
    // when -> try to find teamId in the repository -> return dummy team
    Mockito.when(teamService.getTeamByTeamId(Mockito.anyLong())).thenReturn(testTeam);
    // when -> try to find userId in the repository -> return null
    Mockito.when(userRepository.findById(Mockito.any())).thenReturn(java.util.Optional.empty());
    // when -> try to find the teamUser link -> return null
    // Mockito.when(teamUserRepository.findByTeamAndUser(Mockito.any(),
    // Mockito.any())).thenReturn(null);

    // then -> expect exception
    assertThrows(ResponseStatusException.class,
        () -> teamUserService.deleteUserOfTeam(testTeam.getTeamId(), testUser.getUserId()));
    // then -> verify delete not called
    Mockito.verify(teamUserRepository, Mockito.never()).deleteById(Mockito.any());
    Mockito.verify(pusherService, Mockito.never()).updateTeam(Mockito.anyString());
  }
  // endregion
}
