package ch.uzh.ifi.hase.soprafs24.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.service.AuthorizationService;
import ch.uzh.ifi.hase.soprafs24.service.TeamUserService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
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
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private UserService userService;
  @MockBean private AuthorizationService authorizationService;
  @MockBean private TeamUserService teamUserService;

  // region createUser
  @Test
  public void createUser_validInput_userCreated() throws Exception {
    // given
    User user = new User();
    user.setUserId(1L);
    user.setName("Test User");
    user.setUsername("testUsername");
    user.setToken("1");

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setName("Test User");
    userPostDTO.setUsername("testUsername");

    given(userService.createUser(Mockito.any())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest =
        post("/api/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(userPostDTO))
            .header("Authorization", "1234");

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.userId", is(user.getUserId().intValue())))
        .andExpect(jsonPath("$.name", is(user.getName())))
        .andExpect(jsonPath("$.username", is(user.getUsername())));
  }

  // Alihan TEST:
  @Test
  public void createUser_duplicateUsername_throwsError() throws Exception {
    // given
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setName("Test User");
    userPostDTO.setUsername("testUsername");

    given(userService.createUser(Mockito.any()))
        .willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists"));

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest =
        post("/api/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(userPostDTO))
            .header("Authorization", "1234");

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isBadRequest())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
        .andExpect(result
            -> assertTrue(
                result.getResolvedException().getMessage().contains("Username already exists")));
  }

  // endregion

  // region update user

  // Alihan: Update User happy path
  @Test
  public void updateUser_validInput_userUpdated() throws Exception {
    // given updated user data
    User user = new User();
    user.setUserId(1L);
    user.setName("Test User Updated");
    user.setUsername("testUsernameUpdated");

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setName("Test User Updated");
    userPostDTO.setUsername("testUsernameUpdated");

    // when -> check existing and authorized user -> ok
    given(authorizationService.isExistingAndAuthorized(Mockito.anyString(), Mockito.anyLong()))
        .willReturn(user);

    // when -> update user -> return updated user
    given(userService.updateUser(Mockito.any())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder putRequest =
        put("/api/v1/users/" + user.getUserId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(userPostDTO))
            .header("Authorization", "1234");

    // then
    mockMvc.perform(putRequest)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId", is(user.getUserId().intValue())))
        .andExpect(jsonPath("$.name", is(user.getName())))
        .andExpect(jsonPath("$.username", is(user.getUsername())));
  }

  // Alihan: Update User; Test case for PUT method where user doesn't exist
  @Test
  public void updateUser_nonExistingUser_throwsError() throws Exception {
    // given
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setName("Test User Updated");
    userPostDTO.setUsername("testUsernameUpdated");

    // when -> check existing and authorized user -> user not found
    given(authorizationService.isExistingAndAuthorized(Mockito.anyString(), Mockito.anyLong()))
        .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    // not needed because auth checks that
    // given(userService.updateUser(Mockito.any()))
    //     .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder putRequest =
        put("/api/v1/users/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(userPostDTO))
            .header("Authorization", "1234");

    // then
    mockMvc.perform(putRequest).andExpect(status().isNotFound());
  }

  // Alihan: Update User; Test case for PUT method where token does not correspond to user's id or
  // token is not valid (not found)
  @Test
  public void updateUser_notAuthorizedUser_throwsError() throws Exception {
    // given
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setName("Test User Updated");
    userPostDTO.setUsername("testUsernameUpdated");

    // when -> check existing and authorized user -> not authorized
    given(authorizationService.isExistingAndAuthorized(Mockito.anyString(), Mockito.anyLong()))
        .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token"));

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder putRequest =
        put("/api/v1/users/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(userPostDTO))
            .header("Authorization", "1234");

    // then
    mockMvc.perform(putRequest).andExpect(status().isUnauthorized());
  }

  // Alihan: Update User; Test case for PUT method where user tries to update with invalid input
  @Test
  public void updateUser_invalidInput_throwsError() throws Exception {
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setName("");
    userPostDTO.setUsername("");

    // when -> check existing and authorized user -> ok
    given(authorizationService.isExistingAndAuthorized(Mockito.anyString(), Mockito.anyLong()))
        .willReturn(new User());

    // when -> update user -> invalid input
    given(userService.updateUser(Mockito.any()))
        .willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid input"));

    MockHttpServletRequestBuilder putRequest =
        put("/api/v1/users/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(userPostDTO))
            .header("Authorization", "1234");

    mockMvc.perform(putRequest).andExpect(status().isBadRequest());
  }

  // endregion

  // region delete user

  // Alihan: Delete user (happy path)
  @Test
  public void deleteUser_validInput_success() throws Exception {
    // given
    User user = new User();
    user.setUserId(1L);
    user.setName("Test User");
    user.setUsername("testUsername");
    user.setToken("1234");

    // when -> check existing and authorized user -> ok
    given(authorizationService.isExistingAndAuthorized(Mockito.anyString(), Mockito.anyLong()))
        .willReturn(user);

    // when -> do the request
    MockHttpServletRequestBuilder deleteRequest =
        delete("/api/v1/users/" + user.getUserId()).header("Authorization", "1234");

    // then
    mockMvc.perform(deleteRequest).andExpect(status().isOk());

    // verify that the userService.deleteUser method was called once
    verify(userService, times(1)).deleteUser(user.getUserId());
  }

  // Alihan: Delete User; Test case for DELETE method where user is not authorized (token invalid or
  // not found)
  @Test
  public void deleteUser_notAuthorized_throwsError() throws Exception {
    // when -> check existing and authorized user -> not authorized
    given(authorizationService.isExistingAndAuthorized(Mockito.anyString(), Mockito.anyLong()))
        .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

    MockHttpServletRequestBuilder deleteRequest =
        delete("/api/v1/users/1").header("Authorization", "invalid token");

    mockMvc.perform(deleteRequest).andExpect(status().isUnauthorized());
  }

  // Alihan: Delete User; Test case for DELETE method where user is not found
  @Test
  public void deleteUser_nonExistingUser_throwsError() throws Exception {
    // when -> check existing and authorized user -> not authorized
    given(authorizationService.isExistingAndAuthorized(Mockito.anyString(), Mockito.anyLong()))
        .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

    MockHttpServletRequestBuilder deleteRequest =
        delete("/api/v1/users/1").header("Authorization", "1234");

    mockMvc.perform(deleteRequest).andExpect(status().isNotFound());
  }

  // endregion

  // region get teams by user tests
  /**
   * Test for getting all teams of a user, but not valid token
   */
  @Test
  public void getTeamsByUser_invalidToken_throwsError() throws Exception {
    // given test user
    User testUser = new User();
    testUser.setUserId(1L);
    testUser.setToken("1");

    // when -> is auth check -> is invalid
    given(authorizationService.isExistingAndAuthorized(Mockito.anyString(), Mockito.anyLong()))
        .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

    // when -> perform get request
    MockHttpServletRequestBuilder getRequest =
        get("/api/v1/users/" + testUser.getUserId().toString() + "/teams")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "invalid token");

    // then -> validate result for unauthorized
    mockMvc.perform(getRequest)
        .andExpect(status().isUnauthorized())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException));
  }

  /**
   * Test for getting all teams of a user, but user does not exist (wrong userId in uri)
   */
  @Test
  public void getTeamsByUser_userDoesNotExist_throwsError() throws Exception {
    // given test user
    User testUser = new User();
    testUser.setUserId(1L);
    testUser.setToken("1");

    // when -> is auth check -> is valid
    given(authorizationService.isExistingAndAuthorized(Mockito.anyString(), Mockito.anyLong()))
        .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

    // when -> service request to get all teams of a user -> return empty list
    // given(teamUserService.getTeamsOfUser(Mockito.anyLong()))
    //     .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

    // when -> perform get request
    MockHttpServletRequestBuilder getRequest = get("/api/v1/users/42/teams")
                                                   .contentType(MediaType.APPLICATION_JSON)
                                                   .header("Authorization", testUser.getToken());

    // then -> validate result for not found
    mockMvc.perform(getRequest)
        .andExpect(status().isNotFound())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException));
  }

  /**
   * Test for getting all teams of a user with no link to another team
   */
  @Test
  public void getTeamsByUser_noTeams_emptyList() throws Exception {
    // given test user
    User testUser = new User();
    testUser.setUserId(1L);
    testUser.setToken("1");

    // when -> is auth check -> is valid
    given(authorizationService.isExistingAndAuthorized(Mockito.anyString(), Mockito.anyLong()))
        .willReturn(testUser);

    // when -> service request to get all teams of a user -> return empty list
    given(teamUserService.getTeamsOfUser(Mockito.anyLong())).willReturn(java.util.List.of());

    // when -> perform get request
    MockHttpServletRequestBuilder getRequest =
        get("/api/v1/users/" + testUser.getUserId().toString() + "/teams")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", testUser.getToken());

    // then -> validate result for empty list of teamGetDTOs
    mockMvc.perform(getRequest).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
  }

  /**
   * Test for getting all teams of a user with one link to another team
   */
  @Test
  public void getTeamsByUser_withTeams_jsonArrayList() throws Exception {
    // given test user
    User testUser = new User();
    testUser.setUserId(1L);
    testUser.setToken("1");

    // given test team (which user is linked to)
    Team testTeam = new Team();
    testTeam.setTeamId(1L);
    testTeam.setName("productiviTeam");
    testTeam.setDescription("We are a productive team!");

    // when -> is auth check -> is valid
    given(authorizationService.isExistingAndAuthorized(Mockito.anyString(), Mockito.anyLong()))
        .willReturn(testUser);

    // when -> service request to get all teams of a user -> return empty list
    given(teamUserService.getTeamsOfUser(Mockito.anyLong()))
        .willReturn(java.util.List.of(testTeam));

    // when -> perform get request
    MockHttpServletRequestBuilder getRequest =
        get("/api/v1/users/" + testUser.getUserId().toString() + "/teams")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", testUser.getToken());

    // then -> validate result for empty list of teamGetDTOs
    mockMvc.perform(getRequest)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].teamId", is(testTeam.getTeamId().intValue())))
        .andExpect(jsonPath("$[0].name", is(testTeam.getName())))
        .andExpect(jsonPath("$[0].description", is(testTeam.getDescription())));
  }
  // endregion
}
