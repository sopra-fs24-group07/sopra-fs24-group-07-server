package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.entity.TeamUser;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TeamGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TeamInvitationPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.AuthorizationService;
import ch.uzh.ifi.hase.soprafs24.service.TeamUserService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.service.PusherService; //PUSHER

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
@RequestMapping("api/v1")
public class UserController {
  private final UserService userService;
  private final AuthorizationService authorizationService;
  private final TeamUserService teamUserService;
  private final PusherService pusherService; //PUSHER

  UserController(UserService userService, AuthorizationService authorizationService,
      TeamUserService teamUserService, PusherService pusherService) { //PUSHER ADDED
    this.userService = userService;
    this.authorizationService = authorizationService;
    this.teamUserService = teamUserService;
    this.pusherService = pusherService; //PUSHER ADDED
  }

  // Account creation
  @PostMapping("/users")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
    // convert API user to internal representation
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    // create user
    User createdUser = userService.createUser(userInput);
    // convert internal representation of user back to API
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
  }

  @PutMapping("/users/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public UserGetDTO updateUser(@PathVariable Long id, @RequestBody UserPostDTO userPostDTO,
      @RequestHeader("Authorization") String token) {
    // Check if the user exists and the token is valid
    authorizationService.isExistingAndAuthorized(token, id);

    // convert API user to internal representation
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
    userInput.setUserId(id);

    // update user
    User updatedUser = userService.updateUser(userInput);

    // convert internal representation of user back to API
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(updatedUser);
  }

  @DeleteMapping("/users/{id}")
  @ResponseStatus(HttpStatus.OK)
  public void deleteUser(@PathVariable Long id, @RequestHeader("Authorization") String token) {
    // Check if the user exists and the token is valid
    authorizationService.isExistingAndAuthorized(token, id);

    userService.deleteUser(id);
  }

  // Get teams of user
  @GetMapping("/users/{userId}/teams")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<TeamGetDTO> getTeamsOfUser(
      @PathVariable Long userId, @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
    // check if user is authorized
    User user = authorizationService.isExistingAndAuthorized(token, userId);

    // get teams of user
    List<Team> teams = teamUserService.getTeamsOfUser(userId);

    // convert internal representation of teams back to API
    List<TeamGetDTO> teamGetDTOs = new ArrayList<>();
    for (Team team : teams) {
      teamGetDTOs.add(DTOMapper.INSTANCE.convertEntityToTeamGetDTO(team));
    }

    return teamGetDTOs;
  }

  /**
   * Add user to team with team invitation.
   */
  @PostMapping("/users/{userId}/teams")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public TeamGetDTO userAcceptsTeamInvitation(@PathVariable Long userId,
      @RequestBody TeamInvitationPostDTO teamInvitationPostDTO,
      @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
    // check if user is authorized
    User user = authorizationService.isExistingAndAuthorized(token, userId);

    // convert team invitation uuid to team instance with only team uuid
    // String teamUUID =
    // DTOMapper.INSTANCE.convertTeamInvitationPostDTOtoString(teamInvitationPostDTO);
    String teamUUID = teamInvitationPostDTO.getTeamUUID();

    // add user to existing team
    TeamUser createdTeamUser = teamUserService.createTeamUser(teamUUID, user.getUserId());

    //update pusher
    // teamId not present and no way to make pusher call to correct channel
    //because pusher in client cant subscribe to a channel with teamUUID we need to somehow get the teamId in order
    //to update members
    //pusherService.updateTeam(teamId.toString());

    return DTOMapper.INSTANCE.convertEntityToTeamGetDTO(createdTeamUser.getTeam());
  }
}
