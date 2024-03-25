package ch.uzh.ifi.hase.soprafs24.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "TEAM_USER")
public class TeamUser implements Serializable {
  // JPA wouldn't know that these two fields together form the primary key for the TeamUser entity.
  // By using an embedded id, we're making it clear to JPA that these two fields together form the
  // primary key.
  // entity that uses this composite key (TeamUser), annotated with @EmbeddedId
  @EmbeddedId private TeamUserId teamUserId;

  @ManyToOne @MapsId("teamId") Team team;

  @ManyToOne @MapsId("userId") User user;

  @CreationTimestamp @Column(nullable = false) private LocalDateTime creationTimestamp;

  public TeamUserId getTeamUserId() {
    return teamUserId;
  }

  public void setTeamUserId(TeamUserId teamUserId) {
    this.teamUserId = teamUserId;
  }

  public Team getTeam() {
    return team;
  }

  public void setTeam(Team team) {
    this.team = team;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public LocalDateTime getCreationTimestamp() {
    return creationTimestamp;
  }

  public void setCreationTimestamp(LocalDateTime creationTimestamp) {
    this.creationTimestamp = creationTimestamp;
  }
}
