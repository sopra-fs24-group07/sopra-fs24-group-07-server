package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs24.entity.Session;
import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.repository.SessionRepository;
import java.time.LocalDateTime;
import java.util.List;
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
  private Long mockGoalMinutes;
  private Team testTeam;
  private LocalDateTime testStartDateTime;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);

    // time
    testStartDateTime = LocalDateTime.now();
    mockGoalMinutes = 30L;

    // given team
    testTeam = new Team();
    testTeam.setTeamId(1L);
    testTeam.setName("the a-team");
    testTeam.setDescription("There Is No Plan B... They Don't Need One.");

    // given session
    testSession = new Session();
    testSession.setSessionId(1L);
    testSession.setTeam(testTeam);
    testSession.setStartDateTime(testStartDateTime);
    testSession.setGoalMinutes(mockGoalMinutes);

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
    Session createdSession = sessionService.createSession(testTeam.getTeamId(), mockGoalMinutes);
    assertEquals(testSession.getSessionId(), createdSession.getSessionId());
    assertEquals(testSession.getTeam(), createdSession.getTeam());
    assertEquals(testStartDateTime, createdSession.getStartDateTime());
    assertEquals(mockGoalMinutes, createdSession.getGoalMinutes());
  }

  @Test
  public void createSession_validInputs_endedSessions_success() {
    // given test session with end date
    testSession.setEndDateTime(testStartDateTime.plusHours(1));

    // when -> call task service to get the team -> return the dummy testTeam
    Mockito.when(teamService.getTeamByTeamId(Mockito.any())).thenReturn(testTeam);

    // when call to getSessionsByTeamId -> mock return ended session
    SessionService spySessionService = Mockito.spy(sessionService);
    Mockito.doReturn(java.util.List.of(testSession))
        .when(spySessionService)
        .getSessionsByTeamId(Mockito.anyLong());

    // then -> the session is saved successfully
    Session createdSession = spySessionService.createSession(testTeam.getTeamId(), mockGoalMinutes);

    // check if the session is returned
    assertEquals(testSession.getSessionId(), createdSession.getSessionId());
    assertEquals(testSession.getTeam(), createdSession.getTeam());
    assertEquals(testStartDateTime, createdSession.getStartDateTime());
    assertEquals(mockGoalMinutes, createdSession.getGoalMinutes());
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
    assertThrows(ResponseStatusException.class,
        () -> sessionService.createSession(99L, mockGoalMinutes)); // team not found
  }

  /* test if there is already an active session */
  @Test
  public void createSession_activeSession_throwsException() {
    // when -> call task service to get the team -> return the dummy testTeam
    Mockito.when(teamService.getTeamByTeamId(Mockito.any())).thenReturn(testTeam);

    // when call to getSessionsByTeamId -> mock return active session
    SessionService spySessionService = Mockito.spy(sessionService);
    Mockito.doReturn(java.util.List.of(testSession))
        .when(spySessionService)
        .getSessionsByTeamId(Mockito.anyLong());

    // then -> an exception is thrown
    assertThrows(ResponseStatusException.class,
        () -> spySessionService.createSession(testTeam.getTeamId(), mockGoalMinutes)); // active
    // session
  }
  // endregion

  // region getSessionsByTeamId tests

  @Test
  public void getSessionsByTeamId_validInputs_success() {
    // when -> call task service to get the team -> return the dummy testTeam
    Mockito.when(teamService.getTeamByTeamId(Mockito.any())).thenReturn(testTeam);

    // when -> call sessionRepository to get all sessions for the team -> return the dummy
    // testSession
    Mockito.when(sessionRepository.findByTeamOrderByStartDateTimeDesc(Mockito.any()))
        .thenReturn(java.util.List.of(testSession));

    // then -> the session is saved successfully
    List<Session> sessions = sessionService.getSessionsByTeamId(testTeam.getTeamId());

    // check if the session is returned
    assertEquals(1, sessions.size());
    assertEquals(testSession, sessions.get(0));
  }

  @Test
  public void getSessionsByTeamId_noSessions_success() {
    // when -> call task service to get the team -> return the dummy testTeam
    Mockito.when(teamService.getTeamByTeamId(Mockito.any())).thenReturn(testTeam);

    // when -> call sessionRepository to get all sessions for the team -> return an empty list
    Mockito.when(sessionRepository.findByTeamOrderByStartDateTimeDesc(Mockito.any()))
        .thenReturn(java.util.List.of());

    // then -> the session is saved successfully
    List<Session> sessions = sessionService.getSessionsByTeamId(testTeam.getTeamId());

    // check if the session is returned
    assertEquals(0, sessions.size());
  }

  @Test
  public void getSessionsByTeamId_invalidTeam_throwsException() {
    // when -> call task service to get the team -> return 404 because team not found
    Mockito.when(teamService.getTeamByTeamId(Mockito.any()))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));

    // then -> an exception is thrown
    assertThrows(ResponseStatusException.class,
        () -> sessionService.getSessionsByTeamId(99L)); // team not found
  }
  // endregion

  // region endSession tests
  @Test
  public void endSession_validInputs_success() {
    // given test session with no end date
    testSession.setEndDateTime(null);

    // when -> call task service to get the team -> return the dummy testTeam
    Mockito.when(teamService.getTeamByTeamId(Mockito.any())).thenReturn(testTeam);

    // when call to getSessionsByTeamId -> mock return dummy session
    SessionService spySessionService = Mockito.spy(sessionService);
    Mockito.doReturn(java.util.List.of(testSession))
        .when(spySessionService)
        .getSessionsByTeamId(Mockito.anyLong());

    // then -> the session is saved successfully
    Session endedSession = spySessionService.endSession(testTeam.getTeamId());

    // check if the session is returned
    assertEquals(testSession, endedSession);
    assertNotNull(endedSession.getEndDateTime());
  }

  /* test if no active session, only ended sessions */
  @Test
  public void endSession_noActiveSession_throwsException() {
    // given test session with end date
    testSession.setEndDateTime(testStartDateTime.plusHours(1));

    // when -> call task service to get the team -> return the dummy testTeam
    Mockito.when(teamService.getTeamByTeamId(Mockito.any())).thenReturn(testTeam);

    // when call to getSessionsByTeamId -> mock return ended session
    SessionService spySessionService = Mockito.spy(sessionService);
    Mockito.doReturn(java.util.List.of(testSession))
        .when(spySessionService)
        .getSessionsByTeamId(Mockito.anyLong());

    // then -> an exception is thrown
    assertThrows(ResponseStatusException.class,
        () -> spySessionService.endSession(testTeam.getTeamId())); // no active session
  }

  /* test if no sessions for team */
  @Test
  public void endSession_noSession_throwsException() {
    // when -> call task service to get the team -> return the dummy testTeam
    Mockito.when(teamService.getTeamByTeamId(Mockito.any())).thenReturn(testTeam);

    // when call to getSessionsByTeamId -> mock return empty list
    SessionService spySessionService = Mockito.spy(sessionService);
    Mockito.doReturn(java.util.List.of())
        .when(spySessionService)
        .getSessionsByTeamId(Mockito.anyLong());

    // then -> an exception is thrown
    assertThrows(ResponseStatusException.class,
        () -> spySessionService.endSession(testTeam.getTeamId())); // no active session
  }

  /* test if no sessions for team */
  @Test
  public void endSession_invalidTeam_throwsException() {
    // when -> call task service to get the team -> return 404 because team not found
    Mockito.when(teamService.getTeamByTeamId(Mockito.any()))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));

    // then -> an exception is thrown
    assertThrows(ResponseStatusException.class,
        () -> sessionService.endSession(99L)); // team not found
  }

  /*
   * test if session is expired (more than 24 hours since start)
   */
  @Test
  public void endSession_expiredSession_throwsException() {
    // given test session with no end date
    testSession.setEndDateTime(null);
    testSession.setStartDateTime(LocalDateTime.now().minusHours(25));

    // when -> call task service to get the team -> return the dummy testTeam
    Mockito.when(teamService.getTeamByTeamId(Mockito.any())).thenReturn(testTeam);

    // when call to getSessionsByTeamId -> mock return dummy session
    SessionService spySessionService = Mockito.spy(sessionService);
    Mockito.doReturn(java.util.List.of(testSession))
        .when(spySessionService)
        .getSessionsByTeamId(Mockito.anyLong());

    // then -> an exception is thrown
    assertThrows(ResponseStatusException.class,
        () -> spySessionService.endSession(testTeam.getTeamId())); // session expired
  }

  @Test
  public void endExpiredSessions_noExpiredSessions_success() {
    // given test session with no end date and start date 23 hours ago
    testSession.setEndDateTime(null);
    testSession.setStartDateTime(LocalDateTime.now().minusHours(23));
    // when call to getSessionsByTeamId -> mock return dummy session
    SessionService spySessionService = Mockito.spy(sessionService);
    Mockito.doReturn(java.util.List.of(testSession))
        .when(spySessionService)
        .getSessionsByTeamId(Mockito.anyLong());
    // call endExpiredSessions method
    spySessionService.endExpiredSessions();
    // verify that the session is not ended
    assertNull(testSession.getEndDateTime());
  }

  // endregion
}
