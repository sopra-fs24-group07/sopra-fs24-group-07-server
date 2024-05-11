package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.AgoraAuth;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.AgoraService;
import ch.uzh.ifi.hase.soprafs24.service.AuthorizationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1")
public class AuthorizationController {
  private final AuthorizationService authorizationService;
  private final AgoraService agoraService;

  AuthorizationController(AuthorizationService authorizationService, AgoraService agoraService) {
    this.authorizationService = authorizationService;
    this.agoraService = agoraService;
  }

  @PostMapping("/login")
  @ResponseBody
  @ResponseStatus(HttpStatus.OK)
  AuthGetDTO login(@RequestBody LoginPostDTO loginPostDTO) {
    User loginUser = DTOMapper.INSTANCE.convertLoginPostDTOtoEntity(loginPostDTO);

    User potentialUser =
        authorizationService.login(loginUser.getUsername(), loginUser.getPassword());

    if (potentialUser != null) {
      return DTOMapper.INSTANCE.convertEntityToAuthGetDTO(potentialUser);
    }
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login failed");
  }

  // @PostMapping("/register")
  // @ResponseBody
  // @ResponseStatus(HttpStatus.CREATED)
  // UserGetDTO register(@RequestBody UserPostDTO userPostDTO) {
  //   User registerUser = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
  //
  //   User createdUser = authorizationService.register(registerUser);
  //
  //   return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
  // }

  /**
   * Agora token generation endpoint
   *
   * @see <a
   *     href="https://docs.agora.io/en/voice-calling/get-started/authentication-workflow?platform=web">Agora
   *     Voice Calling - Secure authentication with tokens</a>
   */
  @PostMapping("/agora/getToken")
  @ResponseBody
  @ResponseStatus(HttpStatus.OK)
  AgoraAuthGetDTO getToken(@RequestBody AgoraAuthPostDTO agoraAuthPostDTO,
      @RequestHeader("Authorization") String userToken) {
    AgoraAuth agoraAuth = DTOMapper.INSTANCE.convertAgoraAuthPostDTOtoEntity(agoraAuthPostDTO);

    // check if user is authorized (valid token) and part of team
    authorizationService.isAuthorizedAndBelongsToTeam(
        userToken, agoraAuth.getUserId(), agoraAuth.getTeamId());

    // get token from AgoraService
    String token = agoraService.getToken(agoraAuth.getUserId(), agoraAuth.getChannelName());

    return DTOMapper.INSTANCE.convertEntityToAgoraAuthGetDTO(token);
  }
}
