package ch.uzh.ifi.hase.soprafs24.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.uzh.ifi.hase.soprafs24.entity.Session;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.SessionPostDTO;
import ch.uzh.ifi.hase.soprafs24.service.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
@WebMvcTest(SessionController.class)
public class SessionControllerTest {
  @Autowired MockMvc mockMvc;

  @MockBean private AuthorizationService authorizationService;
  @MockBean private SessionService sessionService;

  private Session testSession;
  private LocalDateTime testSessionStartDateTime;
  private Long testSessionGoalMinutes;
  private Session testSessionEnded;
  private LocalDateTime testSessionEndedStartDateTime;
  private LocalDateTime testSessionEndedEndDateTime;
  private Long testSessionEndedGoalMinutes;
  private final String format = "yyy-MM-dd HH:mm:ss";

  @BeforeEach
  public void setup() {
    // given dates and goals
    testSessionStartDateTime = LocalDateTime.now().minusHours(1);
    testSessionGoalMinutes = 30L;
    testSessionEndedStartDateTime = LocalDateTime.now().minusHours(3);
    testSessionEndedEndDateTime = LocalDateTime.now().minusHours(2);
    testSessionEndedGoalMinutes = 60L;

    // given
    testSession = new Session();
    testSession.setSessionId(1L);
    testSession.setStartDateTime(testSessionStartDateTime);
    testSession.setGoalMinutes(testSessionGoalMinutes);

    testSessionEnded = new Session();
    testSessionEnded.setSessionId(2L);
    testSessionEnded.setStartDateTime(testSessionEndedStartDateTime);
    testSessionEnded.setEndDateTime(testSessionEndedEndDateTime);
    testSessionEnded.setGoalMinutes(testSessionEndedGoalMinutes);
  }

  // region create session
  @Test
  public void createSession_validInputs_success() throws Exception {
    // given session post dto
    SessionPostDTO testSessionPostDTO = new SessionPostDTO();
    testSessionPostDTO.setGoalMinutes(testSession.getGoalMinutes());

    // when auth -> ok
    given(authorizationService.isAuthorizedAndBelongsToTeam(Mockito.anyString(), Mockito.anyLong()))
        .willReturn(new User());

    // when create session service -> return test session successfully
    given(sessionService.createSession(Mockito.anyLong(), Mockito.anyLong()))
        .willReturn(testSession);

    // todo pusher mock

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest =
        post("/api/v1/teams/1/sessions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(testSessionPostDTO))
            .header("Authorization", "valid-token");

    // then

    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.sessionId", is(1)))
        .andExpect(jsonPath("$.startDateTime",
            is(testSessionStartDateTime.format(DateTimeFormatter.ofPattern(format)))))
        .andExpect(jsonPath("$.goalMinutes", is(testSession.getGoalMinutes().intValue())))
        .andExpect(jsonPath("$.endDateTime").doesNotExist());
  }

  @Test
  public void createSession_validInputs_alreadyActiveSession_expectsException() throws Exception {
    // given session post dto
    SessionPostDTO testSessionPostDTO = new SessionPostDTO();
    testSessionPostDTO.setGoalMinutes(testSession.getGoalMinutes());

    // when auth -> ok
    given(authorizationService.isAuthorizedAndBelongsToTeam(Mockito.anyString(), Mockito.anyLong()))
        .willReturn(new User());

    // when create session service -> return test session successfully
    given(sessionService.createSession(Mockito.anyLong(), Mockito.anyLong()))
        .willThrow(
            new ResponseStatusException(HttpStatus.CONFLICT, "There is already an active session"));

    // todo pusher mock

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest =
        post("/api/v1/teams/1/sessions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(testSessionPostDTO))
            .header("Authorization", "valid-token");

    // then
    mockMvc.perform(postRequest).andExpect(status().isConflict());
  }

  @Test
  public void createSession_validInputs_notAuthorized_expectsException() throws Exception {
    // given session post dto
    SessionPostDTO testSessionPostDTO = new SessionPostDTO();
    testSessionPostDTO.setGoalMinutes(testSession.getGoalMinutes());

    // when auth -> ok
    given(authorizationService.isAuthorizedAndBelongsToTeam(Mockito.anyString(), Mockito.anyLong()))
        .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authorized"));

    // // when create session service -> return test session successfully
    // given(sessionService.createSession(Mockito.anyLong(),
    // Mockito.anyLong())).willReturn(testSession);

    // todo pusher mock

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest =
        post("/api/v1/teams/1/sessions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(testSessionPostDTO))
            .header("Authorization", "valid-token");

    // then
    mockMvc.perform(postRequest).andExpect(status().isUnauthorized());
  }

  @Test
  public void createSession_validInputs_teamNotFound_expectsException() throws Exception {
    // given session post dto
    SessionPostDTO testSessionPostDTO = new SessionPostDTO();
    testSessionPostDTO.setGoalMinutes(testSession.getGoalMinutes());

    // when auth -> ok
    given(authorizationService.isAuthorizedAndBelongsToTeam(Mockito.anyString(), Mockito.anyLong()))
        .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));

    // // when create session service -> return test session successfully
    // given(sessionService.createSession(Mockito.anyLong(),
    // Mockito.anyLong())).willReturn(testSession);

    // todo pusher mock

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest =
        post("/api/v1/teams/1/sessions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(testSessionPostDTO))
            .header("Authorization", "valid-token");

    // then
    mockMvc.perform(postRequest).andExpect(status().isNotFound());
  }
  // endregion
}
