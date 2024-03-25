package ch.uzh.ifi.hase.soprafs24.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.uzh.ifi.hase.soprafs24.constant.TaskStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Comment;
import ch.uzh.ifi.hase.soprafs24.entity.Task;
import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
public class CommentRepositoryIntegrationTest {
  @Autowired private TestEntityManager entityManager;

  @Autowired private CommentRepository commentRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private TaskRepository taskRepository;

  @Autowired private TeamRepository teamRepository;

  private User user;
  private Task task;
  private Team team;

  @BeforeEach
  public void setup() {
    // given
    user = new User();
    user.setName("Firstname Lastname");
    user.setUsername("firstname@lastname");
    user.setPassword("1234");
    user.setToken("1");

    team = new Team();
    team.setName("Team Name");
    team.setDescription("Team Description");

    task = new Task();
    task.setTitle("Task Title");
    task.setDescription("Task Description");
    task.setStatus(TaskStatus.TODO);
    task.setTeam(team);

    entityManager.persist(user);
    entityManager.persist(team);
    entityManager.persist(task);
    entityManager.flush();
  }

  @Test
  public void whenSave_thenFindAllReturnsSavedComment() {
    // given
    Comment comment = new Comment();
    comment.setText("This is a comment.");
    comment.setTask(task);
    comment.setUser(user);

    // when
    commentRepository.save(comment);

    // then
    List<Comment> comments = commentRepository.findAll();
    assertEquals(1, comments.size());
    assertEquals(comments.get(0).getText(), comment.getText());
    assertNotNull(comments.get(0).getCreationDate());
  }

  @Test
  public void findByComment_success() {
    // given
    Comment comment = new Comment();
    comment.setText("This is a comment.");
    comment.setTask(task);
    comment.setUser(user);

    entityManager.persist(comment);
    entityManager.flush();

    // when
    Comment found = commentRepository.findByCommentId(comment.getCommentId());

    // then
    assertNotNull(found.getCommentId());
    assertEquals(found.getText(), comment.getText());
    assertEquals(found.getTask(), comment.getTask());
    assertEquals(found.getUser(), comment.getUser());
    assertNotNull(found.getCreationDate());
  }

  @Test
  public void findByTask_success() {
    // given
    Comment comment = new Comment();
    comment.setText("This is a comment.");
    comment.setTask(task);
    comment.setUser(user);

    entityManager.persist(comment);
    entityManager.flush();

    // when
    List<Comment> found = commentRepository.findByTask(task);

    // then
    List<Comment> comments = commentRepository.findAll();
    assertEquals(1, comments.size());
    assertEquals(comments.get(0).getText(), comment.getText());
    assertEquals(comments.get(0).getTask(), task);
    assertNotNull(comments.get(0).getCreationDate());
  }

  @Test
  public void findByUser_success() {
    // given
    Comment comment = new Comment();
    comment.setText("This is a comment.");
    comment.setTask(task);
    comment.setUser(user);

    entityManager.persist(comment);
    entityManager.flush();

    // when
    List<Comment> found = commentRepository.findByUser(user);

    // then
    List<Comment> comments = commentRepository.findAll();
    assertEquals(1, comments.size());
    assertEquals(comments.get(0).getText(), comment.getText());
    assertEquals(comments.get(0).getTask(), task);
  }
}
