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
}
