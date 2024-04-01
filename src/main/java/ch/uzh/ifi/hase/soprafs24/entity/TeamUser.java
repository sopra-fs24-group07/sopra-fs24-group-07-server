package ch.uzh.ifi.hase.soprafs24.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Many-to-many relationship between Team and User.
 * Instances should be created using the provided constructors, which ensure the proper setup of the
 * embedded id.
 */
@Entity
@Table(name = "TEAM_USER")
public class TeamUser implements Serializable {
  private static final long serialVersionUID = 1L;

  // JPA wouldn't know that these two fields together form the primary key for the TeamUser entity.
  // By using an embedded id, we're making it clear to JPA that these two fields together form the
  // primary key.
  // entity that uses this composite key (TeamUser), annotated with @EmbeddedId
  @EmbeddedId private TeamUserId teamUserId;

  @ManyToOne @MapsId("teamId") Team team;

  @ManyToOne @MapsId("userId") User user;

  @CreationTimestamp @Column(nullable = false) private LocalDateTime creationTimestamp;

  /**
   * Default constructor
   */
  public TeamUser() {}

  /**
   * Constructor that sets up a TeamUser with the provided Team and User.
   * Also sets up the embedded id based on the ids of the provided entities.
   *
   * @param team The team to link with a user.
   * @param user The user to link with a team.
   */
  public TeamUser(Team team, User user) {
    this.team = team;
    this.user = user;
    this.teamUserId = new TeamUserId(team.getTeamId(), user.getUserId());
  }

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
