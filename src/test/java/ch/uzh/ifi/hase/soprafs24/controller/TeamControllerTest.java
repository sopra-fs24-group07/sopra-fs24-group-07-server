package ch.uzh.ifi.hase.soprafs24.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.uzh.ifi.hase.soprafs24.entity.Task;
import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.entity.TeamUser;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TaskPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TaskPutDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TeamPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TeamPutDTO;
import ch.uzh.ifi.hase.soprafs24.service.AuthorizationService;
import ch.uzh.ifi.hase.soprafs24.service.TaskService;
import ch.uzh.ifi.hase.soprafs24.service.TeamService;
import ch.uzh.ifi.hase.soprafs24.service.TeamUserService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

/**
 * TeamControllerTest
 * This is a WebMvcTest which allows to test the TeamController.
 */
@WebMvcTest(TeamController.class)
public class TeamControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private TeamService teamService;
  @MockBean private AuthorizationService authorizationService;
  @MockBean private TeamUserService teamUserService;
  @MockBean private TaskService taskService;

  private User testUser;

  @BeforeEach
  public void setup() {
    testUser = new User();
    testUser.setUserId(1L);
  }

  /**
   * Test for creating a team with valid input
   */
  @Test
  public void createTeam_validInput_teamCreated() throws Exception {
    // given
    Team team = new Team();
    team.setTeamId(1L);
    team.setName("productiviteam");
    team.setDescription("We are the most productive team in sopra");
    team.setTeamUUID("team-uuid");

    TeamPostDTO teamPostDTO = new TeamPostDTO();
    teamPostDTO.setName("productiviteam");
    teamPostDTO.setDescription("We are the most productive team in sopra");

    // mock valid token
    Mockito.when(authorizationService.isAuthorized(Mockito.anyString())).thenReturn(testUser);
    // mock team service
    given(teamService.createTeam(Mockito.any())).willReturn(team);
    // mock add user to team on creation service call
    given(teamUserService.createTeamUser(Mockito.anyLong(), Mockito.anyLong()))
        .willReturn(new TeamUser(team, testUser));

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest =
        post("/api/v1/teams")
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(teamPostDTO))
            .header("Authorization", "1234");

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.teamId", is(team.getTeamId().intValue())))
        .andExpect(jsonPath("$.teamUUID", is(team.getTeamUUID())))
        .andExpect(jsonPath("$.name", is(team.getName())))
        .andExpect(jsonPath("$.description", is(team.getDescription())));
  }

  /**
   * Test for creating a team with invalid authorization
   */
  @Test
  public void createTeam_validInput_invalidAuth() throws Exception {
    // given
    // Team team = new Team();
    // team.setTeamId(1L);
    // team.setName("productiviteam");
    // team.setDescription("We are the most productive team in sopra");

    TeamPostDTO teamPostDTO = new TeamPostDTO();
    teamPostDTO.setName("productiviteam");
    teamPostDTO.setDescription("We are the most productive team in sopra");

    // mock invalid token -> throw exception
    Mockito.when(authorizationService.isAuthorized(Mockito.anyString()))
        .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest =
        post("/api/v1/teams")
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(teamPostDTO))
            .header("Authorization", "1234");

    // then -> isUnauthorized
    mockMvc.perform(postRequest).andExpect(status().isUnauthorized());
  }

  /**
   * Test for creating a team with invalid input
   */
  @Test
  public void createTeam_invalidInput() throws Exception {
    // given
    Team team = new Team();
    team.setTeamId(1L);
    team.setName("");
    team.setDescription("We are the most productive team in sopra");

    TeamPostDTO teamPostDTO = new TeamPostDTO();
    teamPostDTO.setName(""); // empty name is invalid
    teamPostDTO.setDescription("We are the most productive team in sopra");

    // mock invalid token -> throw exception
    // Mockito.doNothing().when(authorizationService).isAuthorized(Mockito.anyString());
    Mockito.when(authorizationService.isAuthorized(Mockito.anyString())).thenReturn(testUser);

    // mock team service
    given(teamService.createTeam(Mockito.any()))
        .willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST));

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest =
        post("/api/v1/teams")
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(teamPostDTO))
            .header("Authorization", "1234");

    // then -> isUnauthorized
    mockMvc.perform(postRequest).andExpect(status().isBadRequest());
  }

  // region getUsersOfTeam tests
  // Don't need to test if we get empty list of users, because there should always at least one user
  // linked to a team (no rotten green path testing)

  /**
   * Test for getting all users of a team, but not valid token (user token not in db)
   */
  @Test
  public void getUsersOfTeam_invalidToken_throwsError() throws Exception {
    // given test team
    Team testTeam = new Team();
    testTeam.setTeamId(1L);

    // when -> is auth check -> is invalid
    given(authorizationService.isAuthorizedAndBelongsToTeam(Mockito.anyString(), Mockito.anyLong()))
        .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

    // when -> perform get request
    MockHttpServletRequestBuilder getRequest =
        get("/api/v1/teams/" + testTeam.getTeamId().toString() + "/users")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "invalid token");

    // then -> validate result for unauthorized
    mockMvc.perform(getRequest)
        .andExpect(status().isUnauthorized())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException));
  }

  /**
   * Test for getting all users of a team, but not valid token (user not in team)
   */
  @Test
  public void getUsersOfTeam_userNotInTeam_throwsError() throws Exception {
    // given test team
    Team testTeam = new Team();
    testTeam.setTeamId(1L);

    // testUser is in team

    // given user that makes request, but is not in teasTeam
    User userNotInTeam = new User();
    userNotInTeam.setUserId(2L);
    userNotInTeam.setToken("userNotInTeamToken");

    // when -> is auth check -> is valid -> but user is not the valid one
    given(authorizationService.isAuthorizedAndBelongsToTeam(Mockito.anyString(), Mockito.anyLong()))
        .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

    // when -> get users of team -> testUser is in team, but not the other user
    given(teamUserService.getUsersOfTeam(Mockito.anyLong()))
        .willReturn(java.util.List.of(testUser));

    // when -> perform get request
    MockHttpServletRequestBuilder getRequest =
        get("/api/v1/teams/" + testTeam.getTeamId().toString() + "/users")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", userNotInTeam.getToken()); // request with this token

    // then -> validate result for unauthorized
    mockMvc.perform(getRequest)
        .andExpect(status().isUnauthorized())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException));
  }

  /**
   * Test for getting all users of a team successfully
   */
  @Test
  public void getUsersOfTeam_validInput() throws Exception {
    // given test team
    Team testTeam = new Team();
    testTeam.setTeamId(1L);

    // testUser is in team

    // when -> is auth check -> is valid
    given(authorizationService.isAuthorizedAndBelongsToTeam(Mockito.anyString(), Mockito.anyLong()))
        .willReturn(testUser);
    // when -> get users of team -> testUser is in team
    given(teamUserService.getUsersOfTeam(Mockito.anyLong()))
        .willReturn(java.util.List.of(testUser));

    // when -> perform get request
    MockHttpServletRequestBuilder getRequest =
        get("/api/v1/teams/" + testTeam.getTeamId().toString() + "/users")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "valid-token");

    // then -> validate result for unauthorized
    mockMvc.perform(getRequest)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].userId", is(testUser.getUserId().intValue())))
        .andExpect(jsonPath("$[0].username", is(testUser.getUsername())));
  }

  // endregion

  // region deleteUserFromTeam tests
  @Test
  public void deleteUserFromTeam_validInput_success() throws Exception {
    // given test team
    Team testTeam = new Team();
    testTeam.setTeamId(1L);

    // testUser is in team
    TeamUser teamUser = new TeamUser(testTeam, testUser);

    // when -> is auth check -> is valid
    given(authorizationService.isAuthorizedAndBelongsToTeam(
              Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong()))
        .willReturn(testUser);
    // when -> delete user from team -> testUser is in team
    Mockito.doReturn(teamUser)
        .when(teamUserService)
        .deleteUserOfTeam(Mockito.anyLong(), Mockito.anyLong());

    // when -> perform delete request
    MockHttpServletRequestBuilder deleteRequest = delete(
        "/api/v1/teams/" + testTeam.getTeamId().toString() + "/users/" + testUser.getUserId())
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .header("Authorization", "valid-token");

    // then -> validate result for unauthorized
    mockMvc.perform(deleteRequest).andExpect(status().isOk());
    Mockito.verify(teamUserService, Mockito.times(1))
        .deleteUserOfTeam(Mockito.anyLong(), Mockito.anyLong());
  }

  @Test
  public void deleteUserFromTeam_unauthorized_throwsError() throws Exception {
    // e.g. also testUser not in team -> error

    // given test team
    Team testTeam = new Team();
    testTeam.setTeamId(1L);

    // when -> is auth check -> is invalid
    given(authorizationService.isAuthorizedAndBelongsToTeam(
              Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong()))
        .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

    // when -> perform delete request
    MockHttpServletRequestBuilder deleteRequest = delete(
        "/api/v1/teams/" + testTeam.getTeamId().toString() + "/users/" + testUser.getUserId())
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .header("Authorization", "invalid-token");

    // then -> validate result for unauthorized
    mockMvc.perform(deleteRequest).andExpect(status().isUnauthorized());
    Mockito.verify(teamUserService, Mockito.never())
        .deleteUserOfTeam(Mockito.anyLong(), Mockito.anyLong());
  }

  @Test
  public void deleteUserFromTeam_somethingNotFound_throwsError() throws Exception {
    // given test team
    Team testTeam = new Team();
    testTeam.setTeamId(1L);

    // testUser is in team

    // when -> is auth check -> is valid
    given(authorizationService.isAuthorizedAndBelongsToTeam(
              Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong()))
        .willReturn(testUser);
    // when -> delete user from team -> something not found
    Mockito.doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
        .when(teamUserService)
        .deleteUserOfTeam(Mockito.anyLong(), Mockito.anyLong());

    // when -> perform delete request
    MockHttpServletRequestBuilder deleteRequest = delete("/api/v1/teams/"
        + testTeam.getTeamId().toString() + "/users/" + testUser.getUserId().toString())
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .header("Authorization", "valid-token");

    // then -> validate result for unauthorized
    mockMvc.perform(deleteRequest).andExpect(status().isNotFound());
    Mockito.verify(teamUserService, Mockito.times(1))
        .deleteUserOfTeam(Mockito.anyLong(), Mockito.anyLong()); // was called, but error
  }

  // endregion

  // region updateTeam tests
  @Test
  public void updateTeam_validInput_success() throws Exception {
    // given test team
    Team testTeam = new Team();
    testTeam.setTeamId(1L);
    testTeam.setName("productiviteam");
    testTeam.setDescription("We are the most productive team in sopra");

    // given updated team
    Team testUpdatedTeam = new Team();
    testUpdatedTeam.setTeamId(1L);
    testUpdatedTeam.setName("productiviERteam");
    testUpdatedTeam.setDescription("We are the MORE most productive team in sopra");

    // given teamPutDTO
    TeamPutDTO teamPutDTO = new TeamPutDTO();
    teamPutDTO.setName(testUpdatedTeam.getName());
    teamPutDTO.setDescription(testUpdatedTeam.getDescription());

    // when -> is auth check -> is valid
    given(authorizationService.isAuthorizedAndBelongsToTeam(Mockito.anyString(), Mockito.anyLong()))
        .willReturn(testUser);
    // when -> update team -> testUser is in team
    given(teamService.updateTeam(Mockito.any())).willReturn(testUpdatedTeam);

    // when -> perform put request
    MockHttpServletRequestBuilder putRequest =
        put("/api/v1/teams/" + testTeam.getTeamId().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(teamPutDTO))
            .header("Authorization", "valid-token");

    // then -> validate result for unauthorized
    mockMvc.perform(putRequest)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.teamId", is(testUpdatedTeam.getTeamId().intValue())))
        .andExpect(jsonPath("$.name", is(testUpdatedTeam.getName())))
        .andExpect(jsonPath("$.description", is(testUpdatedTeam.getDescription())));
  }

  @Test
  public void updateTeam_unauthorized_throwsError() throws Exception {
    // given test team
    Team testTeam = new Team();
    testTeam.setTeamId(1L);
    testTeam.setName("productiviteam");
    testTeam.setDescription("We are the most productive team in sopra");

    // given updated team
    Team testUpdatedTeam = new Team();
    testUpdatedTeam.setTeamId(1L);
    testUpdatedTeam.setName("productiviERteam");
    testUpdatedTeam.setDescription("We are the MORE most productive team in sopra");

    // given teamPutDTO
    TeamPutDTO teamPutDTO = new TeamPutDTO();
    teamPutDTO.setName(testUpdatedTeam.getName());
    teamPutDTO.setDescription(testUpdatedTeam.getDescription());

    // when -> is auth check -> is invalid
    given(authorizationService.isAuthorizedAndBelongsToTeam(Mockito.anyString(), Mockito.anyLong()))
        .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

    // when -> perform put request
    MockHttpServletRequestBuilder putRequest =
        put("/api/v1/teams/" + testTeam.getTeamId().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(teamPutDTO))
            .header("Authorization", "invalid-token");

    // then -> validate result for unauthorized
    mockMvc.perform(putRequest).andExpect(status().isUnauthorized());
  }

  @Test
  public void updateTeam_somethingNotFound_throwsError() throws Exception {
    // given test team
    Team testTeam = new Team();
    testTeam.setTeamId(1L);
    testTeam.setName("productiviteam");
    testTeam.setDescription("We are the most productive team in sopra");

    // given updated team
    Team testUpdatedTeam = new Team();
    testUpdatedTeam.setTeamId(1L);
    testUpdatedTeam.setName("productiviERteam");
    testUpdatedTeam.setDescription("We are the MORE most productive team in sopra");

    // given teamPutDTO
    TeamPutDTO teamPutDTO = new TeamPutDTO();
    teamPutDTO.setName(testUpdatedTeam.getName());
    teamPutDTO.setDescription(testUpdatedTeam.getDescription());

    // when -> is auth check -> is valid
    given(authorizationService.isAuthorizedAndBelongsToTeam(Mockito.anyString(), Mockito.anyLong()))
        .willReturn(testUser);
    // when -> update team -> something not found
    given(teamService.updateTeam(Mockito.any()))
        .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

    // when -> perform put request
    MockHttpServletRequestBuilder putRequest =
        put("/api/v1/teams/" + testTeam.getTeamId().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(teamPutDTO))
            .header("Authorization", "valid-token");

    // then -> validate result for unauthorized
    mockMvc.perform(putRequest).andExpect(status().isNotFound());
  }

  @Test
  public void updateTeam_badRequest_throwsError() throws Exception {
    // given test team
    Team testTeam = new Team();
    testTeam.setTeamId(1L);
    testTeam.setName("productiviteam");
    testTeam.setDescription("We are the most productive team in sopra");

    // given updated team
    Team testUpdatedTeam = new Team();
    testUpdatedTeam.setTeamId(1L);
    testUpdatedTeam.setName("");
    testUpdatedTeam.setDescription("We are the MORE most productive team in sopra");

    // given teamPutDTO
    TeamPutDTO teamPutDTO = new TeamPutDTO();
    teamPutDTO.setName(testUpdatedTeam.getName());
    teamPutDTO.setDescription(testUpdatedTeam.getDescription());

    // when -> is auth check -> is valid
    given(authorizationService.isAuthorizedAndBelongsToTeam(Mockito.anyString(), Mockito.anyLong()))
        .willReturn(testUser);
    // when -> update team -> something not found
    given(teamService.updateTeam(Mockito.any()))
        .willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST));

    // when -> perform put request
    MockHttpServletRequestBuilder putRequest =
        put("/api/v1/teams/" + testTeam.getTeamId().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(teamPutDTO))
            .header("Authorization", "valid-token");

    // then -> validate result for unauthorized
    mockMvc.perform(putRequest).andExpect(status().isBadRequest());
  }
  // endregion

  // region TaskControllerTest for POST

  // POST

  /**
   * Test for creating a Task with valid Inputs (Happy-Path)
   */
  @Test
  public void createTask_validInput_taskCreated() throws Exception {
    // given
    Task task = new Task();
    task.setTaskId(1L);
    task.setTitle("Test Task");
    task.setDescription("This is a test task.");

    TaskPostDTO taskPostDTO = new TaskPostDTO();
    taskPostDTO.setTitle("Test Task");
    taskPostDTO.setDescription("This is a test task.");

    User mockUser = new User();
    mockUser.setUserId(1L);
    mockUser.setToken("1234");

    // mock the return of isAuthorizedAndBelongsToTeam()
    Mockito
        .when(authorizationService.isAuthorizedAndBelongsToTeam(
            Mockito.anyString(), Mockito.anyLong()))
        .thenReturn(mockUser);
    given(taskService.createTask(Mockito.any())).willReturn(task);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest =
        post("/api/v1/teams/1/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(taskPostDTO))
            .header("Authorization", "1234");

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.taskId", is(task.getTaskId().intValue())))
        .andExpect(jsonPath("$.title", is(task.getTitle())))
        .andExpect(jsonPath("$.description", is(task.getDescription())));
  }

  /**
   * Test for creating a Task with missing Fields
   */
  @Test
  public void createTask_missingFields_throwsError() throws Exception {
    // given
    TaskPostDTO taskPostDTO = new TaskPostDTO();
    taskPostDTO.setTitle("Test Task");

    User mockUser = new User();
    mockUser.setUserId(1L);
    mockUser.setToken("1234");

    // mock the return of isAuthorizedAndBelongsToTeam()
    Mockito
        .when(authorizationService.isAuthorizedAndBelongsToTeam(
            Mockito.anyString(), Mockito.anyLong()))
        .thenReturn(mockUser);
    given(taskService.createTask(Mockito.any()))
        .willThrow(new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Some needed fields are missing in the task object."));

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest =
        post("/api/v1/teams/1/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(taskPostDTO))
            .header("Authorization", "1234");

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isBadRequest())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
        .andExpect(result
            -> assertTrue(result.getResolvedException().getMessage().contains(
                "Some needed fields are missing in the task object.")));
  }

  /**
   * Test for creating a Task where User has unauthorized Access
   */
  @Test
  public void createTask_unauthorizedAccess_throwsError() throws Exception {
    // given
    TaskPostDTO taskPostDTO = new TaskPostDTO();
    taskPostDTO.setTitle("Test Task");
    taskPostDTO.setDescription("This is a test task.");

    Mockito
        .doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authorized to access."))
        .when(authorizationService)
        .isAuthorizedAndBelongsToTeam(Mockito.any(), Mockito.any());

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest =
        post("/api/v1/teams/1/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(taskPostDTO))
            .header("Authorization", "1234");

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isUnauthorized())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
        .andExpect(result
            -> assertTrue(
                result.getResolvedException().getMessage().contains("Not authorized to access.")));
  }

  // endregion

  // region TaskControllerTest for GET

  // GET

  /**
   * Test for fetching a Task with valid input (happy-path)
   */
  @Test
  public void getTasks_validInput_returnTasks() throws Exception {
    // given
    Task task = new Task();
    task.setTaskId(1L);
    task.setTitle("Test Task");
    task.setDescription("This is a test task.");

    List<Task> tasks = new ArrayList<>();
    tasks.add(task);

    Mockito
        .when(authorizationService.isAuthorizedAndBelongsToTeam(
            Mockito.anyString(), Mockito.anyLong()))
        .thenReturn(testUser);
    given(taskService.getTasksByTeamId(Mockito.anyLong())).willReturn(tasks);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder getRequest =
        get("/api/v1/teams/1/tasks").header("Authorization", "1234");

    // then
    mockMvc.perform(getRequest)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].taskId", is(task.getTaskId().intValue())))
        .andExpect(jsonPath("$[0].title", is(task.getTitle())))
        .andExpect(jsonPath("$[0].description", is(task.getDescription())));
  }

  /**
   * Test for trying to fetch a Task, where there is no task in team
   */
  @Test
  public void getTaskes_noTasksInTeam_success() throws Exception {
    // given no tasks
    Mockito
        .when(authorizationService.isAuthorizedAndBelongsToTeam(
            Mockito.anyString(), Mockito.anyLong()))
        .thenReturn(testUser);
    given(taskService.getTasksByTeamId(Mockito.anyLong())).willReturn(List.of());

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder getRequest =
        get("/api/v1/teams/1/tasks").header("Authorization", "1234");

    // then
    mockMvc.perform(getRequest).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
  }

  /**
   * Test for trying to fetch a Task, where i'm not authorized to access
   */
  @Test
  public void getTasks_unauthorizedAccess_throwsError() throws Exception {
    // given
    Mockito
        .doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authorized to access."))
        .when(authorizationService)
        .isAuthorizedAndBelongsToTeam(Mockito.any(), Mockito.anyLong());

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder getRequest =
        get("/api/v1/teams/1/tasks").header("Authorization", "1234");

    // then
    mockMvc.perform(getRequest)
        .andExpect(status().isUnauthorized())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
        .andExpect(result
            -> assertTrue(
                result.getResolvedException().getMessage().contains("Not authorized to access.")));
  }

  /*
   * GET method which uses getTasksByTeamIdAndStatus method from TaskService to get tasks by status
   */
  @Test
  public void getTasksByStatus_validInput_returnTasks() throws Exception {
    // given
    Task task = new Task();
    task.setTaskId(1L);
    task.setTitle("Test Task");
    task.setDescription("This is a test task.");
  
    List<Task> tasks = new ArrayList<>();
    tasks.add(task);
  
    Mockito
        .when(authorizationService.isAuthorizedAndBelongsToTeam(
            Mockito.anyString(), Mockito.anyLong()))
        .thenReturn(testUser);
    given(taskService.getTasksByTeamIdAndStatus(Mockito.anyLong(), Mockito.anyList()))
        .willReturn(tasks);
  
    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder getRequest =
        get("/api/v1/teams/1/tasks?status=TODO").header("Authorization", "1234");
  
    // then
    mockMvc.perform(getRequest)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1))) // Check if the returned list has one element
        .andExpect(jsonPath("$[0].taskId", is(task.getTaskId().intValue())))
        .andExpect(jsonPath("$[0].title", is(task.getTitle())))
        .andExpect(jsonPath("$[0].description", is(task.getDescription())));
  }

  @Test
  public void getTasksByStatus_noTasksInTeam_emptyList() throws Exception {
    // given
    given(taskService.getTasksByTeamIdAndStatus(Mockito.anyLong(), Mockito.anyList()))
        .willReturn(new ArrayList<>());

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder getRequest =
        get("/api/v1/teams/1/tasks?status=TODO").header("Authorization", "1234");

    // then
    mockMvc.perform(getRequest).andExpect(status().isOk()).andExpect(content().json("[]"));
  }

  /*
   * Test for trying to fetch a Task, where i'm not authorized to access
   */
  @Test
  public void getTasksByStatus_unauthorizedAccess_throwsError() throws Exception {
    // given
    Mockito
        .doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authorized to access."))
        .when(authorizationService)
        .isAuthorizedAndBelongsToTeam(Mockito.anyString(), Mockito.anyLong());

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder getRequest =
        get("/api/v1/teams/1/tasks?status=TODO").header("Authorization", "1234");

    // then
    mockMvc.perform(getRequest)
        .andExpect(status().isUnauthorized())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
        .andExpect(result
            -> assertTrue(
                result.getResolvedException().getMessage().contains("Not authorized to access.")));
  }

  /*
   * Test for trying to fetch a Task with invalid status
   */
  @Test
  public void getTasksByStatus_invalidStatus_throwsError() throws Exception {
    // given
    Mockito
        .when(authorizationService.isAuthorizedAndBelongsToTeam(
            Mockito.anyString(), Mockito.anyLong()))
        .thenReturn(testUser);
    Mockito.doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Invalid task status."))
        .when(taskService)
        .getTasksByTeamIdAndStatus(Mockito.anyLong(), Mockito.anyList());

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder getRequest =
        get("/api/v1/teams/1/tasks?status=ABC").header("Authorization", "1234");

    // then
    mockMvc.perform(getRequest)
        .andExpect(status().isConflict())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
        .andExpect(result
            -> assertTrue(
                result.getResolvedException().getMessage().contains("Invalid task status.")));
  }

  /* test if team not found */
  @Test
  public void getTasks_somethingNotFound_throwsError() throws Exception {
    // given
    Mockito
        .when(authorizationService.isAuthorizedAndBelongsToTeam(
            Mockito.anyString(), Mockito.anyLong()))
        .thenReturn(testUser);
    given(taskService.getTasksByTeamId(Mockito.anyLong()))
        .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder getRequest =
        get("/api/v1/teams/1/tasks").header("Authorization", "1234");

    // then
    mockMvc.perform(getRequest).andExpect(status().isNotFound());
  }

  // endregion

  // region TaskControllerTest for PUT

  // PUT

  /**
   * Test for updating a task with valid inputs (happy-path)
   */
  @Test
  public void updateTask_validInput_taskUpdated() throws Exception {
    // given
    Task task = new Task();
    task.setTaskId(1L);
    task.setTitle("Test Task");
    task.setDescription("This is a test task.");

    TaskPutDTO taskPutDTO = new TaskPutDTO();
    taskPutDTO.setTitle("Updated Task");
    taskPutDTO.setDescription("This is an updated test task.");

    User mockUser = new User();
    mockUser.setUserId(1L);
    mockUser.setToken("1234");

    // mock the return of isAuthorizedAndBelongsToTeam()
    Mockito
        .when(authorizationService.isAuthorizedAndBelongsToTeam(
            Mockito.anyString(), Mockito.anyLong()))
        .thenReturn(mockUser);

    // mock the return of updateTask()
    given(taskService.updateTask(Mockito.any(), Mockito.anyLong())).willReturn(task);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder putRequest =
        put("/api/v1/teams/1/tasks/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(taskPutDTO))
            .header("Authorization", "1234");

    // then
    mockMvc.perform(putRequest)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.taskId", is(task.getTaskId().intValue())))
        .andExpect(jsonPath("$.title", is(task.getTitle())))
        .andExpect(jsonPath("$.description", is(task.getDescription())));
  }

  /**
   * Test for trying to update a task, but i'm not authorized to do so
   */
  @Test
  public void updateTask_unauthorizedAccess_throwsError() throws Exception {
    // given
    TaskPutDTO taskPutDTO = new TaskPutDTO();
    taskPutDTO.setTitle("Updated Task");
    taskPutDTO.setDescription("This is an updated test task.");

    Mockito
        .doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authorized to access."))
        .when(authorizationService)
        .isAuthorizedAndBelongsToTeam(Mockito.any(), Mockito.any());

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder putRequest =
        put("/api/v1/teams/1/tasks/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(taskPutDTO))
            .header("Authorization", "1234");

    // then
    mockMvc.perform(putRequest)
        .andExpect(status().isUnauthorized())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
        .andExpect(result
            -> assertTrue(
                result.getResolvedException().getMessage().contains("Not authorized to access.")));
  }

  /**
   * Test for trying to update a task, but the task doesn't exist
   */
  @Test
  public void updateTask_taskDoesNotExist_throwsError() throws Exception {
    // given
    TaskPutDTO taskPutDTO = new TaskPutDTO();
    taskPutDTO.setTitle("Updated Task");
    taskPutDTO.setDescription("This is an updated test task.");

    User mockUser = new User();
    mockUser.setUserId(1L);
    mockUser.setToken("1234");

    // mock the return of isAuthorizedAndBelongsToTeam()
    Mockito
        .when(authorizationService.isAuthorizedAndBelongsToTeam(
            Mockito.anyString(), Mockito.anyLong()))
        .thenReturn(mockUser);

    // mock the return of updateTask()
    given(taskService.updateTask(Mockito.any(), Mockito.anyLong()))
        .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found."));

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder putRequest =
        put("/api/v1/teams/1/tasks/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(taskPutDTO))
            .header("Authorization", "1234");

    // then
    mockMvc.perform(putRequest)
        .andExpect(status().isNotFound())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
        .andExpect(result
            -> assertTrue(result.getResolvedException().getMessage().contains("Task not found.")));
  }
  // endregion
}
