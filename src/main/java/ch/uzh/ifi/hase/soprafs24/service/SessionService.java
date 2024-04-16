package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Session;
import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.repository.SessionRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Session Service
 * This class is the "worker" and responsible for all functionality related to the session.
 */
@Service
@Transactional
public class SessionService {
  private final Logger log = LoggerFactory.getLogger(SessionService.class);

  private final SessionRepository sessionRepository;

  private final TeamService teamService;

  @Autowired
  public SessionService(SessionRepository sessionRepository, TeamService teamService) {
    this.sessionRepository = sessionRepository;
    this.teamService = teamService;
  }

  /**
   * Create session for a assigned team (already set in session object passed). This is used to
   * start the session. The creation date is automatically set on tuple creation.
   *
   * @param teamId the team id of the team to create the session for
   * @throws ResponseStatusException with status 404 if the team does not exist
   * @return the created session
   */
  public Session createSession(Long teamId) {
    log.debug("Creating session for team with teamId '{}'", teamId);

    // get team (404 if not found)
    Team team = teamService.getTeamByTeamId(teamId);

    // create session
    Session newSession = new Session();
    newSession.setTeam(team);
    newSession.setStartDateTime(LocalDateTime.now());

    // save session in the database
    Session createdSession = sessionRepository.save(newSession);
    sessionRepository.flush();

    log.debug("Created session: {} at {}", createdSession, createdSession.getStartDateTime());
    return createdSession;
  }

  /**
   * Get all sessions that a team has participated in and might still be participating in.
   *
   * @param teamId the team id of the team to get the sessions for
   * @throws ResponseStatusException with status 404 if the team does not exist
   * @return the list sessions of the team
   */
  public List<Session> getSessionsByTeamId(Long teamId) {
    log.debug("Get all sessions for team with teamId '{}'", teamId);

    // get team (404 if not found)
    Team team = teamService.getTeamByTeamId(teamId);

    // get all sessions for the team (more recent first)
    List<Session> sessions = sessionRepository.findByTeamOrderByStartDateTimeDesc(team);

    log.debug("Found {} sessions: {}", sessions.size(), sessions);
    return sessions;
  }

  /**
   * End the active session for a team, if the session is active. If the team has no active session,
   * 410 is thrown.
   *
   * @param teamId the team id of the team to end the session for
   * @throws ResponseStatusException with status 410 if the team has no active session; 404 if team
   *     not found.
   * @return the ended session
   */
  public Session endSession(Long teamId) {
    log.debug("Ending session for team with teamId '{}'", teamId);

    // get all sessions for the team (more recent first)
    List<Session> sessions = getSessionsByTeamId(teamId);

    // check if the team has an active session
    if (sessions.isEmpty() || sessions.get(0).getEndDateTime() != null) {
      log.error("Team with teamId '{}' has no active session", teamId);
      throw new ResponseStatusException(HttpStatus.GONE, "Team has no active session");
    }

    // end the session
    Session activeSession = sessions.get(0);
    activeSession.setEndDateTime(LocalDateTime.now());

    // save session in the database
    Session endedSession = sessionRepository.save(activeSession);
    sessionRepository.flush();

    log.debug("Ended session: {}; started at {}; ended at {}", endedSession,
        endedSession.getStartDateTime(), endedSession.getEndDateTime());
    return endedSession;
  }
}