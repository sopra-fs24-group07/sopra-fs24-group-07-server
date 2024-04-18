package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs24.entity.Comment;
import ch.uzh.ifi.hase.soprafs24.entity.Task;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.CommentRepository;
import ch.uzh.ifi.hase.soprafs24.repository.TaskRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

/**
 * Test class for the CommentServiceIntegration.
 *
 * @see CommentService
 */
@WebAppConfiguration
@SpringBootTest
public class CommentServiceIntegrationTest {
  @Qualifier("commentRepository") @Autowired private CommentRepository commentRepository;

  @Qualifier("taskRepository") @Autowired private TaskRepository taskRepository;

  @Qualifier("userRepository") @Autowired private UserRepository userRepository;

  @Autowired private CommentService commentService;

  @BeforeEach
  public void setup() {
    commentRepository.deleteAll();
    taskRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  public void createComment_validInputs_success() {
    // given a task
    Task testTask = new Task();
    testTask.setTitle("Test Task");
    testTask.setDescription("This is a task for testing");
    taskRepository.saveAndFlush(testTask);

    // given a user
    User testUser = new User();
    testUser.setUsername("testUser");
    testUser.setName("Test User");
    testUser.setPassword("password123");
    testUser.setToken("1");
    userRepository.saveAndFlush(testUser);

    // given a comment
    Comment testComment = new Comment();
    testComment.setText("This is a test comment");
    testComment.setTask(testTask);
    testComment.setUser(testUser);

    // when
    Comment createdComment = commentService.createComment(testComment, testTask.getTaskId());

    // then
    assertNotNull(createdComment.getCreationDate());
    assertEquals(testComment.getText(), createdComment.getText());
    assertEquals(testTask.getTaskId(), createdComment.getTask().getTaskId());
    assertEquals(testUser.getUserId(), createdComment.getUser().getUserId());
  }
}
