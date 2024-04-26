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
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TaskRepositoryIntegrationTest {
  @Autowired private TestEntityManager entityManager;

  @Autowired TaskRepository taskRepository;

  @Autowired TeamRepository teamRepository;

  private Team team;

  @BeforeEach
  public void setup() {
    // given
    team = new Team();
    team.setName("Team Name");
    team.setDescription("Team Description");
    team.setTeamUUID("team-uuid");

    entityManager.persist(team);
    entityManager.flush();
  }

  @Test
  public void findByTeam_empty() {
    // when
    List<Task> found = taskRepository.findByTeam(team);

    // then
    assertEquals(found.size(), 0);
  }

  @Test
  public void whenSaved_thenFindAll() {
    // given
    Task task = new Task();
    task.setTitle("Task Title");
    task.setDescription("Task Description");
    task.setStatus(TaskStatus.TODO);
    task.setTeam(team);

    entityManager.persist(task);
    entityManager.flush();

    // when
    List<Task> found = taskRepository.findAll();

    // then
    assertNotNull(found.get(0).getTaskId());
    assertEquals(found.get(0).getTitle(), task.getTitle());
    assertEquals(found.get(0).getDescription(), task.getDescription());
    assertEquals(found.get(0).getStatus(), task.getStatus());
    assertEquals(found.get(0).getTeam(), task.getTeam());
    assertNotNull(found.get(0).getCreationDate());
  }

  @Test
  public void findByTeam_success() {
    // given
    Task task = new Task();
    task.setTitle("Task Title");
    task.setDescription("Task Description");
    task.setStatus(TaskStatus.TODO);
    task.setTeam(team);

    entityManager.persist(task);
    entityManager.flush();

    // when
    List<Task> found = taskRepository.findByTeam(team);

    // then
    assertNotNull(found.get(0).getTaskId());
    assertEquals(found.get(0).getTitle(), task.getTitle());
    assertEquals(found.get(0).getDescription(), task.getDescription());
    assertEquals(found.get(0).getStatus(), task.getStatus());
    assertEquals(found.get(0).getTeam(), task.getTeam());
    assertNotNull(found.get(0).getCreationDate());
  }

  /* test to get a list of tasks where status is not DELETED */
  @Test
  public void findByTeamAndStatusNot_success() {
    // given
    Task task = new Task();
    task.setTitle("Task Title");
    task.setDescription("Task Description");
    task.setStatus(TaskStatus.TODO);
    task.setTeam(team);
    entityManager.persist(task);
    entityManager.flush();

    // given task2 with status DELETED
    Task task2 = new Task();
    task2.setTitle("Task Title 2");
    task2.setDescription("Task Description 2");
    task2.setStatus(TaskStatus.DELETED);
    task2.setTeam(team);
    entityManager.persist(task2);
    entityManager.flush();

    // when
    List<Task> found = taskRepository.findByTeamAndStatusNot(team, TaskStatus.DELETED);

    // then
    assertEquals(1, found.size());
    assertNotNull(found.get(0).getTaskId());
    assertEquals(found.get(0).getTitle(), task.getTitle());
    assertEquals(found.get(0).getDescription(), task.getDescription());
    assertEquals(found.get(0).getStatus(), task.getStatus());
    assertEquals(found.get(0).getTeam(), task.getTeam());
    assertNotNull(found.get(0).getCreationDate());
  }
}
