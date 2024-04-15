package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class AuthGetDTO {
  private String token;
  private Long userId;

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }
}
