package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.repository.TeamRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Team Service
 * This class is the "worker" and responsible for all functionality related to teams (e.g., create,
 * modify, delete, get users, get team tasks). The result will be passed back to the caller.
 */
@Service
@Transactional
public class TeamService {
  private final Logger log = LoggerFactory.getLogger(TeamService.class);

  private final TeamRepository teamRepository;

  @Autowired
  public TeamService(TeamRepository teamRepository) {
    this.teamRepository = teamRepository;
  }

  /**
   * Get team by team id.
   *
   * @param teamId team id
   * @return team with given team id
   * @throws ResponseStatusException 404 if team id not found
   */
  public Team getTeamByTeamId(Long teamId) {
    Team found = teamRepository.findById(teamId).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));

    log.debug("getTeamByTeamId: Found team: {} for team-id {}", found, teamId);
    return found;
  }

  /**
   * Get team by team uuid.
   *
   * @param teamUUID team uuid
   * @return team with given team uuid
   * @throws ResponseStatusException 404 if team uuid not found
   */
  public Team getTeamByTeamUUID(String teamUUID) {
    Team found = teamRepository.findByTeamUUID(teamUUID);

    // check if team exists
    if (found == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found");
    }

    log.debug("getTeamByTeamUUID: Found team: {} for team-uuid {}", found, found.getTeamUUID());
    return found;
  }

  /**
   * Create team method. The only constraint is that the name cannot be empty string. Creates unique
   * id (done in the repo).
   *
   * @param newTeam team to be created
   * @return team created
   */
  public Team createTeam(Team newTeam) {
    // check that name is not empty string
    ServiceHelpers.checkValidString(newTeam.getName(), "Name");

    newTeam.setTeamUUID(UUID.randomUUID().toString());

    // saves the given entity but data is only persisted in the database once
    // flush() is called
    newTeam = teamRepository.save(newTeam);
    teamRepository.flush();

    log.debug("Created Information for Team: {} with id {}", newTeam, newTeam.getTeamId());
    return newTeam;
  }
}
