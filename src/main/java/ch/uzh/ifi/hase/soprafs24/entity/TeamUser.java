package ch.uzh.ifi.hase.soprafs24.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Many-to-many relationship between Team and User.
 * Instances should be created using the provided constructors, which ensure the proper setup of the
 * embedded id.
 * @see <a
 *     href="https://www.baeldung.com/jpa-many-to-many#many-to-many-using-a-composite-key">https://www.baeldung.com/jpa-many-to-many#many-to-many-using-a-composite-key</a>
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

  @ManyToOne @MapsId("teamId") @JoinColumn(name = "teamId") private Team team;

  @ManyToOne @MapsId("userId") @JoinColumn(name = "userId") private User user;

  @CreationTimestamp private LocalDateTime joinTimestamp;

  /**
   * Default constructor for TeamUser, but uses postLoad to ensure that the embedded id is set.
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

  @PostLoad
  private void postLoad() {
    if (this.teamUserId == null) {
      this.teamUserId = new TeamUserId(team.getTeamId(), user.getUserId());
    }
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

  public LocalDateTime getJoinTimestamp() {
    return joinTimestamp;
  }

  public void setJoinTimestamp(LocalDateTime joinTimestamp) {
    this.joinTimestamp = joinTimestamp;
  }
}
