package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs24.entity.Session;
import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class SessionServiceTest {
  @Mock private SessionRepository sessionRepository;
  @Mock private TeamService teamService;

  @InjectMocks private SessionService sessionService;

  private Session testSession;
  private Team testTeam;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);

    // given team
    testTeam = new Team();
    testTeam.setTeamId(1L);
    testTeam.setName("the a-team");
    testTeam.setDescription("There Is No Plan B... They Don't Need One.");

    // given session
    testSession = new Session();
    testSession.setSessionId(1L);
    testSession.setTeam(testTeam);

    // when -> any object is being saved in the sessionRepository -> return the dummy testSession
    Mockito.when(sessionRepository.save(Mockito.any())).thenReturn(testSession);
  }

  // region createSession tests
  @Test
  public void createSession_validInputs_success() {
    // when -> any object is being saved in the sessionRepository -> return the dummy testSession
    Mockito.when(sessionRepository.save(Mockito.any())).thenReturn(testSession);

    // when -> call task service to get the team -> return the dummy testTeam
    Mockito.when(teamService.getTeamByTeamId(Mockito.any())).thenReturn(testTeam);

    // then -> the session is saved successfully
    Session createdSession = sessionService.createSession(testTeam.getTeamId());
    assertEquals(testSession.getSessionId(), createdSession.getSessionId());
    assertEquals(testSession.getTeam(), createdSession.getTeam());
  }

  @Test
  public void createSession_invalidInputs_throwsException() {
    // when -> call task service to get the team -> return 404 because team not found
    Mockito.when(teamService.getTeamByTeamId(Mockito.any()))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));

    // when -> an invalid session (missing team) is saved -> an exception is thrown (just to be
    // safe)
    Mockito.when(sessionRepository.save(Mockito.any()))
        .thenThrow(DataIntegrityViolationException.class);

    // then -> an exception is thrown
    assertThrows(
        ResponseStatusException.class, () -> sessionService.createSession(99L)); // team not found
  }
  // endregion
}
