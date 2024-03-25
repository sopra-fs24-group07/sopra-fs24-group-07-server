package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.entity.TeamUser;
import ch.uzh.ifi.hase.soprafs24.entity.TeamUserId;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("teamUserRepository")
public interface TeamUserRepository extends JpaRepository<TeamUser, TeamUserId> {
  /**
   * Retrieves all TeamUser entities associated with a specific Team entity.
   *
   * @param team The Team entity for which to retrieve associated TeamUser entities.
   * @return A list of TeamUser entities associated with the given Team entity.
   *
   * Usage:
   * List<TeamUser> teamUsersForTeam = teamUserRepository.findByTeam(team);
   * List<User> usersForTeam = teamUsersForTeam.stream()
   *     .map(TeamUser::getUser)
   *     .collect(Collectors.toList());
   */
  List<TeamUser> findByTeam(Team team);

  /**
   * Retrieves all TeamUser entities associated with a specific User entity.
   *
   * @param user The User entity for which to retrieve associated TeamUser entities.
   * @return A list of TeamUser entities associated with the given User entity.
   *
   * Usage:
   * User user = ...; // Obtain a User entity
   * List<TeamUser> teamUsersForUser = teamUserRepository.findByUser(user);
   * List<Team> teamsForUser = teamUsersForUser.stream()
   *     .map(TeamUser::getTeam)
   *     .collect(Collectors.toList());
   */
  List<TeamUser> findByUser(User user);
}
