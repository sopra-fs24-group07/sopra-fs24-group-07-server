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
import ch.uzh.ifi.hase.soprafs24.service.PusherService;
import ch.uzh.ifi.hase.soprafs24.service.TaskService;
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
 * CommentControllerTest
 * This is a WebMvcTest which allows to test the CommentController.
 */
@WebMvcTest(CommentController.class)
public class CommentControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private CommentService commentService;
  @MockBean private AuthorizationService authorizationService;
  @MockBean private TaskService taskService;
  @MockBean private PusherService pusherService;

  private User testUser;

  @BeforeEach
  public void setup() {
    testUser = new User();
    testUser.setUserId(1L);
  }

  private void mockPusherService() {
    Mockito.doNothing().when(pusherService).updateComments(Mockito.anyString());
  }

  // region Comment Controller POST

  /**
   * Test for creating a Comment with valid input
   */
  @Test
  public void createComment_validInput_commentCreated() throws Exception {
    // given
    Comment comment = new Comment();
    comment.setCommentId(1L);
    comment.setText("This is a test comment.");
    comment.setUser(testUser);

    CommentPostDTO commentPostDTO = new CommentPostDTO();
    commentPostDTO.setText("This is a test comment.");
    commentPostDTO.setUserId(testUser.getUserId());

    // mock valid token
    Mockito
        .when(authorizationService.isAuthorizedAndBelongsToTeam(
            Mockito.anyString(), Mockito.eq(testUser.getUserId()), Mockito.anyLong()))
        .thenReturn(testUser);
    // mock comment service
    given(commentService.createComment(Mockito.any(), Mockito.anyLong())).willReturn(comment);

    // mock pusher service
    mockPusherService();

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
        .andExpect(jsonPath("$.text", is(comment.getText())))
        .andExpect(jsonPath("$.authorId", is(comment.getUser().getUserId().intValue())))
        .andExpect(jsonPath("$.authorName", is(comment.getUser().getName())));

    // verify that pusher service was called
    Mockito.verify(pusherService, Mockito.times(1)).updateComments(Mockito.anyString());
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

    // mock pusher service
    mockPusherService();

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

    // mock pusher service
    mockPusherService();

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

  // region Comment Service Integration GET

  /**
   * Test for fetching a Comment with valid input (happy-path)
   */
  @Test
  public void getComments_validInput_returnComments() throws Exception {
    // given
    Comment comment = new Comment();
    comment.setCommentId(1L);
    comment.setText("This is a test comment.");
    comment.setUser(testUser);

    Mockito
        .when(authorizationService.isAuthorizedAndBelongsToTeam(
            Mockito.anyString(), Mockito.anyLong()))
        .thenReturn(testUser);
    given(commentService.getCommentsByTaskId(Mockito.anyLong())).willReturn(List.of(comment));

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder getRequest =
        get("/api/v1/teams/1/tasks/1/comments").header("Authorization", "1234");

    // then
    mockMvc.perform(getRequest)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].commentId", is(comment.getCommentId().intValue())))
        .andExpect(jsonPath("$[0].text", is(comment.getText())))
        .andExpect(jsonPath("$[0].authorId", is(comment.getUser().getUserId().intValue())))
        .andExpect(jsonPath("$[0].authorName", is(comment.getUser().getName())));
  }

  /**
   * Test for trying to fetch a Comment, where there are no comments in task
   */
  @Test
  public void getComments_noCommentsInTask_throwsError() throws Exception {
    // given
    given(commentService.getCommentsByTaskId(Mockito.anyLong()))
        .willThrow(new ResponseStatusException(
            HttpStatus.NOT_FOUND, "No comments found for task with id 1"));

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder getRequest =
        get("/api/v1/teams/1/tasks/1/comments").header("Authorization", "1234");

    // then
    mockMvc.perform(getRequest)
        .andExpect(status().isNotFound())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
        .andExpect(result
            -> assertTrue(result.getResolvedException().getMessage().contains(
                "No comments found for task with id 1")));
  }
  /**
   * Test for trying to fetch a Comment, where i'm not authorized to access
   */
  @Test
  public void getComments_unauthorizedAccess_throwsError() throws Exception {
    // given
    Mockito
        .doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authorized to access."))
        .when(authorizationService)
        .isAuthorizedAndBelongsToTeam(Mockito.anyString(), Mockito.anyLong());

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder getRequest =
        get("/api/v1/teams/1/tasks/1/comments").header("Authorization", "1234");

    // then
    mockMvc.perform(getRequest)
        .andExpect(status().isUnauthorized())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
        .andExpect(result
            -> assertTrue(
                result.getResolvedException().getMessage().contains("Not authorized to access.")));
  }

  // endregion
}
