package ch.uzh.ifi.hase.soprafs24.entity;
import ch.uzh.ifi.hase.soprafs24.constant.TaskStatus;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Internal Task Representation
 */
@Entity
@Table(name = "TASK")
public class Task implements Serializable {
  public static final long serialVersionUID = 1L;

  // @Id: primary key
  // @GeneratedValue: auto increment
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long taskId;

  // varchar(100), not null
  @Column(length = 100, nullable = false) private String title;

  // varchar(500), not null
  @Column(length = 500, nullable = false) private String description;

  // timestamp, not null
  @CreationTimestamp private LocalDateTime creationDate;

  // string, not null
  @Column(nullable = false) private TaskStatus status;

  // foreign key to Team
  @OneToOne @JoinColumn(name = "teamId", nullable = false) private Team teamId;

  public Long getTaskId() {
    return taskId;
  }

  public void setTaskId(Long taskId) {
    this.taskId = taskId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDateTime getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(LocalDateTime creationDate) {
    this.creationDate = creationDate;
  }

  public TaskStatus getStatus() {
    return status;
  }

  public void setStatus(TaskStatus status) {
    this.status = status;
  }

  public Team getTeamId() {
    return teamId;
  }

  public void setTeamId(Team teamId) {
    this.teamId = teamId;
  }
}
