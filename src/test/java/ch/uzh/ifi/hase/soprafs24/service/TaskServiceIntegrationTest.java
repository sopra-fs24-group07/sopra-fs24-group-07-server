package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs24.constant.TaskStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Task;
import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.repository.TaskRepository;
import ch.uzh.ifi.hase.soprafs24.repository.TeamRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

/**
 * Test class for the TaskService
 *
 * @see TaskService
 */
@WebAppConfiguration
@SpringBootTest
public class TaskServiceIntegrationTest {
  @Qualifier("taskRepository") @Autowired private TaskRepository taskRepository;
  @Qualifier("teamRepository") @Autowired private TeamRepository teamRepository;

  @Autowired private TaskService taskService;

  @BeforeEach
  public void setup() {
    taskRepository.deleteAll();
    teamRepository.deleteAll();
  }

  // POST
  @Test
  public void createTask_validInputs_success() {
    // given a team
    Team team = new Team();
    team.setName("Team A");
    team.setDescription("Lorem");
    team = teamRepository.saveAndFlush(team);

    // given a task
    Task testTask = new Task();
    testTask.setTitle("Task A");
    testTask.setDescription("This is task A");
    testTask.setTeam(team);

    // when
    Task createdTask = taskService.createTask(testTask);

    // then
    assertNotNull(createdTask.getTaskId());
    assertEquals(testTask.getTitle(), createdTask.getTitle());
    assertEquals(testTask.getDescription(), createdTask.getDescription());
    assertEquals(testTask.getTeam().getTeamId(), createdTask.getTeam().getTeamId());
  }

  @Test
  public void createTask_emptyTitle_throwsException() {
    // given a team
    Team team = new Team();
    team.setName("Team A");
    team.setDescription("Lorem");
    team = teamRepository.saveAndFlush(team);

    // given a task with empty title
    Task testTask = new Task();
    testTask.setTitle("");
    testTask.setDescription("This is task A");
    testTask.setTeam(team);

    // when & then
    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> taskService.createTask(testTask));
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    assertTrue(
        exception.getReason().contains("Some needed fields are missing in the task object."));
  }

  @Test
  public void createTask_emptyDescription_throwsException() {
    // given a team
    Team team = new Team();
    team.setName("Team A");
    team.setDescription("Lorem");
    team = teamRepository.saveAndFlush(team);

    // given a task with empty description
    Task testTask = new Task();
    testTask.setTitle("Task A");
    testTask.setDescription("");
    testTask.setTeam(team);

    // when & then
    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> taskService.createTask(testTask));
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    assertTrue(
        exception.getReason().contains("Some needed fields are missing in the task object."));
  }

  @Test
  public void createTask_nullTitle_throwsException() {
    // given a team
    Team team = new Team();
    team.setName("Team A");
    team.setDescription("Lorem");
    team = teamRepository.saveAndFlush(team);

    // given a task with null title
    Task testTask = new Task();
    testTask.setTitle(null);
    testTask.setDescription("This is task A");
    testTask.setTeam(team);

    // when & then
    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> taskService.createTask(testTask));
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    assertTrue(
        exception.getReason().contains("Some needed fields are missing in the task object."));
  }

  @Test
  public void createTask_nullDescription_throwsException() {
    // given a team
    Team team = new Team();
    team.setName("Team A");
    team.setDescription("Lorem");
    team = teamRepository.saveAndFlush(team);

    // given a task with null description
    Task testTask = new Task();
    testTask.setTitle("Task A");
    testTask.setDescription(null);
    testTask.setTeam(team);

    // when & then
    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> taskService.createTask(testTask));
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    assertTrue(
        exception.getReason().contains("Some needed fields are missing in the task object."));
  }

  // GET
  @Test
  public void getTasksByTeamId_validInputs_success() {
    // given a team with tasks
    Team team = new Team();
    team.setName("Team A");
    team.setDescription("Lorem");
    team = teamRepository.saveAndFlush(team);

    Task testTask1 = new Task();
    testTask1.setTitle("Task A");
    testTask1.setDescription("This is task A");
    testTask1.setTeam(team);
    testTask1.setStatus(TaskStatus.TODO);
    taskRepository.saveAndFlush(testTask1);

    Task testTask2 = new Task();
    testTask2.setTitle("Task B");
    testTask2.setDescription("This is task B");
    testTask2.setTeam(team);
    testTask2.setStatus(TaskStatus.TODO);
    taskRepository.saveAndFlush(testTask2);

    // when
    List<Task> tasks = taskService.getTasksByTeamId(team.getTeamId());

    // then
    assertEquals(2, tasks.size());
    assertTrue(tasks.stream().anyMatch(task -> task.getTaskId().equals(testTask1.getTaskId())));
    assertTrue(tasks.stream().anyMatch(task -> task.getTaskId().equals(testTask2.getTaskId())));
  }

  @Test
  public void getTasksByTeamId_invalidTeamId_throwsException() {
    // given an invalid team id
    Long invalidTeamId = 999L;

    // when & then
    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class, () -> taskService.getTasksByTeamId(invalidTeamId));
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    assertTrue(exception.getReason().contains("Team not found with id " + invalidTeamId));
  }
}
