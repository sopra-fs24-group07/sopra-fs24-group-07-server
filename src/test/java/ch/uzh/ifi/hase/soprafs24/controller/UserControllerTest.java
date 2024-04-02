package ch.uzh.ifi.hase.soprafs24.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

  // region get teams by user tests

  /**
   * Test for getting all teams of a user with no link to another team
   */
  @Test
  public void getTeamsByUser_noTeams_emptyList() throws Exception {
    // given test user
    User testUser = new User();
    testUser.setUserId(1L);

    // when -> is auth check -> is valid
    given(authorizationService.isAuthorized(Mockito.anyString())).willReturn(testUser);

    // when -> service request to get all teams of a user -> return empty list
    given(teamUserService.getTeamsOfUser(Mockito.anyLong())).willReturn(java.util.List.of());

    // when -> perform get request
    MockHttpServletRequestBuilder getRequest =
        get("/api/v1/users/" + testUser.getUserId().toString() + "/teams")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "1");

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

    // given test team (which user is linked to)
    Team testTeam = new Team();
    testTeam.setTeamId(1L);
    testTeam.setName("productiviTeam");
    testTeam.setDescription("We are a productive team!");

    // when -> is auth check -> is valid
    given(authorizationService.isAuthorized(Mockito.anyString())).willReturn(testUser);

    // when -> service request to get all teams of a user -> return empty list
    given(teamUserService.getTeamsOfUser(Mockito.anyLong()))
        .willReturn(java.util.List.of(testTeam));

    // when -> perform get request
    MockHttpServletRequestBuilder getRequest =
        get("/api/v1/users/" + testUser.getUserId().toString() + "/teams")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "1");

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
