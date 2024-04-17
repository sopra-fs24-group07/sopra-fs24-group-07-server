package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs24.entity.Session;
import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.repository.SessionRepository;
import ch.uzh.ifi.hase.soprafs24.repository.TeamRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

@WebAppConfiguration
@SpringBootTest
public class SessionServiceIntegrationTest {
  @Qualifier("sessionRepository") @Autowired private SessionRepository sessionRepository;
  @Qualifier("teamRepository") @Autowired private TeamRepository teamRepository;

  @Autowired private SessionService sessionService;
  @Autowired private TeamService teamService;

  private Long mockGoalMinutes;

  @BeforeEach
  public void setup() {
    sessionRepository.deleteAll();
    teamRepository.deleteAll();
    mockGoalMinutes = 30L;
  }

  @AfterEach
  public void teardown() {
    sessionRepository.deleteAll();
    teamRepository.deleteAll();
  }

  // region createSession tests
  @Test
  public void createSession_validInputs_success() {
    // given
    Team testTeam = new Team();
    testTeam.setName("productiviTeam");
    testTeam.setDescription("We are a productive team!");
    testTeam.setTeamUUID("team-uuid");
    teamRepository.saveAndFlush(testTeam);

    // when
    Session createdSession = sessionService.createSession(testTeam.getTeamId(), mockGoalMinutes);

    // then
    assertNotNull(createdSession.getSessionId());
    assertNotNull(createdSession.getStartDateTime());
    assertEquals(mockGoalMinutes, createdSession.getGoalMinutes());
    assertNull(createdSession.getEndDateTime());
    assertEquals(mockGoalMinutes, createdSession.getGoalMinutes());
    assertEquals(createdSession.getTeam().getTeamId(), testTeam.getTeamId());
  }

  @Test
  public void createSession_validInputs_existingActiveSession_expectsError() {
    // given
    Team testTeam = new Team();
    testTeam.setName("productiviTeam");
    testTeam.setDescription("We are a productive team!");
    testTeam.setTeamUUID("team-uuid");
    teamRepository.saveAndFlush(testTeam);

    // given active session
    Session testSession = new Session();
    testSession.setTeam(testTeam);
    testSession.setStartDateTime(LocalDateTime.now());
    testSession.setGoalMinutes(mockGoalMinutes);
    sessionRepository.saveAndFlush(testSession);

    // when
    assertThrows(ResponseStatusException.class,
        () -> sessionService.createSession(testTeam.getTeamId(), mockGoalMinutes));
  }

  // endregion

  // region getSessionsByTeam tests
  @Test
  public void getSessionsByTeam_validInputs_success() {
    // given
    Team testTeam = new Team();
    testTeam.setName("productiviTeam");
    testTeam.setDescription("We are a productive team!");
    testTeam.setTeamUUID("team-uuid");
    teamRepository.saveAndFlush(testTeam);

    Session testSession = new Session();
    testSession.setTeam(testTeam);
    testSession.setStartDateTime(LocalDateTime.now());
    testSession.setGoalMinutes(mockGoalMinutes);
    sessionRepository.saveAndFlush(testSession);

    // when
    List<Session> createdSession = sessionService.getSessionsByTeamId(testTeam.getTeamId());

    // then
    assertEquals(1, createdSession.size());
    assertEquals(testSession.getSessionId(), createdSession.get(0).getSessionId());
    assertEquals(testSession.getTeam().getTeamId(), createdSession.get(0).getTeam().getTeamId());
    assertEquals(
        testSession.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
        createdSession.get(0).getStartDateTime().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    assertEquals(mockGoalMinutes, createdSession.get(0).getGoalMinutes());
    assertNull(createdSession.get(0).getEndDateTime());
  }
  @Test
  public void getSessionsByTeam_teamNoSessions_expectEmpty() {
    // given
    Team testTeam = new Team();
    testTeam.setName("productiviTeam");
    testTeam.setDescription("We are a productive team!");
    testTeam.setTeamUUID("team-uuid");
    teamRepository.saveAndFlush(testTeam);

    // when
    List<Session> createdSession = sessionService.getSessionsByTeamId(testTeam.getTeamId());

    // then
    assertEquals(0, createdSession.size());
  }

  @Test
  public void getSessionsByTeam_noExistingTeam() {
    // given
    Team testTeam = new Team();
    testTeam.setName("productiviTeam");
    testTeam.setDescription("We are a productive team!");
    testTeam.setTeamUUID("team-uuid");
    teamRepository.saveAndFlush(testTeam);

    Session testSession = new Session();
    testSession.setTeam(testTeam);
    testSession.setStartDateTime(LocalDateTime.now());
    testSession.setGoalMinutes(mockGoalMinutes);
    sessionRepository.saveAndFlush(testSession);

    // when (have something stored, but not that team)
    assertThrows(ResponseStatusException.class, () -> sessionService.getSessionsByTeamId(99L));
  }

  /* Test if the descending order works */
  @Test
  public void getSessionsByTeamId_desc_success() {
    // given
    Team testTeam = new Team();
    testTeam.setName("productiviTeam");
    testTeam.setDescription("We are a productive team!");
    testTeam.setTeamUUID("team-uuid");
    teamRepository.saveAndFlush(testTeam);

    // given session more recent (at pos 0)
    Session testSession = new Session();
    testSession.setTeam(testTeam);
    testSession.setStartDateTime(LocalDateTime.now());
    testSession.setGoalMinutes(mockGoalMinutes);
    sessionRepository.saveAndFlush(testSession);

    // given session less recent (at pos 1)
    Session testSession2 = new Session();
    testSession2.setTeam(testTeam);
    testSession2.setStartDateTime(LocalDateTime.now().minusHours(1));
    testSession2.setGoalMinutes(mockGoalMinutes);
    sessionRepository.saveAndFlush(testSession2);

    // when
    List<Session> found = sessionService.getSessionsByTeamId(testTeam.getTeamId());

    // then (only test ordering, other characteristics are tested in other tests)
    assertEquals(2, found.size());
    assertEquals(testSession.getSessionId(), found.get(0).getSessionId());
    assertEquals(testSession2.getSessionId(), found.get(1).getSessionId());
  }
  // endregion
}
