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
  }
}
