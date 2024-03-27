package ch.uzh.ifi.hase.soprafs24.entity;

import java.io.Serializable;
import javax.persistence.Embeddable;

/**
 * Represents the embedded id for TeamUser.
 * Instances should be created using the provided constructor,
 * which ensures the proper setup of the team id and user id.
 */
@Embeddable
public class TeamUserId implements Serializable {
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
}
