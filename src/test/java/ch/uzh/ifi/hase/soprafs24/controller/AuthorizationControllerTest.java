package ch.uzh.ifi.hase.soprafs24.controller;

import static ch.uzh.ifi.hase.soprafs24.controller.ControllerTestHelper.asJsonString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.AgoraAuthPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LoginPostDTO;
import ch.uzh.ifi.hase.soprafs24.service.AgoraService;
import ch.uzh.ifi.hase.soprafs24.service.AuthorizationService;
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

@WebMvcTest(AuthorizationController.class)
public class AuthorizationControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private AuthorizationService authorizationService;
  @MockBean private AgoraService agoraService;
  @MockBean private UserRepository userRepository;

  /**
   * validate if login with valid credentials work
   */
  @Test
  public void loginUser_validInput_successful() throws Exception {
    // given user
    User user = new User();
    user.setUserId(1L);
    user.setToken("token");

    // given
    LoginPostDTO loginPostDTO = new LoginPostDTO();
    loginPostDTO.setUsername("admin");
    loginPostDTO.setPassword("1234");

    given(authorizationService.login(Mockito.anyString(), Mockito.anyString())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/api/v1/login")
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .content(asJsonString(loginPostDTO));

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("token"));
  }

  /**
   * create if login with invalid credentials throws error
   */
  @Test
  public void loginUser_invalidInput_expectException() throws Exception {
    // given user
    User user = new User();
    user.setUserId(1L);
    user.setToken("token");

    // given
    LoginPostDTO loginPostDTO = new LoginPostDTO();
    loginPostDTO.setUsername("admin");
    loginPostDTO.setPassword("wrong password");

    given(authorizationService.login(Mockito.anyString(), Mockito.anyString())).willReturn(null);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/api/v1/login")
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .content(asJsonString(loginPostDTO));

    // then expect error on call
    mockMvc.perform(postRequest).andExpect(status().isUnauthorized());
  }

  // region Agora authentication
  @Test
  public void testGetToken_success() throws Exception {
    // given
    Long userId = 1L;
    Long teamId = 1L;
    String channelName = "channelName";

    AgoraAuthPostDTO agoraAuthPostDTO = new AgoraAuthPostDTO();
    agoraAuthPostDTO.setUserId(userId);
    agoraAuthPostDTO.setTeamId(teamId);
    agoraAuthPostDTO.setChannelName(channelName);

    // when -> authorized and part of team -> success
    given(authorizationService.isAuthorizedAndBelongsToTeam(
              Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong()))
        .willReturn(new User());

    // when -> get token -> return testToken
    given(agoraService.getToken(Mockito.anyLong(), Mockito.anyString())).willReturn("testToken");

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/api/v1/agora/getToken")
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .content(asJsonString(agoraAuthPostDTO))
                                                    .header("Authorization", "1234");

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("testToken"));

    // verify service was called
    Mockito.verify(agoraService, Mockito.times(1)).getToken(Mockito.anyLong(), Mockito.anyString());
  }

  @Test
  public void testGetToken_rtcTokenBuilderException() throws Exception {
    // given
    Long userId = 1L;
    Long teamId = 1L;
    String channelName = "channelName";

    AgoraAuthPostDTO agoraAuthPostDTO = new AgoraAuthPostDTO();
    agoraAuthPostDTO.setUserId(userId);
    agoraAuthPostDTO.setTeamId(teamId);
    agoraAuthPostDTO.setChannelName(channelName);

    // when -> authorized and part of team -> success
    given(authorizationService.isAuthorizedAndBelongsToTeam(
              Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong()))
        .willReturn(new User());

    // when -> get token -> throw exception
    given(agoraService.getToken(Mockito.anyLong(), Mockito.anyString()))
        .willThrow(new ResponseStatusException(
            HttpStatus.SERVICE_UNAVAILABLE, "Error while generating Agora token: Test exception"));

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/api/v1/agora/getToken")
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .content(asJsonString(agoraAuthPostDTO))
                                                    .header("Authorization", "1234");

    // then
    mockMvc.perform(postRequest).andExpect(status().isServiceUnavailable());
    // verify service was called\
    Mockito.verify(agoraService, Mockito.times(1)).getToken(Mockito.anyLong(), Mockito.anyString());
  }

  @Test
  public void testGetToken_unauthorized() throws Exception {
    // given
    Long userId = 1L;
    Long teamId = 1L;
    String channelName = "channelName";

    AgoraAuthPostDTO agoraAuthPostDTO = new AgoraAuthPostDTO();
    agoraAuthPostDTO.setUserId(userId);
    agoraAuthPostDTO.setTeamId(teamId);
    agoraAuthPostDTO.setChannelName(channelName);

    // when -> authorized and part of team -> throw exception
    given(authorizationService.isAuthorizedAndBelongsToTeam(
              Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong()))
        .willThrow(new ResponseStatusException(
            HttpStatus.UNAUTHORIZED, "User is not authorized to access this resource"));

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/api/v1/agora/getToken")
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .content(asJsonString(agoraAuthPostDTO))
                                                    .header("Authorization", "1234");

    // then
    mockMvc.perform(postRequest).andExpect(status().isUnauthorized());
    // verify service was called
    Mockito.verify(agoraService, Mockito.never()).getToken(Mockito.anyLong(), Mockito.anyString());
  }

  @Test
  public void testGetToken_invalidFormat() throws Exception {
    // given
    Long userId = 1L;
    Long teamId = 1L;
    String channelName = "   ";

    AgoraAuthPostDTO agoraAuthPostDTO = new AgoraAuthPostDTO();
    agoraAuthPostDTO.setUserId(userId);
    agoraAuthPostDTO.setTeamId(teamId);
    agoraAuthPostDTO.setChannelName(channelName);

    // when -> authorized and part of team -> success
    given(authorizationService.isAuthorizedAndBelongsToTeam(
              Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong()))
        .willReturn(new User());

    // when -> get token -> return testToken
    given(agoraService.getToken(Mockito.anyLong(), Mockito.anyString()))
        .willThrow(new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "channelName cannot be empty or only whitespace!"));

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/api/v1/agora/getToken")
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .content(asJsonString(agoraAuthPostDTO))
                                                    .header("Authorization", "1234");

    // then
    mockMvc.perform(postRequest).andExpect(status().isBadRequest());
    // verify service was called
    Mockito.verify(agoraService, Mockito.times(1)).getToken(Mockito.anyLong(), Mockito.anyString());
  }

  // endregion
}
