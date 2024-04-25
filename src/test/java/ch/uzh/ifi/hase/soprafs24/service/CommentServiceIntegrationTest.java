package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs24.constant.TaskStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Comment;
import ch.uzh.ifi.hase.soprafs24.entity.Task;
import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.CommentRepository;
import ch.uzh.ifi.hase.soprafs24.repository.TaskRepository;
import ch.uzh.ifi.hase.soprafs24.repository.TeamRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
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

  @Qualifier("teamRepository") @Autowired private TeamRepository teamRepository;

  @Qualifier("userRepository") @Autowired private UserRepository userRepository;

  @Autowired private CommentService commentService;

  @BeforeEach
  @AfterEach
  public void setup() {
    commentRepository.deleteAll();
    taskRepository.deleteAll();
    teamRepository.deleteAll();
    userRepository.deleteAll();
  }

  // region Comment Service Integration

  @Test
  public void createComment_validInputs_success() {
    String justiceTeam = "productiviteam";

    // given
    Team testTeam1 = new Team();
    testTeam1.setName(justiceTeam);
    testTeam1.setDescription("We are a productive team!");
    testTeam1.setTeamUUID("team-uuid");
    teamRepository.saveAndFlush(testTeam1);

    // given a task
    Task testTask = new Task();
    testTask.setTitle("Test Task");
    testTask.setDescription("This is a task for testing");
    testTask.setStatus(TaskStatus.TODO);
    testTask.setTeam(testTeam1);
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
    testComment.setUser(testUser);

    // when
    Comment createdComment = commentService.createComment(testComment, testTask.getTaskId());

    // then
    assertNotNull(createdComment.getCreationDate());

    assertEquals(testComment.getText(), createdComment.getText());
    assertEquals(testTask.getTaskId(), createdComment.getTask().getTaskId());
    assertEquals(testUser.getUserId(), createdComment.getUser().getUserId());
  }

  @Test
  public void createComment_emptyText_throwsException() {
    // given a team
    Team testTeam = new Team();
    testTeam.setName("Test Team");
    testTeam.setDescription("This is a test team");
    testTeam.setTeamUUID("team-uuid");
    teamRepository.saveAndFlush(testTeam);

    // given a task
    Task testTask = new Task();
    testTask.setTitle("Test Task");
    testTask.setDescription("This is a task for testing");
    testTask.setStatus(TaskStatus.TODO);
    testTask.setTeam(testTeam);
    taskRepository.saveAndFlush(testTask);

    // given a user
    User testUser = new User();
    testUser.setUsername("testUser");
    testUser.setName("Test User");
    testUser.setPassword("password123");
    testUser.setToken("1");
    userRepository.saveAndFlush(testUser);

    // given a comment with empty text
    Comment testComment = new Comment();
    testComment.setText("");
    testComment.setUser(testUser);

    // when & then
    assertThrows(ResponseStatusException.class,
        () -> commentService.createComment(testComment, testTask.getTaskId()));
  }

  @Test
  public void createComment_nullText_throwsException() {
    // given a team
    Team testTeam = new Team();
    testTeam.setName("Test Team");
    testTeam.setDescription("This is a test team");
    testTeam.setTeamUUID("team-uuid");
    teamRepository.saveAndFlush(testTeam);

    // given a task
    Task testTask = new Task();
    testTask.setTitle("Test Task");
    testTask.setDescription("This is a task for testing");
    testTask.setStatus(TaskStatus.TODO);
    testTask.setTeam(testTeam);
    taskRepository.saveAndFlush(testTask);

    // given a user
    User testUser = new User();
    testUser.setUsername("testUser");
    testUser.setName("Test User");
    testUser.setPassword("password123");
    testUser.setToken("1");
    userRepository.saveAndFlush(testUser);

    // given a comment with null text
    Comment testComment = new Comment();
    testComment.setText(null);
    testComment.setUser(testUser);

    // when & then
    assertThrows(ResponseStatusException.class,
        () -> commentService.createComment(testComment, testTask.getTaskId()));
  }

  @Test
  public void createComment_invalidTaskId_throwsException() {
    // given a task id that does not exist
    Long invalidTaskId = 999L;

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
    testComment.setUser(testUser);

    // when & then
    assertThrows(ResponseStatusException.class,
        () -> commentService.createComment(testComment, invalidTaskId));
  }

  @Test
  public void createComment_invalidUserId_throwsException() {
    // given a team
    Team testTeam = new Team();
    testTeam.setName("Test Team");
    testTeam.setDescription("This is a test team");
    testTeam.setTeamUUID("team-uuid");
    teamRepository.saveAndFlush(testTeam);

    // given a task
    Task testTask = new Task();
    testTask.setTitle("Test Task");
    testTask.setDescription("This is a task for testing");
    testTask.setStatus(TaskStatus.TODO);
    testTask.setTeam(testTeam);
    taskRepository.saveAndFlush(testTask);

    // given a user id that does not exist in db
    Long invalidUserId = 999L;
    User testUser = new User();
    testUser.setUsername("testUser");

    // given a comment
    Comment testComment = new Comment();
    testComment.setText("This is a test comment");
    testComment.setUser(testUser);

    // when & then
    assertThrows(ResponseStatusException.class,
        () -> commentService.createComment(testComment, testTask.getTaskId()));
  }

  // endregion

  // region Comment Service Integration GET

  @Test
  public void getComments_validInputs_success() {
    // given a team
    Team testTeam = new Team();
    testTeam.setName("Test Team");
    testTeam.setDescription("This is a test team");
    testTeam.setTeamUUID("team-uuid");
    teamRepository.saveAndFlush(testTeam);

    // given a task
    Task testTask = new Task();
    testTask.setTitle("Test Task");
    testTask.setDescription("This is a task for testing");
    testTask.setStatus(TaskStatus.TODO);
    testTask.setTeam(testTeam);
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
    testComment.setCreationDate(LocalDateTime.now());
    commentRepository.saveAndFlush(testComment);

    // when
    List<Comment> comments = commentService.getCommentsByTaskId(testTask.getTaskId());

    // then
    assertNotNull(comments);
    assertEquals(1, comments.size());
    assertEquals(testComment.getText(), comments.get(0).getText());
  }

  @Test
  public void getComments_invalidTaskId_throwsException() {
    // given a task id that does not exist
    Long invalidTaskId = 999L;

    // when & then
    assertThrows(
        ResponseStatusException.class, () -> commentService.getCommentsByTaskId(invalidTaskId));
  }

  @Test
  public void getComments_noCommentsForTask_returnsEmptyList() {
    // given a user
    User testUser = new User();
    testUser.setUsername("testUser");
    testUser.setName("Test User");
    testUser.setPassword("password123");
    testUser.setToken("1");
    userRepository.saveAndFlush(testUser);

    // given a team
    Team testTeam = new Team();
    testTeam.setName("Test Team");
    testTeam.setDescription("This is a test team");
    testTeam.setTeamUUID("team-uuid");
    teamRepository.saveAndFlush(testTeam);

    // given a task with no comments
    Task testTask = new Task();
    testTask.setTitle("Test Task");
    testTask.setDescription("This is a task for testing");
    testTask.setStatus(TaskStatus.TODO);
    testTask.setTeam(testTeam);
    taskRepository.saveAndFlush(testTask);

    // when
    List<Comment> comments = commentService.getCommentsByTaskId(testTask.getTaskId());

    // then
    assertNotNull(comments);
    assertTrue(comments.isEmpty());
  }

  @Test
  public void getCommentsByTaskId_orderedByCreationDate_success() {
    // given a team
    Team testTeam = new Team();
    testTeam.setName("Test Team");
    testTeam.setDescription("This is a test team");
    testTeam.setTeamUUID("team-uuid");
    teamRepository.saveAndFlush(testTeam);

    // given a task
    Task testTask = new Task();
    testTask.setTitle("Test Task");
    testTask.setDescription("This is a task for testing");
    testTask.setStatus(TaskStatus.TODO);
    testTask.setTeam(testTeam);
    taskRepository.saveAndFlush(testTask);

    // given a user
    User testUser = new User();
    testUser.setUsername("testUser");
    testUser.setName("Test User");
    testUser.setPassword("password123");
    testUser.setToken("1");
    userRepository.saveAndFlush(testUser);

    // given first comment (older)
    Comment firstComment = new Comment();
    firstComment.setText("This is the first comment");
    firstComment.setUser(testUser);
    firstComment.setTask(testTask);
    firstComment.setCreationDate(LocalDateTime.now().minusHours(5));
    commentRepository.saveAndFlush(firstComment);

    // given second comment (more recent)
    Comment secondComment = new Comment();
    secondComment.setText("This is the second comment");
    secondComment.setUser(testUser);
    secondComment.setTask(testTask);
    secondComment.setCreationDate(LocalDateTime.now());
    commentRepository.saveAndFlush(secondComment);

    // when
    List<Comment> comments = commentService.getCommentsByTaskId(testTask.getTaskId());

    // then
    assertNotNull(comments);
    assertEquals(2, comments.size());
    assertEquals(secondComment.getText(), comments.get(0).getText()); // more recent first
    assertEquals(firstComment.getText(), comments.get(1).getText());
    assertTrue(comments.get(0).getCreationDate().isAfter(comments.get(1).getCreationDate()));
  }

  // endregion

  // region delete comment
  @Test
  public void deleteComment_validInputs_success() {
    // given a team
    Team testTeam = new Team();
    testTeam.setName("Test Team");
    testTeam.setDescription("This is a test team");
    testTeam.setTeamUUID("team-uuid");
    teamRepository.saveAndFlush(testTeam);

    // given a task
    Task testTask = new Task();
    testTask.setTitle("Test Task");
    testTask.setDescription("This is a task for testing");
    testTask.setStatus(TaskStatus.TODO);
    testTask.setTeam(testTeam);
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
    testComment.setCreationDate(LocalDateTime.now());
    commentRepository.saveAndFlush(testComment);

    // when
    Comment deletedComment = commentService.deleteCommentById(testComment.getCommentId());

    // then
    assertNotNull(deletedComment);
    assertEquals(testComment.getCommentId(), deletedComment.getCommentId());
    assertTrue(commentRepository.findById(testComment.getCommentId()).isEmpty());
  }

  @Test
  public void deleteComment_invalidCommentId_throwsException() {
    // given a comment id that does not exist
    Long invalidCommentId = 999L;

    // verify that the comment does not exist (we want to test if the error is thrown)
    assertTrue(commentRepository.findById(invalidCommentId).isEmpty());

    // when & then
    assertThrows(
        ResponseStatusException.class, () -> commentService.deleteCommentById(invalidCommentId));
  }
  // endregion
}
