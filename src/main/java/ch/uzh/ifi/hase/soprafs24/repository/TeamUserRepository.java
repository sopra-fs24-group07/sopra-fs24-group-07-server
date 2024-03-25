package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.TeamUser;
import ch.uzh.ifi.hase.soprafs24.entity.TeamUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("teamUserRepository")
public interface TeamUserRepository extends JpaRepository<TeamUser, TeamUserId> {
  //   TODO get all users for team
  //   TODO get all teams of user
}
