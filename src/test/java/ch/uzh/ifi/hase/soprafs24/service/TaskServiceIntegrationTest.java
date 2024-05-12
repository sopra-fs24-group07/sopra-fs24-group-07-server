package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs24.constant.TaskStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Task;
import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.repository.TaskRepository;
import ch.uzh.ifi.hase.soprafs24.repository.TeamRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
  @MockBean private PusherService pusherService;

  @BeforeEach
  public void setup() {
    taskRepository.deleteAll();
    teamRepository.deleteAll();

    // mock pusher service taskModification method
    Mockito.doNothing().when(pusherService).taskModification(Mockito.anyString());
  }

  // region create task
  /**
   * Test for creating a new task with valid inputs
   */
  @Test
  public void createTask_validInputs_success() {
    // given a team
    Team team = new Team();
    team.setName("Team A");
    team.setDescription("Lorem");
    team.setTeamUUID("team-uuid");
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

  /**
   * Test for creating a new task with empty title (is not successful because title is required)
   */
  @Test
  public void createTask_emptyTitle_throwsException() {
    // given a team
    Team team = new Team();
    team.setName("Team A");
    team.setDescription("Lorem");
    team.setTeamUUID("team-uuid");
    team = teamRepository.saveAndFlush(team);

    // given a task with empty title
    Task testTask = new Task();
    testTask.setTitle("");
    testTask.setDescription("This is task A");
    testTask.setTeam(team);

    // when & then
    assertThrows(ResponseStatusException.class, () -> taskService.createTask(testTask));
  }

  /**
   * Test for creating a new task with empty description (is successfully, because default value ok)
   */
  @Test
  public void createTask_emptyDescription_defaultValue_success() {
    // given a team
    Team team = new Team();
    team.setName("Team A");
    team.setDescription("Lorem");
    team.setTeamUUID("team-uuid");
    team = teamRepository.saveAndFlush(team);

    // given a task with empty description
    Task testTask = new Task();
    testTask.setTitle("Task A");
    testTask.setDescription("");
    testTask.setTeam(team);

    // when & then
    assertDoesNotThrow(() -> taskService.createTask(testTask));
  }

  /**
   * Test for creating a new task with null title
   */
  @Test
  public void createTask_nullTitle_throwsException() {
    // given a team
    Team team = new Team();
    team.setName("Team A");
    team.setDescription("Lorem");
    team.setTeamUUID("team-uuid");
    team = teamRepository.saveAndFlush(team);

    // given a task with null title
    Task testTask = new Task();
    testTask.setTitle(null);
    testTask.setDescription("This is task A");
    testTask.setTeam(team);

    // when & then
    assertThrows(ResponseStatusException.class, () -> taskService.createTask(testTask));
  }

  /**
   * Test for creating a new task with null description
   */
  @Test
  public void createTask_nullDescription_throwsException() {
    // given a team
    Team team = new Team();
    team.setName("Team A");
    team.setDescription("Lorem");
    team.setTeamUUID("team-uuid");
    team = teamRepository.saveAndFlush(team);

    // given a task with null description
    Task testTask = new Task();
    testTask.setTitle("Task A");
    testTask.setDescription(null);
    testTask.setTeam(team);

    // when & then
    assertThrows(ResponseStatusException.class, () -> taskService.createTask(testTask));
  }

  @ParameterizedTest
  @MethodSource("createTaskLengthTests")
  void createTask_testInputLength(String title, String description, boolean shouldThrow) {
    // given user in db to update
    Team team = new Team();
    team.setName("Team A");
    team.setDescription("Lorem");
    team.setTeamUUID("team-uuid");
    team = teamRepository.saveAndFlush(team);

    // given a task
    Task testTask = new Task();
    testTask.setTitle(title);
    testTask.setDescription(description);
    testTask.setTeam(team);

    if (shouldThrow) {
      assertThrows(ResponseStatusException.class, () -> { taskService.createTask(testTask); });
    } else {
      assertDoesNotThrow(() -> { taskService.createTask(testTask); });
    }
  }

  private static Stream<Arguments> createTaskLengthTests() {
    return Stream.of(Arguments.of("task title 123", "description", false), // all ok
        Arguments.of("t".repeat(101), "username", true), // too long title
        Arguments.of("task title 123", "d".repeat(1001), true) // too long desc
    );
  }
  // endregion

  // GET
  @Test
  public void getTasksByTeamId_validInputs_success() {
    // given a team with tasks
    Team team = new Team();
    team.setName("Team A");
    team.setDescription("Lorem");
    team.setTeamUUID("team-uuid"); // set teamUUID
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

  /* get tasks by team is but without deleted tasks */
  @Test
  public void getTasksByTeamId_validInputs_noDeletedTasks_success() {
    // given a team with tasks
    Team team = new Team();
    team.setName("Team A");
    team.setDescription("Lorem");
    team.setTeamUUID("team-uuid"); // set teamUUID
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
    testTask2.setStatus(TaskStatus.DELETED);
    taskRepository.saveAndFlush(testTask2);

    // when
    List<Task> tasks = taskService.getTasksByTeamId(team.getTeamId());

    // then
    assertEquals(1, tasks.size());
    assertEquals(tasks.get(0).getTaskId(), testTask1.getTaskId());
    assertEquals(tasks.get(0).getTitle(), testTask1.getTitle());
    assertEquals(tasks.get(0).getDescription(), testTask1.getDescription());
    assertEquals(tasks.get(0).getStatus(), testTask1.getStatus());
    assertEquals(tasks.get(0).getTeam().getTeamId(), testTask1.getTeam().getTeamId());
  }

  @Test
  public void getTasksByTeamId_invalidTeamId_throwsException() {
    // given a valid team id
    Team validTeam = new Team();
    validTeam.setName("Valid Team");
    validTeam.setDescription("Valid Team Description");
    validTeam.setTeamUUID("valid-team-uuid");
    validTeam = teamRepository.saveAndFlush(validTeam);

    // given an invalid team id
    Long invalidTeamId = validTeam.getTeamId() + 1;

    // when & then
    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class, () -> taskService.getTasksByTeamId(invalidTeamId));
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    assertTrue(exception.getReason().contains("Team not found")); // adjusted assertion
  }

  /**
   * Test for getting tasks by team id and status with valid inputs
   */
  @Test
  public void getTasksByTeamIdAndStatus_validInputs_success() {
    // given a team with tasks
    Team team = new Team();
    team.setName("Team A");
    team.setDescription("Lorem");
    team.setTeamUUID("team-uuid"); // set teamUUID
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
    testTask2.setStatus(TaskStatus.IN_SESSION);
    taskRepository.saveAndFlush(testTask2);

    List<TaskStatus> statusList = new ArrayList<>();
    statusList.add(TaskStatus.TODO);

    // when
    List<Task> tasks = taskService.getTasksByTeamIdAndStatus(team.getTeamId(), statusList);

    // then
    assertEquals(1, tasks.size());
    assertEquals(testTask1.getTaskId(), tasks.get(0).getTaskId());
  }

  /**
   * Test for getting tasks by team id and status with invalid team id
   */
  @Test
  public void getTasksByTeamIdAndStatus_invalidTeamId_throwsException() {
    // given a valid team id
    Team validTeam = new Team();
    validTeam.setName("Valid Team");
    validTeam.setDescription("Valid Team Description");
    validTeam.setTeamUUID("valid-team-uuid");
    validTeam = teamRepository.saveAndFlush(validTeam);

    // given an invalid team id
    Long invalidTeamId = validTeam.getTeamId() + 1;
    List<TaskStatus> statusList = new ArrayList<>();
    statusList.add(TaskStatus.TODO);

    // when & then
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> taskService.getTasksByTeamIdAndStatus(invalidTeamId, statusList));
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    assertTrue(exception.getReason().contains("Team not found")); // adjusted assertion
  }

  // PUT

  // region update task
  /**
   * Test for updating an existing task with valid inputs
   */
  @Test
  public void updateTask_validInputs_success() {
    // given a team
    Team team = new Team();
    team.setName("Team A");
    team.setDescription("Lorem");
    team.setTeamUUID("team-uuid");
    Team savedTeam = teamRepository.saveAndFlush(team);

    // given an existing task
    Task existingTask = new Task();
    existingTask.setTitle("Task A");
    existingTask.setDescription("This is task A");
    existingTask.setTeam(savedTeam);
    existingTask.setStatus(TaskStatus.TODO); // set status
    Task savedTask = taskRepository.saveAndFlush(existingTask);

    // update task
    savedTask.setTitle("Updated Task A");
    savedTask.setDescription("This is updated task A");
    savedTask.setStatus(TaskStatus.IN_SESSION); // update status

    // when
    Task updatedTask = taskService.updateTask(savedTask, savedTeam.getTeamId());

    // then
    assertEquals(savedTask.getTaskId(), updatedTask.getTaskId());
    assertEquals("Updated Task A", updatedTask.getTitle());
    assertEquals("This is updated task A", updatedTask.getDescription());
    assertEquals(TaskStatus.IN_SESSION, updatedTask.getStatus()); // assert status
  }

  /**
   * Test for updating a task with invalid TeamId
   */
  @Test
  public void updateTask_invalidTeamId_throwsException() {
    // given a valid team
    Team validTeam = new Team();
    validTeam.setName("Team A");
    validTeam.setDescription("Lorem");
    validTeam.setTeamUUID("team-uuid");
    Team savedTeam = teamRepository.saveAndFlush(validTeam);

    // given an existing task
    Task existingTask = new Task();
    existingTask.setTitle("Task A");
    existingTask.setDescription("This is task A");
    existingTask.setTeam(savedTeam);
    existingTask.setStatus(TaskStatus.TODO); // set status
    Task savedTask = taskRepository.saveAndFlush(existingTask);

    // given an invalid team id
    Long invalidTeamId = savedTeam.getTeamId() + 1;

    // when & then
    assertThrows(
        ResponseStatusException.class, () -> taskService.updateTask(savedTask, invalidTeamId));
  }

  /**
   * Test for tryinng to update a non-existing task
   */
  @Test
  public void updateTask_nonExistingTask_throwsException() {
    // given a team
    Team team = new Team();
    team.setName("Team A");
    team.setDescription("Lorem");
    team.setTeamUUID("team-uuid");
    Team savedTeam = teamRepository.saveAndFlush(team);

    // given a non-existing task
    Task nonExistingTask = new Task();
    nonExistingTask.setTaskId(99L);
    nonExistingTask.setTitle("Task A");
    nonExistingTask.setDescription("This is task A");
    nonExistingTask.setTeam(savedTeam);

    // when & then
    assertThrows(ResponseStatusException.class,
        () -> taskService.updateTask(nonExistingTask, savedTeam.getTeamId()));
  }

  @ParameterizedTest
  @MethodSource("updateTaskLengthTests")
  void updateTask_testInputLength(String title, String description, boolean shouldThrow) {
    // given a team
    Team team = new Team();
    team.setName("Team A");
    team.setDescription("Lorem");
    team.setTeamUUID("team-uuid");
    Team savedTeam = teamRepository.saveAndFlush(team);

    // given an existing task
    Task existingTask = new Task();
    existingTask.setTitle("Task A");
    existingTask.setDescription("This is task A");
    existingTask.setTeam(savedTeam);
    existingTask.setStatus(TaskStatus.TODO);
    Task savedTask = taskRepository.saveAndFlush(existingTask);

    // update task
    savedTask.setTitle(title);
    savedTask.setDescription(description);
    savedTask.setStatus(TaskStatus.IN_SESSION);

    if (shouldThrow) {
      assertThrows(ResponseStatusException.class,
          () -> { taskService.updateTask(savedTask, team.getTeamId()); });
    } else {
      assertDoesNotThrow(() -> { taskService.updateTask(savedTask, team.getTeamId()); });
    }
  }

  private static Stream<Arguments> updateTaskLengthTests() {
    return Stream.of(Arguments.of("task title 123", "description", false), // all ok
        Arguments.of("t".repeat(101), "username", true), // too long title
        Arguments.of("task title 123", "d".repeat(1001), true) // too long desc
    );
  }
  // endregion
}
