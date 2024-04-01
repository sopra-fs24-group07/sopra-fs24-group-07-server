package ch.uzh.ifi.hase.soprafs24.entity;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Embeddable;
import javax.persistence.PostLoad;

/**
 * Represents the embedded id for TeamUser.
 * Instances should be created using the provided constructor,
 * which ensures the proper setup of the team id and user id.
 */
@Embeddable
public class TeamUserId implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long teamId;

  private Long userId;

  /**
   *  Default constructor
   */
  public TeamUserId() {}

  /**
   * Constructor that sets up a TeamUserId with the provided team id and user id.
   *
   * @param teamId The id of the team.
   * @param userId The id of the user.
   */
  public TeamUserId(Long teamId, Long userId) {
    this.teamId = teamId;
    this.userId = userId;
  }

  @PostLoad
  private void postLoad() {
    if (this.teamId == null || this.userId == null) {
      throw new IllegalStateException(
          "TeamUserId is not properly initialized. teamId or userId is null and needs to be set.");
    }
  }

  public Long getTeamId() {
    return teamId;
  }

  public void setTeamId(Long teamId) {
    this.teamId = teamId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    TeamUserId that = (TeamUserId) o;
    return Objects.equals(teamId, that.teamId) && Objects.equals(userId, that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(teamId, userId);
  }
}
