package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs24.entity.Comment;
import ch.uzh.ifi.hase.soprafs24.entity.Task;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.CommentRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CommentServiceTest {
  @Mock private CommentRepository commentRepository;
  @Mock private TaskService taskService;

  @InjectMocks private CommentService commentService;

  private Comment testComment;
  private Task testTask;
  private User testUser;
  private List<Comment> comments;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);

    // given
    testComment = new Comment();
    testComment.setCommentId(1L);
    testComment.setText("This is a test comment");
    testComment.setCreationDate(LocalDateTime.now());

    testTask = new Task();
    testTask.setTaskId(1L);
    testTask.setTitle("task1");

    testUser = new User();
    testUser.setUserId(1L);
    testUser.setUsername("user1");

    testComment.setUser(testUser);

    comments = new ArrayList<>();
    comments.add(testComment);

    // when -> any object is being save in the commentRepository -> return the dummy testComment
    Mockito.when(commentRepository.save(Mockito.any())).thenReturn(testComment);
  }

  // POST
  /**
   * Test for creating a new comment with valid inputs
   */
  @Test
  public void createComment_validInputs_success() {
    // when -> try to find taskId in the taskService -> return dummy task
    Mockito.when(taskService.getTask(Mockito.any())).thenReturn(testTask);

    // when -> any object is being saved in the commentRepository -> return the dummy testComment
    Comment createdComment = commentService.createComment(testComment, testTask.getTaskId());

    // then
    Mockito.verify(commentRepository, Mockito.times(1)).save(Mockito.any());

    // check that comment objects are expected
    assertEquals(testComment.getCommentId(), createdComment.getCommentId());
    assertEquals(testComment.getText(), createdComment.getText());
    assertEquals(testComment.getCreationDate(), createdComment.getCreationDate());
    assertEquals(testComment.getTask(), createdComment.getTask());
    assertEquals(testComment.getUser(), createdComment.getUser());
  }

  /**
   * Test for creating a new comment with missing fields throws exception
   */
  @Test
  public void createComment_missingFields_throwsException() {
    // given
    Comment incompleteComment = new Comment();
    incompleteComment.setText(null);
    incompleteComment.setUser(testUser);

    // when/then -> try to create comment with missing fields -> should throw an exception
    assertThrows(ResponseStatusException.class,
        () -> commentService.createComment(incompleteComment, testTask.getTaskId()));
  }

  /**
   * Test for creating a new comment with empty fields throws exception
   */
  @Test
  public void createComment_emptyFields_throwsException() {
    // given
    Comment emptyComment = new Comment();
    emptyComment.setText("");
    emptyComment.setUser(testUser);

    // when/then -> try to create comment with empty fields -> should throw an exception
    assertThrows(ResponseStatusException.class,
        () -> commentService.createComment(emptyComment, testTask.getTaskId()));
  }

  // GET

  /**
   * Test for getting all comments of a task if task exists and has comments
   */
  @Test
  public void getCommentsByTaskId_validInputs_success() {
    // when -> try to find taskId in the taskService -> return dummy task
    Mockito.when(taskService.getTask(Mockito.any())).thenReturn(testTask);

    // when -> try to find comments by task in the commentRepository -> return list with dummy
    // comment
    Mockito.when(commentRepository.findByTask(Mockito.any())).thenReturn(comments);

    // call the method under test
    List<Comment> foundComments = commentService.getCommentsByTaskId(testTask.getTaskId());

    // assert found list with one comment
    assertEquals(1, foundComments.size());
    assertEquals(testComment, foundComments.get(0));
  }

  /**
   * Test for getting all comments of a task if task exists but has no comments
   */
  @Test
  public void getCommentsByTaskId_validInputs_noComments() {
    // when -> try to find taskId in the taskService -> return dummy task
    Mockito.when(taskService.getTask(Mockito.any())).thenReturn(testTask);

    // when -> try to find comments by task in the commentRepository -> return empty list
    Mockito.when(commentRepository.findByTask(Mockito.any())).thenReturn(new ArrayList<>());

    // call the method under test
    List<Comment> foundComments = commentService.getCommentsByTaskId(testTask.getTaskId());

    // assert found empty list
    assertEquals(0, foundComments.size());
  }

  /**
   * Test for getting all comments of a task if task does not exist
   */
  @Test
  public void getCommentsByTaskId_invalidInputs_taskDoesNotExist_throwsException() {
    // when -> try to find taskId in the taskService -> return null
    Mockito.when(taskService.getTask(Mockito.any())).thenReturn(null);

    // call the method under test and assert an exception is thrown
    assertThrows(ResponseStatusException.class,
        () -> commentService.getCommentsByTaskId(testTask.getTaskId()));
  }
}
