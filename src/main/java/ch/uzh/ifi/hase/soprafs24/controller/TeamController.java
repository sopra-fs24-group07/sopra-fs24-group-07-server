package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Task;
import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TaskGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TaskPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TeamGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TeamPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.AuthorizationService;
import ch.uzh.ifi.hase.soprafs24.service.TaskService;
import ch.uzh.ifi.hase.soprafs24.service.TeamService;
import ch.uzh.ifi.hase.soprafs24.service.TeamUserService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/*
 * Team Controller
 * This class is responsible for handling all REST request that are related to the team, and linking
 * the team to the user.
 */
@RestController
@RequestMapping("api/v1")
public class TeamController {
  private final TeamService teamService;
  private final AuthorizationService authorizationService;
  private final TeamUserService teamUserService;
  private final TaskService taskService;

  TeamController(TeamService teamService, AuthorizationService authorizationService,
      TeamUserService teamUserService, TaskService taskService) {
    this.teamService = teamService;
    this.authorizationService = authorizationService;
    this.teamUserService = teamUserService;
    this.taskService = taskService;
  }

  @PostMapping("/teams")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public TeamGetDTO createTeam(
      @RequestBody TeamPostDTO teamPostDTO, @RequestHeader("Authorization") String token) {
    // check if user is authorized (valid token)
    User authorizedUser = authorizationService.isAuthorized(token);

    // convert API team to internal representation
    Team teamInput = DTOMapper.INSTANCE.convertTeamPostDTOtoEntity(teamPostDTO);

    // create team
    Team createdTeam = teamService.createTeam(teamInput);

    // add user to created team
    teamUserService.createTeamUser(createdTeam.getTeamId(), authorizedUser.getUserId());

    // convert internal representation of team back to API
    return DTOMapper.INSTANCE.convertEntityToTeamGetDTO(createdTeam);
  }

  @GetMapping("/teams/{teamId}/users")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<UserGetDTO> getUsersOfTeam(
      @PathVariable Long teamId, @RequestHeader("Authorization") String token) {
    // check if user is authorized (valid token) also throws 404 if teamId not found
    User authorizedUser = authorizationService.isAuthorizedAndBelongsToTeam(token, teamId);

    // get users of team
    List<User> users = teamUserService.getUsersOfTeam(teamId);

    // convert internal representation of users back to API
    List<UserGetDTO> userGetDTOs = new ArrayList<>();
    for (User user : users) {
      userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
    }

    return userGetDTOs;
  }

  @PostMapping("/teams/{ID}/tasks")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public TaskGetDTO createTask(@PathVariable("ID") Long teamId,
      @RequestBody TaskPostDTO taskPostDTO, @RequestHeader("Authorization") String token) {
    // check if user is authorized (valid token) and if the user exists
    User authorizedUser = authorizationService.isAuthorizedAndBelongsToTeam(token, teamId);

    // convert API task to internal representation
    Task taskInput = DTOMapper.INSTANCE.convertTaskPostDTOtoEntity(taskPostDTO);
    // we hereby also check whether the team exists or not!!
    taskInput.setTeam(teamService.getTeamByTeamId(teamId));

    // create task
    Task createdTask = taskService.createTask(taskInput);

    // convert internal representation of task back to API
    return DTOMapper.INSTANCE.convertEntityToTaskGetDTO(createdTask);
  }

  @GetMapping("/teams/{ID}/tasks")
  @ResponseBody
  public List<TaskGetDTO> getTasks(
      @PathVariable("ID") Long teamId, @RequestHeader("Authorization") String token) {
    // check if user is authorized (valid token)
    authorizationService.isAuthorized(token);

    // get tasks
    List<Task> tasks = taskService.getTasksByTeamId(teamId);

    // convert internal representation of tasks back to API
    return tasks.stream()
        .map(DTOMapper.INSTANCE::convertEntityToTaskGetDTO)
        .collect(Collectors.toList());
  }

  @PutMapping("/teams/{teamId}/tasks/{taskId}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public TaskGetDTO updateTask(@PathVariable Long teamId, @PathVariable Long taskId,
      @RequestBody Task taskInput, @RequestHeader("Authorization") String token) {
    // check if user is authorized (valid token) and if the user exists
    authorizationService.isAuthorizedAndBelongsToTeam(token, teamId);

    // check if the input task's Id matches the taskId in the path
    if (!taskId.equals(taskInput.getTaskId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task Id does not match");
    }

    // set the team to the taskInput
    taskInput.setTeam(teamService.getTeamByTeamId(teamId));

    // update task
    Task updatedTask = taskService.updateTask(taskInput, teamId);

    // convert internal representation of task back to API
    return DTOMapper.INSTANCE.convertEntityToTaskGetDTO(updatedTask);
  }
}
