package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Task;
import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TaskGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TaskPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TeamGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TeamPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.AuthorizationService;
import ch.uzh.ifi.hase.soprafs24.service.TaskService;
import ch.uzh.ifi.hase.soprafs24.service.TeamService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
  private final TaskService taskService;

  TeamController(
      TeamService teamService, AuthorizationService authorizationService, TaskService taskService) {
    this.teamService = teamService;
    this.authorizationService = authorizationService;
    this.taskService = taskService;
  }

  @PostMapping("/teams")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public TeamGetDTO createTeam(
      @RequestBody TeamPostDTO teamPostDTO, @RequestHeader("Authorization") String token) {
    // check if user is authorized (valid token)
    authorizationService.isAuthorized(token);

    // convert API team to internal representation
    Team teamInput = DTOMapper.INSTANCE.convertTeamPostDTOtoEntity(teamPostDTO);

    // create team
    Team createdTeam = teamService.createTeam(teamInput);

    // TODO: call service to add user to created team

    // convert internal representation of team back to API
    return DTOMapper.INSTANCE.convertEntityToTeamGetDTO(createdTeam);
  }

  @PostMapping("/teams/{ID}/tasks")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public TaskGetDTO createTask(@PathVariable("ID") Long teamId,
      @RequestBody TaskPostDTO taskPostDTO, @RequestHeader("Authorization") String token) {
    // check if user is authorized (valid token)
    authorizationService.isAuthorized(token);

    // convert API task to internal representation
    Task taskInput = DTOMapper.INSTANCE.convertTaskPostDTOtoEntity(taskPostDTO);
    // we hereby also check whether the team exists or not!!
    taskInput.setTeam(teamService.getTeam(teamId)); // set the team for the task

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
}
