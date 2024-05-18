package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class TeamInvitationPostDTO {
  private String teamUUID;

  private String receiverEmail;

  public String getTeamUUID() {
    return teamUUID;
  }

  public void setTeamUUID(String teamUUID) {
    this.teamUUID = teamUUID;
  }

  public String getReceiverEmail() {
    return receiverEmail;
  }

  public void setReceiverEmail(String receiverEmail) {
    this.receiverEmail = receiverEmail;
  }
}
