package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.AuthGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LoginPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.AuthorizationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1")
public class AuthorizationController {
  private final AuthorizationService authorizationService;

  AuthorizationController(AuthorizationService authorizationService) {
    this.authorizationService = authorizationService;
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
}
