package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs24.constant.TaskStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Task;
import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.repository.TaskRepository;
import ch.uzh.ifi.hase.soprafs24.repository.TeamRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class TaskServiceTest {
  @Mock private TaskRepository taskRepository;
  @Mock private TeamService teamService;

  @InjectMocks private TaskService taskService;

  private Task testTask;
  private Team testTeam;
  private List<Task> tasks;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);

    // given
    testTask = new Task();
    testTask.setTaskId(1L);
    testTask.setTitle("task1");
    testTask.setDescription("This is task 1");
    testTask.setCreationDate(LocalDateTime.now());
    testTask.setStatus(TaskStatus.TODO);

    testTeam = new Team();
    testTeam.setTeamId(1L);
    testTeam.setName("team1");
    testTeam.setDescription("This is team 1");

    testTask.setTeam(testTeam);

    tasks = new ArrayList<>();
    tasks.add(testTask);

    // when -> any object is being save in the taskRepository -> return the dummy testTask
    Mockito.when(taskRepository.save(Mockito.any())).thenReturn(testTask);
  }

  // POST
  /**
   * Test for creating a new task with valid inputs
   */
  @Test
  public void createTask_validInputs_success() {
    // when -> try to find teamId in the teamService -> return dummy team
    Mockito.when(teamService.getTeam(Mockito.any())).thenReturn(testTeam);

    // when -> any object is being saved in the taskRepository -> return the dummy testTask
    Task createdTask = taskService.createTask(testTask);

    // then
    Mockito.verify(taskRepository, Mockito.times(1)).save(Mockito.any());

    // check that task objects are expected
    assertEquals(testTask.getTaskId(), createdTask.getTaskId());
    assertEquals(testTask.getTitle(), createdTask.getTitle());
    assertEquals(testTask.getDescription(), createdTask.getDescription());
    assertEquals(testTask.getStatus(), createdTask.getStatus());
    assertEquals(testTask.getTeam(), createdTask.getTeam());
  }

  /**
   * Test for creating a new task with missing fields throws exception
   */
  @Test
  public void createTask_missingFields_throwsException() {
    // given
    Task incompleteTask = new Task();
    incompleteTask.setTitle(null);
    incompleteTask.setDescription("This is task 1");
    incompleteTask.setTeam(testTeam);

    // when/then -> try to create task with missing fields -> should throw an exception
    assertThrows(ResponseStatusException.class, () -> taskService.createTask(incompleteTask));
  }

  /**
   * Test for creating a new task with empty fields throws exception
   */
  @Test
  public void createTask_emptyFields_throwsException() {
    // given
    Task emptyTask = new Task();
    emptyTask.setTitle("");
    emptyTask.setDescription("");
    emptyTask.setTeam(testTeam);

    // when/then -> try to create task with empty fields -> should throw an exception
    assertThrows(ResponseStatusException.class, () -> taskService.createTask(emptyTask));
  }

  // GET
  /**
   * Test for getting all tasks of a team if team exists and has tasks
   */
  @Test
  public void getTasksByTeamId_validInputs_success() {
    // when -> try to find teamId in the teamService -> return dummy team
    Mockito.when(teamService.getTeam(Mockito.any())).thenReturn(testTeam);

    // when -> try to find tasks by team in the taskRepository -> return list with dummy task
    Mockito.when(taskRepository.findByTeam(Mockito.any())).thenReturn(tasks);

    // call the method under test
    List<Task> foundTasks = taskService.getTasksByTeamId(testTeam.getTeamId());

    // assert found list with one task
    assertEquals(1, foundTasks.size());
    assertEquals(testTask, foundTasks.get(0));
  }

  /**
   * Test for getting all tasks of a team if team exists but has no tasks
   */
  @Test
  public void getTasksByTeamId_validInputs_noTasks() {
    // when -> try to find teamId in the teamService -> return dummy team
    Mockito.when(teamService.getTeam(Mockito.any())).thenReturn(testTeam);

    // when -> try to find tasks by team in the taskRepository -> return empty list
    Mockito.when(taskRepository.findByTeam(Mockito.any())).thenReturn(new ArrayList<>());

    // call the method under test
    List<Task> foundTasks = taskService.getTasksByTeamId(testTeam.getTeamId());

    // assert found empty list
    assertEquals(0, foundTasks.size());
  }

  /**
   * Test for getting all tasks of a team if team does not exist
   */
  @Test
  public void getTasksByTeamId_invalidInputs_teamDoesNotExist_throwsException() {
    // when -> try to find teamId in the teamService -> return null
    Mockito.when(teamService.getTeam(Mockito.any()))
        .thenThrow(new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Team not found with id " + testTeam.getTeamId()));

    // call the method under test and assert an exception is thrown
    assertThrows(
        ResponseStatusException.class, () -> taskService.getTasksByTeamId(testTeam.getTeamId()));
  }
}
