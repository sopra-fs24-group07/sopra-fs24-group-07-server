package ch.uzh.ifi.hase.soprafs24.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.entity.TeamUser;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TeamUserRepositoryIntegrationTest {
  @Autowired private TestEntityManager entityManager;

  @Autowired private TeamUserRepository teamUserRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private TeamRepository teamRepository;

  private User user;
  private Team team;
  private TeamUser teamUser;

  @BeforeEach
  public void setup() {
    // given
    user = new User();
    user.setName("Firstname Lastname");
    user.setUsername("firstname@lastname");
    user.setPassword("1234");
    user.setToken("1");

    team = new Team();
    team.setName("Team Name");
    team.setDescription("Team Description");

    // save team and user
    entityManager.persist(user);
    entityManager.persist(team);
    entityManager.flush();

    // create teamUser link
    teamUser = new TeamUser(team, user);
    entityManager.persist(teamUser);
    entityManager.flush();
  }

  @Test
  public void whenFindByTeam_thenReturnTeamUser() {
    // when
    List<TeamUser> found = teamUserRepository.findByTeam(team);

    // then
    assertEquals(found.size(), 1);
    assertEquals(found.get(0), teamUser);
  }

  @Test
  public void whenFindByUser_thenReturnTeamUser() {
    // when
    List<TeamUser> found = teamUserRepository.findByUser(user);

    // then
    assertEquals(found.size(), 1);
    assertEquals(found.get(0), teamUser);
    assertEquals(found.get(0).getTeam(), team);
    assertEquals(found.get(0).getUser(), user);
    assertNotNull(found.get(0).getTeamUserId());
    assertEquals(found.get(0).getTeamUserId().getTeamId(), team.getTeamId());
    assertEquals(found.get(0).getTeamUserId().getUserId(), user.getUserId());
    assertNotNull(found.get(0).getJoinTimestamp());
  }

  @Test
  public void whenFindById_thenReturnTeamUser() {
    // when
    TeamUser found = teamUserRepository.findById(teamUser.getTeamUserId()).orElse(null);

    // then
    assertNotNull(found.getTeamUserId());
    assertNotNull(found.getJoinTimestamp());
    assertEquals(found.getUser().getUserId(), user.getUserId());
    assertEquals(found.getTeam().getTeamId(), team.getTeamId());
  }

  // region findByUser tests

  /**
   * Test for finding a teamUser by a user that does exist, but has no team
   */
  @Test
  public void whenFindByUser_thenReturnEmptyList() {
    // given user with no team link
    User user2 = new User();
    user2.setName("Alan Turing");
    user2.setUsername("alan@turing");
    user2.setPassword("enigma");
    user2.setToken("2");

    // save user with no team link
    entityManager.persist(user2);
    entityManager.flush();

    // when
    List<TeamUser> found = teamUserRepository.findByUser(user2);

    // then
    assertEquals(found.size(), 0);
  }

  /**
   * Test for finding a teamUser by a user that does exist
   */
  @Test
  public void whenFindByUser_thenReturnTeamUserList() {
    // when
    List<TeamUser> found = teamUserRepository.findByUser(user);

    // then
    assertEquals(found.size(), 1);
    assertEquals(found.get(0), teamUser);
    assertEquals(found.get(0).getTeam(), team);
    assertEquals(found.get(0).getUser(), user);
  }

  // endregion

  // region findByTeam tests

  /**
   * Test for finding a teamUser by a team that does exist, but has no user
   */
  @Test
  public void whenFindByTeam_thenReturnEmptyList() {
    // given user with no user link
    Team team2 = new Team();
    team2.setName("The A-team");
    team2.setDescription("I love it when a plan comes together!");

    // save user with no team link
    entityManager.persist(team2);
    entityManager.flush();

    // when
    List<TeamUser> found = teamUserRepository.findByTeam(team2);

    // then
    assertEquals(found.size(), 0);
  }

  /**
   * Test for finding a teamUser by a team that does exist
   */
  @Test
  public void whenFindByTeam_thenReturnTeamUserList() {
    // when
    List<TeamUser> found = teamUserRepository.findByTeam(team);

    // then
    assertEquals(found.size(), 1);
    assertEquals(found.get(0), teamUser);
    assertEquals(found.get(0).getTeam(), team);
    assertEquals(found.get(0).getUser(), user);
  }
  // endregion
}
