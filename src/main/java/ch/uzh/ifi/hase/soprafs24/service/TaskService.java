package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.TaskStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Task;
import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.repository.TaskRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.service.PusherService; //PUSHER

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
  private final TeamService teamService;
  private final TaskRepository taskRepository;
  private final PusherService pusherService; //PUSHER ADDED

  @Autowired
  public TaskService(@Qualifier("taskRepository") TaskRepository taskRepository,
      @Qualifier("teamService") TeamService teamService, PusherService pusherService) {  //PUSHER ADDED
    this.taskRepository = taskRepository;
    this.teamService = teamService;
    this.pusherService = pusherService; //PUSHER ADDED
  }

  /**
   * Create tasks method.
   *
   * @param newTask to be created
   * @return newTask
   */
  public Task createTask(Task newTask) {
    // check that title and description are not null or empty
    ServiceHelpers.checkValidString(newTask.getTitle(), "title");
    if (newTask.getDescription() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description cannot be null.");
    }
    if (newTask.getTeam() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team cannot be null.");
    }

    // Set the status of the new task to to-do
    newTask.setStatus(TaskStatus.TODO);

    newTask = taskRepository.save(newTask);
    taskRepository.flush();

    log.debug("Created Information for Task: {}", newTask);

    // let other users know a task has been edited
    pusherService.updateTask(newTask.getTeam().getTeamId().toString()); //PUSHER

    return newTask;
  }

  /**
   * Get tasks method.
   *
   * @param teamId for tasks to be taken from
   * @return List of tasks
   */
  public List<Task> getTasksByTeamId(Long teamId) {
    Team team = teamService.getTeamByTeamId(teamId);
    List<Task> tasks = taskRepository.findByTeam(team);
    return tasks; // just return the list as it is, whether it's empty or not
  }

  /**
   * Update task method.
   *
   * @param task to be updated
   * @return updatedTask
   */
  public Task updateTask(Task task, Long teamId) {
    // Reusing the getTask method
    Task existingTask = getTask(task.getTaskId());

    // check that task belongs to the correct team
    if (!existingTask.getTeam().getTeamId().equals(teamId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found.");
    }

    // update allowed fields
    existingTask.setTitle(task.getTitle());
    existingTask.setDescription(task.getDescription());
    existingTask.setStatus(task.getStatus());

    Task updatedTask = taskRepository.save(existingTask);
    taskRepository.flush();

    // Send pusher event
    pusherService.updateTask(updatedTask.getTeam().getTeamId().toString());

    log.debug("Updated Information for Task: {}", updatedTask);
    return updatedTask;
  }

  /**
   * Get a task by its Id.
   *
   * @param taskId Id of the task to be fetched
   * @return Task if found, throws ResponseStatusException otherwise
   */
  public Task getTask(Long taskId) {
    return taskRepository.findById(taskId).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found."));
  }
}
