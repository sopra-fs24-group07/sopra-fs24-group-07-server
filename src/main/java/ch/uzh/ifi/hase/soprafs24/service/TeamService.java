package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.repository.TeamRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
