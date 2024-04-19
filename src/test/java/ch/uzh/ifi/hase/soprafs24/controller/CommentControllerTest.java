package ch.uzh.ifi.hase.soprafs24.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.uzh.ifi.hase.soprafs24.entity.Comment;
import ch.uzh.ifi.hase.soprafs24.entity.Task;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.CommentPostDTO;
import ch.uzh.ifi.hase.soprafs24.service.AuthorizationService;
import ch.uzh.ifi.hase.soprafs24.service.CommentService;
import ch.uzh.ifi.hase.soprafs24.service.TaskService;
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
 * CommentControllerTest
 * This is a WebMvcTest which allows to test the CommentController.
 */
@WebMvcTest(CommentController.class)
public class CommentControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private CommentService commentService;
  @MockBean private AuthorizationService authorizationService;
  @MockBean private TaskService taskService;

  private User testUser;

  @BeforeEach
  public void setup() {
    testUser = new User();
    testUser.setUserId(1L);
  }

  /**
   * Test for creating a Comment with valid input
   */
  @Test
  public void createComment_validInput_commentCreated() throws Exception {
    // given
    Comment comment = new Comment();
    comment.setCommentId(1L);
    comment.setText("This is a test comment.");

    CommentPostDTO commentPostDTO = new CommentPostDTO();
    commentPostDTO.setText("This is a test comment.");
    commentPostDTO.setUserId(1L);

    // mock valid token
    Mockito
        .when(authorizationService.isAuthorizedAndBelongsToTeam(
            Mockito.anyString(), Mockito.eq(1L), Mockito.anyLong()))
        .thenReturn(testUser);
    // mock comment service
    given(commentService.createComment(Mockito.any(), Mockito.anyLong())).willReturn(comment);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest =
        post("/api/v1/teams/1/tasks/1/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(commentPostDTO))
            .header("Authorization", "1234");

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.commentId", is(comment.getCommentId().intValue())))
        .andExpect(jsonPath("$.text", is(comment.getText())));
  }

  /**
   * Test for creating a Comment with missing Fields
   */
  @Test
  public void createComment_missingFields_throwsError() throws Exception {
    // given
    CommentPostDTO commentPostDTO = new CommentPostDTO();

    User mockUser = new User();
    mockUser.setUserId(1L);
    mockUser.setToken("1234");

    // mock the return of isAuthorizedAndBelongsToTeam()
    Mockito
        .when(authorizationService.isAuthorizedAndBelongsToTeam(
            Mockito.anyString(), Mockito.eq(1L), Mockito.anyLong()))
        .thenReturn(mockUser);
    given(commentService.createComment(Mockito.any(), Mockito.anyLong()))
        .willThrow(
            new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment text cannot be null."));

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest =
        post("/api/v1/teams/1/tasks/1/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(commentPostDTO))
            .header("Authorization", "1234");

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isBadRequest())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
        .andExpect(result
            -> assertTrue(result.getResolvedException().getMessage().contains(
                "Comment text cannot be null.")));
  }

  /**
   * Test for creating a Comment where User has unauthorized Access
   */
  @Test
  public void createComment_unauthorizedAccess_throwsError() throws Exception {
    // given
    CommentPostDTO commentPostDTO = new CommentPostDTO();
    commentPostDTO.setText("This is a test comment.");
    commentPostDTO.setUserId(1L);

    Mockito
        .doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authorized to access."))
        .when(authorizationService)
        .isAuthorizedAndBelongsToTeam(Mockito.any(), Mockito.eq(1L), Mockito.any());

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest =
        post("/api/v1/teams/1/tasks/1/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(ControllerTestHelper.asJsonString(commentPostDTO))
            .header("Authorization", "1234");

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isUnauthorized())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
        .andExpect(result
            -> assertTrue(
                result.getResolvedException().getMessage().contains("Not authorized to access.")));
  }
}
