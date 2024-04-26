package ch.uzh.ifi.hase.soprafs24.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.uzh.ifi.hase.soprafs24.entity.Team;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TeamRepositoryIntegrationTest {
  @Autowired private TestEntityManager entityManager;

  @Autowired private TeamRepository teamRepository;

  @Test
  public void findByTeamId_success() {
    // given
    Team team = new Team();
    team.setName("Team Name");
    team.setDescription("Team Description");
    team.setTeamUUID("team-uuid");

    entityManager.persist(team);
    entityManager.flush();

    // when
    Optional<Team> found = teamRepository.findById(team.getTeamId());

    // then
    assertNotNull(found.get().getTeamId());
    assertEquals(found.get().getName(), team.getName());
    assertEquals(found.get().getDescription(), team.getDescription());
    assertEquals(found.get().getTeamUUID(), team.getTeamUUID());
  }
}
