package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TeamGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TeamPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.AuthorizationService;
import ch.uzh.ifi.hase.soprafs24.service.TeamService;
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

  TeamController(TeamService teamService, AuthorizationService authorizationService) {
    this.teamService = teamService;
    this.authorizationService = authorizationService;
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

    // TODO: call service to add user to created team

    // convert internal representation of team back to API
    return DTOMapper.INSTANCE.convertEntityToTeamGetDTO(createdTeam);
  }
}
