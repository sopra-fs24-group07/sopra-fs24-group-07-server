package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.constant.TaskStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Task;
import ch.uzh.ifi.hase.soprafs24.entity.Team;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("taskRepository")
public interface TaskRepository extends JpaRepository<Task, Long> {
  // find by id already implicitly there

  // might not be needed if already get all tasks of team (client can sort for status)
  // List<Task> findByStatus(TaskStatus status);

  List<Task> findByTeam(Team team);

  /** Select * from TASK where team = team and status <> status */
  List<Task> findByTeamAndStatusNot(Team team, TaskStatus status);

  List<Task> findByTeamAndStatusInOrderByTitleAsc(Team team, List<TaskStatus> status);
}
