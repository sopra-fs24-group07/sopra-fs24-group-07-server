package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.TaskStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Task;
import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.entity.TeamInvitation;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.*;
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
  private final MailService mailService;

  TeamController(TeamService teamService, AuthorizationService authorizationService,
      TeamUserService teamUserService, TaskService taskService, MailService mailService) {
    this.teamService = teamService;
    this.authorizationService = authorizationService;
    this.teamUserService = teamUserService;
    this.taskService = taskService;
    this.mailService = mailService;
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

  @PutMapping("/teams/{teamId}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public TeamGetDTO updateTeam(@PathVariable Long teamId, @RequestBody TeamPutDTO teamPutDTO,
      @RequestHeader("Authorization") String token) {
    // check if user is authorized (valid token)
    User authorizedUser = authorizationService.isAuthorizedAndBelongsToTeam(token, teamId);

    // convert API team to internal representation
    Team teamToUpdate = DTOMapper.INSTANCE.convertTeamPutDTOtoEntity(teamPutDTO);
    teamToUpdate.setTeamId(teamId);

    // updated team
    Team updatedTeam = teamService.updateTeam(teamToUpdate);

    // convert internal representation of team back to API
    return DTOMapper.INSTANCE.convertEntityToTeamGetDTO(updatedTeam);
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

  @PostMapping("/teams/{teamId}/invitations")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public void sendInvitationToEmail(@PathVariable Long teamId,
      @RequestBody TeamInvitationPostDTO invitationPostDTO,
      @RequestHeader("Authorization") String token) {
    // check if user is authorized (valid token) also throws 404 if teamId not found
    User authorizedUser = authorizationService.isAuthorizedAndBelongsToTeam(token, teamId);

    // convert invitation post dto to internal representation
    TeamInvitation teamInvitation =
        DTOMapper.INSTANCE.convertTeamInvitationPostDTOtoEntity(invitationPostDTO);

    // check if the team-uuid in the body is actually the one of the team (so that user cannot send
    // invitation to other team-uuids)
    if (!teamService.getTeamByTeamId(teamId).getTeamUUID().equals(teamInvitation.getTeamUUID())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not in team");
    }

    // send invitation: https://productiviteam.co/invitation/team-uuid
    String invitationUrl = "https://productiviteam.co/invitation/" + teamInvitation.getTeamUUID();
    // throws 400 if email is not valid or api error
    mailService.sendInvitationEmail(teamInvitation.getReceiverEmail(), invitationUrl);
  }

  @DeleteMapping("/teams/{teamId}/users/{userId}")
  @ResponseStatus(HttpStatus.OK)
  public void deleteUserFromTeam(@PathVariable Long teamId, @PathVariable Long userId,
      @RequestHeader("Authorization") String token) {
    // check if user is authorized (valid token) also throws 404 if teamId not found
    User authorizedUser = authorizationService.isAuthorizedAndBelongsToTeam(token, userId, teamId);

    // delete user from team
    teamUserService.deleteUserOfTeam(teamId, userId);
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
    authorizationService.isAuthorizedAndBelongsToTeam(token, teamId);

    // get tasks
    List<Task> tasks = taskService.getTasksByTeamId(teamId);

    // convert internal representation of tasks back to API
    return tasks.stream()
        .map(DTOMapper.INSTANCE::convertEntityToTaskGetDTO)
        .collect(Collectors.toList());
  }

  /*
   * GET method which uses getTasksByTeamIdAndStatus method from TaskService to get tasks by status
   */
  @GetMapping(value = "/teams/{ID}/tasks", params = {"status"})
  @ResponseBody
  public List<TaskGetDTO> getTasksByStatus(@PathVariable("ID") Long teamId,
      @RequestHeader("Authorization") String token, @RequestParam("status") List<String> status) {
    // auth user
    authorizationService.isAuthorizedAndBelongsToTeam(token, teamId);

    // convert status to TaskStatus
    List<TaskStatus> taskStatusList;
    try {
      taskStatusList = status.stream().map(TaskStatus::valueOf).collect(Collectors.toList());
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid task status.", ex);
    }

    // get tasks
    List<Task> tasks = taskService.getTasksByTeamIdAndStatus(teamId, taskStatusList);

    // if tasks list is empty, return empty list
    if (tasks.isEmpty()) {
      return new ArrayList<>();
    }

    // convert internal representation of tasks back to API
    return tasks.stream()
        .map(DTOMapper.INSTANCE::convertEntityToTaskGetDTO)
        .collect(Collectors.toList());
  }

  @PutMapping("/teams/{teamId}/tasks/{taskId}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public TaskGetDTO updateTask(@PathVariable Long teamId, @PathVariable Long taskId,
      @RequestBody TaskPutDTO taskPutDTO, @RequestHeader("Authorization") String token) {
    // check if user is authorized (valid token) and if the user exists
    authorizationService.isAuthorizedAndBelongsToTeam(token, teamId);

    // convert API task to internal representation
    Task taskInput = DTOMapper.INSTANCE.convertTaskPutDTOtoEntity(taskPutDTO);

    // set the team to the taskInput
    taskInput.setTeam(teamService.getTeamByTeamId(teamId));

    // update task
    Task updatedTask = taskService.updateTask(taskInput, teamId);

    // convert internal representation of task back to API
    return DTOMapper.INSTANCE.convertEntityToTaskGetDTO(updatedTask);
  }
}
