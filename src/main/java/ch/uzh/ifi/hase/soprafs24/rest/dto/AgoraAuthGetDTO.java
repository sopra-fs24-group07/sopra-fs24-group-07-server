package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class AgoraAuthGetDTO {
  private String rtcToken;
  private String rtmToken;

  public String getRtcToken() {
    return rtcToken;
  }

  public void setRtcToken(String rtcToken) {
    this.rtcToken = rtcToken;
  }

  public String getRtmToken() {
    return rtmToken;
  }

  public void setRtmToken(String rtmToken) {
    this.rtmToken = rtmToken;
  }
}
