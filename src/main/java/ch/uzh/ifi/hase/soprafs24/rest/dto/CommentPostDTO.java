package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class CommentPostDTO {
  private String text;

  private Long userId;

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }
}
