package ch.uzh.ifi.hase.soprafs24.controller;

import static ch.uzh.ifi.hase.soprafs24.controller.ControllerTestHelper.asJsonString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.service.AIService;
import ch.uzh.ifi.hase.soprafs24.service.AuthorizationService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(AIController.class)
public class AIControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private AIService aiService;
  @MockBean private AuthorizationService authorizationService;

  /**
   * Test for callGpt35Instruct method with valid input.
   * Ensures that the method calls the AI Service and returns a non-null result.
   */
  @Test
  public void callGpt35Instruct_validInput_successful() throws Exception {
    // Given a user with a valid token
    User user = new User();
    user.setUserId(1L);
    user.setToken("token");
  
    // And a valid request body
    String requestBody = "The Warriors";
  
    // When the user is authorized and the AI Service returns a description
    given(authorizationService.isAuthorized(anyString())).willReturn(user);
    given(aiService.generateDescription(anyString())).willReturn(Optional.of("Some description"));
  
    // Construct the POST request
    MockHttpServletRequestBuilder postRequest = post("/api/v1/ai/gpt-3.5-turbo-instruct")
                                                  .header("Authorization", "Bearer token")
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(asJsonString(requestBody));
  
    // Then the request should be successful and the response should not be null or empty
    mockMvc.perform(postRequest)
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.notNullValue()))
        .andExpect(content().string(org.hamcrest.Matchers.not("")));
  }

  /**
   * Test for callGpt35Instruct method with invalid token.
   * Ensures that the method returns Unauthorized status.
   */
  @Test
  public void callGpt35Instruct_invalidToken_unauthorized() throws Exception {
    // Given a request body
    String requestBody = "The Warriors";

    // When the user is not authorized
    given(authorizationService.isAuthorized(anyString())).willReturn(null);

    // Construct the POST request
    MockHttpServletRequestBuilder postRequest = post("/api/v1/ai/gpt-3.5-turbo-instruct")
                                                    .header("Authorization", "Bearer invalid_token")
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .content(asJsonString(requestBody));

    // Then the request should return Unauthorized status
    mockMvc.perform(postRequest).andExpect(status().isUnauthorized());
  }

  /**
   * Test for callGpt35Instruct method when AI Service fails.
   * Ensures that the method handles the exception thrown by AI Service.
   */
  @Test
  public void callGpt35Instruct_aiServiceFails_handleException() throws Exception {
    // Given a user with a valid token
    User user = new User();
    user.setUserId(1L);
    user.setToken("token");
  
    // And a valid request body
    String requestBody = "The Warriors";
  
    // When the user is authorized but the AI Service fails
    given(authorizationService.isAuthorized(anyString())).willReturn(user);
    given(aiService.generateDescription(anyString())).willThrow(new RuntimeException("AI Service failed"));
  
    // Construct the POST request
    MockHttpServletRequestBuilder postRequest = post("/api/v1/ai/gpt-3.5-turbo-instruct")
                                                    .header("Authorization", "Bearer token")
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .content(asJsonString(requestBody));
  
    // Then the request should return Bad Request status
    mockMvc.perform(postRequest)
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Unable to call AI service"));
  }

  /**
   * Test for callGpt35Instruct method with invalid input.
   * Ensures that the method returns Bad Request status.
   */
  @Test
  public void callGpt35Instruct_invalidInput_badRequest() throws Exception {
    // Given a user with a valid token
    User user = new User();
    user.setUserId(1L);
    user.setToken("token");
  
    // And an invalid request body
    String requestBody = "The Warriors";
  
    // When the user is authorized but the request body is invalid
    given(authorizationService.isAuthorized(anyString())).willReturn(user);
    given(aiService.generateDescription(anyString())).willThrow(new IllegalArgumentException("Invalid input"));
  
    // Construct the POST request
    MockHttpServletRequestBuilder postRequest = post("/api/v1/ai/gpt-3.5-turbo-instruct")
                                                .header("Authorization", "Bearer token")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(asJsonString(requestBody));
  
    // Then the request should return Bad Request status
    mockMvc.perform(postRequest).andExpect(status().isBadRequest());
  }
}
