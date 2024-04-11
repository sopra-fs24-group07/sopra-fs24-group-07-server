package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.entity.TeamUser;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.TeamRepository;
import ch.uzh.ifi.hase.soprafs24.repository.TeamUserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Team Service
 */
@Service
@Transactional
public class TeamUserService {
  private final Logger log = LoggerFactory.getLogger(TeamUserService.class);

  private final UserRepository userRepository;
  private final TeamRepository teamRepository;
  private final TeamUserRepository teamUserRepository;
  private final TeamService teamService;

  @Autowired
  public TeamUserService(@Qualifier("userRepository") UserRepository userRepository,
      @Qualifier("teamRepository") TeamRepository teamRepository,
      @Qualifier("teamUserRepository") TeamUserRepository teamUserRepository,
      @Qualifier("teamService") TeamService teamService) {
    this.userRepository = userRepository;
    this.teamRepository = teamRepository;
    this.teamUserRepository = teamUserRepository;
    this.teamService = teamService;
  }

  /**
   * Create a new link between an existing user (identified with userId) and an existing team
   * (identified with teamId)
   *
   * @param userId user id
   * @param teamId team id
   * @return the created teamUser (for testing)
   * @throws ResponseStatusException 404 if user or team not found
   */
  public TeamUser createTeamUser(Long teamId, Long userId) {
    log.debug("create link between teamId: " + teamId + " and userId: " + userId);
    // check that the user and team exist
    User user = userRepository.findById(userId).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    // todo call team service method
    Team team = teamRepository.findById(teamId).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));

    // create link (join timestamp will be set automatically)
    TeamUser teamUser = new TeamUser(team, user);

    // save and flush
    teamUserRepository.save(teamUser);
    teamUserRepository.flush();

    log.debug("Created Information for TeamUser: {}", teamUser);

    return teamUser;
  }

  /**
   * Create a new link between an existing user (identified with userId) and an existing team
   * (identified with teamUUID). This is the case if a user accepts the invitation to a team.
   *
   * @param teamUUID team uuid
   * @param userId user id
   * @return the created teamUser (for testing)
   * @throws ResponseStatusException 404 if user or team not found
   */
  public TeamUser createTeamUser(String teamUUID, Long userId) {
    log.debug("create link between teamUUID: " + teamUUID + " and userId: " + userId);

    // create link (join timestamp will be set automatically)
    return this.createTeamUser(teamService.getTeamByTeamUUID(teamUUID).getTeamId(), userId);
  }

  /**
   * Get all teams of a user.
   *
   * @param userId user id of which the teams should be retrieved
   *
   * @return list of teams (empty if no teams are found)
   * @throws ResponseStatusException 404 if user not found
   */
  public List<Team> getTeamsOfUser(Long userId) {
    // check that the user exists
    User user = userRepository.findById(userId).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    // get all teams of the user
    List<TeamUser> teamUsers = teamUserRepository.findByUser(user);
    List<Team> teams = teamUsers.stream().map(TeamUser::getTeam).collect(Collectors.toList());

    log.debug("Found {} teams for user '{}'", teams.size(), user.getUsername());

    return teams;
  }

  /**
   * Get all users of a team
   *
   * @param teamId team id of which the users should be retrieved
   *
   * @return list of users (empty if no users are found)
   * @throws ResponseStatusException 404 if team not found
   */
  public List<User> getUsersOfTeam(Long teamId) {
    // check that the team exists
    Team team = teamRepository.findById(teamId).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));

    // get all users of the team
    List<TeamUser> teamUsers = teamUserRepository.findByTeam(team);
    List<User> users = teamUsers.stream().map(TeamUser::getUser).collect(Collectors.toList());

    log.debug("Found {} users for team '{}'", users.size(), team.getName());

    return users;
  }
}
