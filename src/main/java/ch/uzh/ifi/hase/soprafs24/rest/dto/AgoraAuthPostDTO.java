package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class AgoraAuthPostDTO {
  private Long userId;
  private Long teamId;
  private String channelName;

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getTeamId() {
    return teamId;
  }

  public void setTeamId(Long teamId) {
    this.teamId = teamId;
  }

  public String getChannelName() {
    return channelName;
  }

  public void setChannelName(String channelName) {
    this.channelName = channelName;
  }
}
