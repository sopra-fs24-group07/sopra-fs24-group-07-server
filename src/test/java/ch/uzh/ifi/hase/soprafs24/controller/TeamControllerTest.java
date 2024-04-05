package ch.uzh.ifi.hase.soprafs24.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.entity.TeamUser;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TeamPostDTO;
import ch.uzh.ifi.hase.soprafs24.service.AuthorizationService;
import ch.uzh.ifi.hase.soprafs24.service.TeamService;
import ch.uzh.ifi.hase.soprafs24.service.TeamUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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
 * TeamControllerTest
 * This is a WebMvcTest which allows to test the TeamController.
 */
@WebMvcTest(TeamController.class)
public class TeamControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private TeamService teamService;
  @MockBean private AuthorizationService authorizationService;
  @MockBean private TeamUserService teamUserService;

  private User testUser;

  @BeforeEach
  public void setup() {
    testUser = new User();
    testUser.setUserId(1L);
  }

  /**
   * Test for creating a team with valid input
   */
  @Test
  public void createTeam_validInput_teamCreated() throws Exception {
    // given
    Team team = new Team();
    team.setTeamId(1L);
    team.setName("productiviteam");
    team.setDescription("We are the most productive team in sopra");

    TeamPostDTO teamPostDTO = new TeamPostDTO();
    teamPostDTO.setName("productiviteam");
    teamPostDTO.setDescription("We are the most productive team in sopra");

    // mock valid token
    Mockito.when(authorizationService.isAuthorized(Mockito.anyString())).thenReturn(testUser);
    // mock team service
    given(teamService.createTeam(Mockito.any())).willReturn(team);
    // mock add user to team on creation service call
    given(teamUserService.createTeamUser(Mockito.anyLong(), Mockito.anyLong()))
        .willReturn(new TeamUser(team, testUser));

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest =
        post("/api/v1/teams")
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(teamPostDTO))
            .header("Authorization", "1234");

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.teamId", is(team.getTeamId().intValue())))
        .andExpect(jsonPath("$.name", is(team.getName())))
        .andExpect(jsonPath("$.description", is(team.getDescription())));
  }

  /**
   * Test for creating a team with invalid authorization
   */
  @Test
  public void createTeam_validInput_invalidAuth() throws Exception {
    // given
    // Team team = new Team();
    // team.setTeamId(1L);
    // team.setName("productiviteam");
    // team.setDescription("We are the most productive team in sopra");

    TeamPostDTO teamPostDTO = new TeamPostDTO();
    teamPostDTO.setName("productiviteam");
    teamPostDTO.setDescription("We are the most productive team in sopra");

    // mock invalid token -> throw exception
    Mockito.when(authorizationService.isAuthorized(Mockito.anyString()))
        .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest =
        post("/api/v1/teams")
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(teamPostDTO))
            .header("Authorization", "1234");

    // then -> isUnauthorized
    mockMvc.perform(postRequest).andExpect(status().isUnauthorized());
  }

  /**
   * Test for creating a team with invalid input
   */
  @Test
  public void createTeam_invalidInput() throws Exception {
    // given
    Team team = new Team();
    team.setTeamId(1L);
    team.setName("");
    team.setDescription("We are the most productive team in sopra");

    TeamPostDTO teamPostDTO = new TeamPostDTO();
    teamPostDTO.setName(""); // empty name is invalid
    teamPostDTO.setDescription("We are the most productive team in sopra");

    // mock invalid token -> throw exception
    // Mockito.doNothing().when(authorizationService).isAuthorized(Mockito.anyString());
    Mockito.when(authorizationService.isAuthorized(Mockito.anyString())).thenReturn(testUser);

    // mock team service
    given(teamService.createTeam(Mockito.any()))
        .willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST));

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest =
        post("/api/v1/teams")
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(teamPostDTO))
            .header("Authorization", "1234");

    // then -> isUnauthorized
    mockMvc.perform(postRequest).andExpect(status().isBadRequest());
  }

  // region getUsersOfTeam tests
  // Don't need to test if we get empty list of users, because there should always at least one user
  // linked to a team (no rotten green path testing)

  /**
   * Test for getting all users of a team, but not valid token (user token not in db)
   */
  @Test
  public void getUsersOfTeam_invalidToken_throwsError() throws Exception {
    // given test team
    Team testTeam = new Team();
    testTeam.setTeamId(1L);

    // when -> is auth check -> is invalid
    given(authorizationService.isAuthorized(Mockito.anyString()))
        .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

    // when -> perform get request
    MockHttpServletRequestBuilder getRequest =
        get("/api/v1/teams/" + testTeam.getTeamId().toString() + "/users")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "invalid token");

    // then -> validate result for unauthorized
    mockMvc.perform(getRequest)
        .andExpect(status().isUnauthorized())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException));
  }

  /**
   * Test for getting all users of a team, but not valid token (user not in team)
   */
  @Test
  public void getUsersOfTeam_userNotInTeam_throwsError() throws Exception {
    // given test team
    Team testTeam = new Team();
    testTeam.setTeamId(1L);

    // testUser is in team

    // given user that makes request, but is not in teasTeam
    User userNotInTeam = new User();
    userNotInTeam.setUserId(2L);
    userNotInTeam.setToken("userNotInTeamToken");

    // when -> is auth check -> is valid -> but user is not the valid one
    given(authorizationService.isAuthorized(Mockito.anyString())).willReturn(userNotInTeam);
    // when -> get users of team -> testUser is in team, but not the other user
    given(teamUserService.getUsersOfTeam(Mockito.anyLong()))
        .willReturn(java.util.List.of(testUser));

    // when -> perform get request
    MockHttpServletRequestBuilder getRequest =
        get("/api/v1/teams/" + testTeam.getTeamId().toString() + "/users")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", userNotInTeam.getToken()); // request with this token

    // then -> validate result for unauthorized
    mockMvc.perform(getRequest)
        .andExpect(status().isForbidden())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException));
  }

  /**
   * Test for getting all users of a team successfully
   */
  @Test
  public void getUsersOfTeam_validInput() throws Exception {
    // given test team
    Team testTeam = new Team();
    testTeam.setTeamId(1L);

    // testUser is in team

    // when -> is auth check -> is valid
    given(authorizationService.isAuthorized(Mockito.anyString())).willReturn(testUser);
    // when -> get users of team -> testUser is in team
    given(teamUserService.getUsersOfTeam(Mockito.anyLong()))
        .willReturn(java.util.List.of(testUser));

    // when -> perform get request
    MockHttpServletRequestBuilder getRequest =
        get("/api/v1/teams/" + testTeam.getTeamId().toString() + "/users")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "valid-token");

    // then -> validate result for unauthorized
    mockMvc.perform(getRequest)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].userId", is(testUser.getUserId().intValue())))
        .andExpect(jsonPath("$[0].username", is(testUser.getUsername())));
  }

  // endregion
}
