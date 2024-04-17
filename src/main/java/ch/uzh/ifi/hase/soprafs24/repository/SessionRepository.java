package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Session;
import ch.uzh.ifi.hase.soprafs24.entity.Team;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("sessionRepository")
public interface SessionRepository extends JpaRepository<Session, Long> {
  List<Session> findByTeamOrderByStartDateTimeDesc(Team team);

  /* just for verifying ascending order in tests */
  List<Session> findByTeamOrderByStartDateTimeAsc(Team team);
}
