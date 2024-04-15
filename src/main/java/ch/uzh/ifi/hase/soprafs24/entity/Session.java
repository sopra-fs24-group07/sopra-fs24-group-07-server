package ch.uzh.ifi.hase.soprafs24.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Internal Session Representation
 */
@Entity
@Table(name = "SESSION")
public class Session implements Serializable {
  public static final long serialVersionUID = 1L;

  // @Id: primary key
  // @GeneratedValue: auto increment
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long sessionId;

  // foreign key to team
  @ManyToOne @JoinColumn(name = "teamId", nullable = false) private Team team;

  @CreationTimestamp private LocalDateTime startDateTime;

  @Column(nullable = true) private LocalDateTime endDateTime;

  public Long getSessionId() {
    return sessionId;
  }

  public void setSessionId(Long sessionId) {
    this.sessionId = sessionId;
  }

  public Team getTeam() {
    return team;
  }

  public void setTeam(Team team) {
    this.team = team;
  }

  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  public void setStartDateTime(LocalDateTime startDateTime) {
    this.startDateTime = startDateTime;
  }

  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  public void setEndDateTime(LocalDateTime endDateTime) {
    this.endDateTime = endDateTime;
  }
}
