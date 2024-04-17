package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Session;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/*
 * Session Controller
 * This class is responsible for handling all REST request that are related to the sessions of a
 * team.
 */
@RestController
@RequestMapping("api/v1")
public class SessionController {
  private final AuthorizationService authorizationService;
  private final SessionService sessionService;

  SessionController(AuthorizationService authorizationService, SessionService sessionService) {
    this.authorizationService = authorizationService;
    this.sessionService = sessionService;
  }

  /**
   * Create a new session for a team.
   *
   * @param teamId the team id of the team to create the session for
   * @param sessionPostDTO the session to create (only goalMinutes is used)
   * @param token the token of the user
   * @throws ResponseStatusException with status 401 if the user is not authorized; with status 404
   *     if the team does not exist; with status 409 if there is already an active session
   * @return the created session
   */
  @PostMapping("/teams/{ID}/sessions")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public SessionGetDTO createSession(@PathVariable("ID") Long teamId,
      @RequestBody SessionPostDTO sessionPostDTO, @RequestHeader("Authorization") String token) {
    // check if user is authorized (valid token) and if the user exists
    User authorizedUser = authorizationService.isAuthorizedAndBelongsToTeam(token, teamId);

    // convert API session to internal representation to get the time goal
    Session sessionInput = DTOMapper.INSTANCE.convertSessionPostDTOtoEntity(sessionPostDTO);

    // create session (409 if active session)
    Session createdSession = sessionService.createSession(teamId, sessionInput.getGoalMinutes());

    // todo pusher call -> also exception handling

    // convert internal representation of session back to API
    return DTOMapper.INSTANCE.convertEntityToSessionGetDTO(createdSession);
  }
}
