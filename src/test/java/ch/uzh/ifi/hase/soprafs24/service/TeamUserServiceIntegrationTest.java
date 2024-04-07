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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Test class for using the TeamUserResource REST resource.
 *
 * @see TeamUserService
 */
@WebAppConfiguration
@SpringBootTest
public class TeamUserServiceIntegrationTest {
  @Qualifier("teamUserRepository") @Autowired private TeamUserRepository teamUserRepository;
  @Qualifier("teamRepository") @Autowired private TeamRepository teamRepository;
  @Qualifier("userRepository") @Autowired private UserRepository userRepository;

  @Autowired private TeamUserService teamUserService;

  @BeforeEach
  public void setup() {
    teamUserRepository.deleteAll();
    teamRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  public void createTeamUser_validInputs_success() {
    // assume create team and create user work correctly (tested in TeamServiceIntegrationTest and
    // UserServiceIntegrationTest)

    // given team
    Team testTeam = new Team();
    testTeam.setName("productiviTeam");
    testTeam.setDescription("We are a productive team!");
    teamRepository.saveAndFlush(testTeam);
    // given user
    User testUser = new User();
    testUser.setUsername("batman");
    testUser.setName("Bruce Wayne");
    testUser.setPassword("alfred123");
    testUser.setToken("1");
    userRepository.saveAndFlush(testUser);

    // when
    TeamUser createdTeamUser =
        teamUserService.createTeamUser(testTeam.getTeamId(), testUser.getUserId());

    // then
    // assertNotNull(createdTeamUser.getCreationTimestamp()); // set date
    assertEquals(testTeam.getTeamId(), createdTeamUser.getTeam().getTeamId());
    assertEquals(testUser.getUserId(), createdTeamUser.getUser().getUserId());
    // assertEquals(testTeam.getTeamId(), createdTeamUser.getTeamUserId().getTeamId());
    // assertEquals(testUser.getUserId(), createdTeamUser.getTeamUserId().getUserId());
  }

  // region get teams by user tests
  // Don't need to test if user does not exist (done in TeamUserServiceTest)

  /**
   * Test for getting all teams of a user with no link to another team
   */
  @Test
  public void getTeamsByUser_noTeams_emptyList() {
    // given user with no link to any team
    User testUser = new User();
    testUser.setUsername("batman");
    testUser.setName("Bruce Wayne");
    testUser.setPassword("alfred123");
    testUser.setToken("1");
    userRepository.saveAndFlush(testUser);

    // when
    List<Team> teams = teamUserService.getTeamsOfUser(testUser.getUserId());

    // then
    assertTrue(teams.isEmpty());
  }

  /**
   * Test for getting all teams of a user with one link to a team
   */
  @Test
  public void getTeamsByUser_oneTeam_oneTeam() {
    // assume create team and create user work correctly (tested in TeamServiceIntegrationTest and
    // UserServiceIntegrationTest)

    // given team
    Team testTeam = new Team();
    testTeam.setName("productiviTeam");
    testTeam.setDescription("We are a productive team!");
    teamRepository.saveAndFlush(testTeam);

    // given user
    User testUser = new User();
    testUser.setUsername("batman");
    testUser.setName("Bruce Wayne");
    testUser.setPassword("alfred123");
    testUser.setToken("1");
    userRepository.saveAndFlush(testUser);

    // given teamUser link (if this works, is tested in another test, focus here is on
    // getTeamsOfUser)
    TeamUser testTeamUser = new TeamUser(testTeam, testUser);
    teamUserRepository.saveAndFlush(testTeamUser);

    // when
    List<Team> teams = teamUserService.getTeamsOfUser(testUser.getUserId());

    // then
    assertEquals(1, teams.size());
    assertEquals(testTeam.getTeamId(), teams.get(0).getTeamId());
    assertEquals(testTeam.getName(), teams.get(0).getName());
    assertEquals(testTeam.getDescription(), teams.get(0).getDescription());
  }
  // endregion

  // region get users by team tests
  // Don't need to test if team does not exist (done in TeamUserServiceTest)

  /**
   * Test for getting all users of a team with no link to another user
   */
  @Test
  public void getUsersByTeam_noUsers_emptyList() {
    // given team with no link to any user
    Team testTeam = new Team();
    testTeam.setName("productiviTeam");
    testTeam.setDescription("We are a productive team!");
    teamRepository.saveAndFlush(testTeam);

    // when
    List<User> users = teamUserService.getUsersOfTeam(testTeam.getTeamId());

    // then
    assertTrue(users.isEmpty());
  }

  /**
   * Test for getting all users of a team with one link to a user
   */
  @Test
  public void getUsersByTeam_oneUser() {
    // given team
    Team testTeam = new Team();
    testTeam.setName("productiviTeam");
    testTeam.setDescription("We are a productive team!");
    teamRepository.saveAndFlush(testTeam);

    // given user
    User testUser = new User();
    testUser.setUsername("batman");
    testUser.setName("Bruce Wayne");
    testUser.setPassword("alfred123");
    testUser.setToken("1");
    userRepository.saveAndFlush(testUser);

    // given teamUser link (if this works, is tested in another test, focus here is on
    // getTeamsOfUser)
    TeamUser testTeamUser = new TeamUser(testTeam, testUser);
    teamUserRepository.saveAndFlush(testTeamUser);

    // when
    List<User> users = teamUserService.getUsersOfTeam(testTeam.getTeamId());

    // then
    assertEquals(1, users.size());
    assertEquals(testUser.getUserId(), users.get(0).getUserId());
    assertEquals(testUser.getUsername(), users.get(0).getUsername());
    assertEquals(testUser.getName(), users.get(0).getName());
  }

  // endregion
}
