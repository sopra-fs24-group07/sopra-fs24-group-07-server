package ch.uzh.ifi.hase.soprafs24.repository;

import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs24.entity.Session;
import ch.uzh.ifi.hase.soprafs24.entity.Team;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class SessionRepositoryIntegrationTest {
  @Autowired private TestEntityManager entityManager;

  @Autowired private SessionRepository sessionRepository;
  @Autowired private TeamRepository teamRepository;

  private LocalDateTime testStartDateTime;

  @BeforeEach
  public void setup() {
    sessionRepository.deleteAll();
    teamRepository.deleteAll();

    testStartDateTime = LocalDateTime.now();
  }

  private Team createTeam() {
    Team team = new Team();
    team.setName("A Team");
    team.setDescription("A Team Description");
    team.setTeamUUID("team-uuid");

    entityManager.persist(team);
    entityManager.flush();
    return team;
  }

  @Test
  public void findById_success() {
    // given
    Team team = createTeam();

    // given session
    Session session = new Session();
    session.setTeam(team);
    session.setStartDateTime(testStartDateTime);
    entityManager.persist(session);
    entityManager.flush();

    // when
    Optional<Session> found = sessionRepository.findById(session.getSessionId());

    // then
    assertTrue(found.isPresent());
    assertEquals(session, found.get());
    assertNotNull(found.get().getSessionId());
    assertEquals(testStartDateTime, found.get().getStartDateTime());
    assertEquals(team.getTeamId(), found.get().getTeam().getTeamId());
  }

  @Test
  public void findByTeam_success() {
    // given
    Team team = createTeam();

    // given session
    Session session = new Session();
    session.setTeam(team);
    session.setStartDateTime(testStartDateTime);
    entityManager.persist(session);
    entityManager.flush();

    // when
    List<Session> found = sessionRepository.findByTeam(team);

    // then
    assertEquals(1, found.size());
    assertEquals(session, found.get(0));
    assertEquals(team.getTeamId(), found.get(0).getTeam().getTeamId());
  }
}
