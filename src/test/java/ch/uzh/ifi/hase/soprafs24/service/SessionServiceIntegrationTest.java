package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs24.entity.Session;
import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.repository.SessionRepository;
import ch.uzh.ifi.hase.soprafs24.repository.TeamRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

@WebAppConfiguration
@SpringBootTest
public class SessionServiceIntegrationTest {
  @Qualifier("sessionRepository") @Autowired private SessionRepository sessionRepository;
  @Qualifier("teamRepository") @Autowired private TeamRepository teamRepository;

  @Autowired private SessionService sessionService;
  @Autowired private TeamService teamService;

  @BeforeEach
  public void setup() {
    sessionRepository.deleteAll();
    teamRepository.deleteAll();
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
    Session createdSession = sessionService.createSession(testTeam.getTeamId());

    // then
    assertNotNull(createdSession.getSessionId());
    assertNotNull(createdSession.getStartDateTime());
    assertNull(createdSession.getEndDateTime());
    assertEquals(createdSession.getTeam().getTeamId(), testTeam.getTeamId());
  }
  // endregion
}
