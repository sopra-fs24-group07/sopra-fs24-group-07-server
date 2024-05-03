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
import java.util.Optional;
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
  @Mock private PusherService pusherService;

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

    // when pusher call -> mock
    Mockito.doNothing().when(pusherService).taskModification(Mockito.anyString());
  }

  // POST
  /**
   * Test for creating a new task with valid inputs
   */
  @Test
  public void createTask_validInputs_success() {
    // when -> try to find teamId in the teamService -> return dummy team
    Mockito.when(teamService.getTeamByTeamId(Mockito.any())).thenReturn(testTeam);

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

  /**
   * Test for creating a new task with non-existing team throws exception
   */
  @Test
  public void createTask_nonExistingTeam_throwsException() {
    // given
    Task taskWithNonExistingTeam = new Task();
    taskWithNonExistingTeam.setTitle("task1");
    taskWithNonExistingTeam.setDescription("This is task 1");
    taskWithNonExistingTeam.setTeam(null);

    // when -> try to find teamId in the teamService -> return null
    Mockito.when(teamService.getTeamByTeamId(Mockito.any())).thenReturn(null);

    // when/then -> try to create task with non-existing team -> should throw an exception
    assertThrows(
        ResponseStatusException.class, () -> taskService.createTask(taskWithNonExistingTeam));
  }

  // GET
  /**
   * Test for getting all tasks of a team if team exists and has tasks
   */
  @Test
  public void getTasksByTeamId_validInputs_success() {
    // when -> try to find teamId in the teamService -> return dummy team
    Mockito.when(teamService.getTeamByTeamId(Mockito.any())).thenReturn(testTeam);

    // when -> try to find tasks by team in the taskRepository -> return list with dummy task
    Mockito
        .when(taskRepository.findByTeamAndStatusNot(Mockito.any(), Mockito.eq(TaskStatus.DELETED)))
        .thenReturn(tasks);

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
    Mockito.when(teamService.getTeamByTeamId(Mockito.any())).thenReturn(testTeam);

    // when -> try to find tasks by team in the taskRepository -> return empty list
    Mockito
        .when(taskRepository.findByTeamAndStatusNot(Mockito.any(), Mockito.eq(TaskStatus.DELETED)))
        .thenReturn(new ArrayList<>());

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
    Mockito.when(teamService.getTeamByTeamId(Mockito.any()))
        .thenThrow(new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Team not found with id " + testTeam.getTeamId()));

    // call the method under test and assert an exception is thrown
    assertThrows(
        ResponseStatusException.class, () -> taskService.getTasksByTeamId(testTeam.getTeamId()));
  }

  // region getTasksByTeamIdAndStatus


  /**
   * Test for getting all tasks of a team by status if team exists and has tasks
   */
  @Test
  public void getTasksByTeamIdAndStatus_validInputs_success() {
    // given
    List<TaskStatus> status = new ArrayList<>();
    status.add(TaskStatus.TODO);
    status.add(TaskStatus.IN_SESSION);

    // when -> try to find teamId in the teamService -> return dummy team
    Mockito.when(teamService.getTeamByTeamId(Mockito.any())).thenReturn(testTeam);

    // when -> try to find tasks by team and status in the taskRepository -> return list with dummy task
    Mockito
        .when(taskRepository.findByTeamAndStatusInOrderByTitleAsc(Mockito.any(), Mockito.eq(status)))
        .thenReturn(tasks);

    // call the method under test
    List<Task> foundTasks = taskService.getTasksByTeamIdAndStatus(testTeam.getTeamId(), status);

    // assert found list with one task
    assertEquals(1, foundTasks.size());
    assertEquals(testTask, foundTasks.get(0));
  }

  /*
   * Test for getting all tasks of a team by status if team exists but has no tasks
   */
  @Test
  public void getTasksByTeamIdAndStatus_validInputs_noTasks() {
    // given
    List<TaskStatus> status = new ArrayList<>();
    status.add(TaskStatus.TODO);
    status.add(TaskStatus.IN_SESSION);

    // when -> try to find teamId in the teamService -> return dummy team
    Mockito.when(teamService.getTeamByTeamId(Mockito.any())).thenReturn(testTeam);

    // when -> try to find tasks by team and status in the taskRepository -> return empty list
    Mockito
        .when(taskRepository.findByTeamAndStatusInOrderByTitleAsc(Mockito.any(), Mockito.eq(status)))
        .thenReturn(new ArrayList<>());

    // call the method under test
    List<Task> foundTasks = taskService.getTasksByTeamIdAndStatus(testTeam.getTeamId(), status);

    // assert found empty list
    assertEquals(0, foundTasks.size());
  }

  /*
   * Test for getting all tasks of a team by status if team does not exist
   */
  @Test
  public void getTasksByTeamIdAndStatus_invalidInputs_teamDoesNotExist_throwsException() {
    // given
    List<TaskStatus> status = new ArrayList<>();
    status.add(TaskStatus.TODO);
    status.add(TaskStatus.IN_SESSION);

    // when -> try to find teamId in the teamService -> return null
    Mockito.when(teamService.getTeamByTeamId(Mockito.any()))
        .thenThrow(new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Team not found with id " + testTeam.getTeamId()));

    // call the method under test and assert an exception is thrown
    assertThrows(
        ResponseStatusException.class,
        () -> taskService.getTasksByTeamIdAndStatus(testTeam.getTeamId(), status));
  }

  // endregion



  // PUT

  /**
   * Test for updating a task with valid input (happy-path)
   */
  @Test
  public void updateTask_validInputs_success() {
    // given
    Task updatedTask = new Task();
    updatedTask.setTaskId(testTask.getTaskId());
    updatedTask.setTitle("Updated Task Title");
    updatedTask.setDescription("Updated Task Description");
    updatedTask.setStatus(TaskStatus.DONE);
    updatedTask.setTeam(testTeam);

    // when
    Mockito.when(taskRepository.findById(testTask.getTaskId())).thenReturn(Optional.of(testTask));
    Mockito.when(teamService.getTeamByTeamId(testTeam.getTeamId())).thenReturn(testTeam);

    // then
    Task returnedTask = taskService.updateTask(updatedTask, testTeam.getTeamId());

    Mockito.verify(taskRepository, Mockito.times(1)).save(Mockito.any());

    assertEquals(updatedTask.getTaskId(), returnedTask.getTaskId());
    assertEquals(updatedTask.getTitle(), returnedTask.getTitle());
    assertEquals(updatedTask.getDescription(), returnedTask.getDescription());
    assertEquals(updatedTask.getStatus(), returnedTask.getStatus());
  }

  /**
   * Test for trying to update a task that does nnot exist
   */
  @Test
  public void updateTask_nonExistingTask_throwsException() {
    // given
    Task updatedTask = new Task();
    updatedTask.setTaskId(2L); // non-existing task id
    updatedTask.setTitle("Updated Task Title");
    updatedTask.setDescription("Updated Task Description");
    updatedTask.setStatus(TaskStatus.DONE);
    updatedTask.setTeam(testTeam);

    // when
    Mockito.when(taskRepository.findById(updatedTask.getTaskId())).thenReturn(Optional.empty());
    Mockito.when(teamService.getTeamByTeamId(testTeam.getTeamId())).thenReturn(testTeam);

    // then
    assertThrows(ResponseStatusException.class,
        () -> taskService.updateTask(updatedTask, testTeam.getTeamId()));
    Mockito.verify(taskRepository, Mockito.times(0)).save(Mockito.any());
  }

  /**
   * Test for trying to update a task that does not belong to a team
   */
  @Test
  public void updateTask_taskDoesNotBelongToTeam_throwsException() {
    // given
    Task updatedTask = new Task();
    updatedTask.setTaskId(testTask.getTaskId());
    updatedTask.setTitle("Updated Task Title");
    updatedTask.setDescription("Updated Task Description");
    updatedTask.setStatus(TaskStatus.DONE);

    Team differentTeam = new Team();
    differentTeam.setTeamId(2L);
    differentTeam.setName("team2");
    differentTeam.setDescription("This is team 2");

    updatedTask.setTeam(differentTeam);

    // when
    Mockito.when(taskRepository.findById(testTask.getTaskId())).thenReturn(Optional.of(testTask));
    Mockito.when(teamService.getTeamByTeamId(differentTeam.getTeamId())).thenReturn(differentTeam);

    // then
    assertThrows(ResponseStatusException.class,
        () -> taskService.updateTask(updatedTask, differentTeam.getTeamId()));
    Mockito.verify(taskRepository, Mockito.times(0)).save(Mockito.any());
  }
}
