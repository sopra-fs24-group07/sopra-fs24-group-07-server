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
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
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

    teamUser = new TeamUser(team, user);

    entityManager.persist(user);
    entityManager.persist(team);
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
  }
}
