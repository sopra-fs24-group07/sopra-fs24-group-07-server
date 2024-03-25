package ch.uzh.ifi.hase.soprafs24.entity;

import java.io.Serializable;
import javax.persistence.Embeddable;

// @Embeddable to show that it's not a full-fledged entity but will be used by other entities
@Embeddable
public class TeamUserId implements Serializable {
  private Long teamId;

  private Long userId;

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
