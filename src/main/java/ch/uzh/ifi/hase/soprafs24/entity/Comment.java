package ch.uzh.ifi.hase.soprafs24.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "COMMENT")
public class Comment implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long commentId;

  @Column(length = 500, nullable = false) private String text;

  @Column(nullable = false) private LocalDateTime creationDate;

  @ManyToOne @JoinColumn(name = "taskId", nullable = false) private Task task;

  @ManyToOne @JoinColumn(name = "userId", nullable = false) private User user;

  public Long getCommentId() {
    return commentId;
  }

  public void setCommentId(Long commentId) {
    this.commentId = commentId;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public LocalDateTime getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(LocalDateTime creationDate) {
    this.creationDate = creationDate;
  }

  public Task getTask() {
    return task;
  }

  public void setTask(Task task) {
    this.task = task;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }
}
