package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.TaskStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Task;
import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.repository.TaskRepository;
import ch.uzh.ifi.hase.soprafs24.repository.TeamRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Task Service
 * This class is the "worker" and responsible for all functionality related to
 * the task
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller
 */
@Service
@Transactional
public class TaskService {
  private final Logger log = LoggerFactory.getLogger(TaskService.class);

  private final TaskRepository taskRepository;
  private final TeamService teamService;

  @Autowired
  public TaskService(@Qualifier("taskRepository") TaskRepository taskRepository,
      @Qualifier("teamService") TeamService teamService) {
    this.taskRepository = taskRepository;
    this.teamService = teamService;
  }

  /**
   * Create tasks method.
   *
   * @param Task newTask
   * @return newTask
   */
  public Task createTask(Task newTask) {
    // check that title and description are not null or empty
    if (newTask.getTitle() == null || newTask.getTitle().isEmpty()
        || newTask.getDescription() == null || newTask.getDescription().isEmpty()
        || newTask.getTeam() == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Some needed fields are missing in the task object.");
    }

    // Set the status of the new task to to-do
    newTask.setStatus(TaskStatus.TODO);

    newTask = taskRepository.save(newTask);
    taskRepository.flush();

    log.debug("Created Information for Task: {}", newTask);
    return newTask;
  }
}
