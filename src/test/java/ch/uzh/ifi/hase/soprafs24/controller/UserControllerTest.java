package ch.uzh.ifi.hase.soprafs24.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.service.AuthorizationService;
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

  @Test
  public void updateUser_validInput_userUpdated() throws Exception {
    // given
    User user = new User();
    user.setUserId(1L);
    user.setName("Test User Updated");
    user.setUsername("testUsernameUpdated");

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setName("Test User Updated");
    userPostDTO.setUsername("testUsernameUpdated");

    given(userService.updateUser(Mockito.any())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder putRequest =
        put("/api/v1/users/" + user.getUserId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(userPostDTO));

    // then
    mockMvc.perform(putRequest)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId", is(user.getUserId().intValue())))
        .andExpect(jsonPath("$.name", is(user.getName())))
        .andExpect(jsonPath("$.username", is(user.getUsername())));
  }

  @Test
  public void deleteUser_validInput_userDeleted() throws Exception {
    // given
    User user = new User();
    user.setUserId(1L);
    user.setName("Test User");
    user.setUsername("testUsername");

    // when -> do the request + validate the result
    MockHttpServletRequestBuilder deleteRequest = delete("/api/v1/users/" + user.getUserId());

    // then
    mockMvc.perform(deleteRequest).andExpect(status().isOk());
  }
}
