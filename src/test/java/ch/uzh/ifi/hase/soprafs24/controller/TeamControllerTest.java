package ch.uzh.ifi.hase.soprafs24.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TeamPostDTO;
import ch.uzh.ifi.hase.soprafs24.service.AuthorizationService;
import ch.uzh.ifi.hase.soprafs24.service.TeamService;
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
    // TODO: mock add user to team on creation service call

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
}
