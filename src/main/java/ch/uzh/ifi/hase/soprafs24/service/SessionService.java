package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Session;
import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.repository.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
   * @return the created session
   */
  public Session createSession(Long teamId) {
    log.debug("Creating session for team with teamId '{}'", teamId);

    // get team (404 if not found)
    Team team = teamService.getTeamByTeamId(teamId);

    // create session
    Session newSession = new Session();
    newSession.setTeam(team);

    // save session in the database
    Session createdSession = sessionRepository.save(newSession);
    sessionRepository.flush();

    log.debug("Created session: {} at {}", createdSession, createdSession.getStartDateTime());
    return createdSession;
  }
}
