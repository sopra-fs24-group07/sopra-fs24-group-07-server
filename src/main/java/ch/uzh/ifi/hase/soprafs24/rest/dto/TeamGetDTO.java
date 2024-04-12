package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class TeamGetDTO {
  private Long teamId;

  private String teamUUID;

  private String name;

  private String description;

  public Long getTeamId() {
    return teamId;
  }

  public void setTeamId(Long teamId) {
    this.teamId = teamId;
  }

  public String getTeamUUID() {
    return teamUUID;
  }

  public void setTeamUUID(String teamUUID) {
    this.teamUUID = teamUUID;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
