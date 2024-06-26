package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.time.LocalDateTime;

public class SessionGetDTO {
  private String startDateTime;
  private String endDateTime;
  private Long goalMinutes;

  public String getStartDateTime() {
    return startDateTime;
  }

  public void setStartDateTime(String startDateTime) {
    this.startDateTime = startDateTime;
  }

  public String getEndDateTime() {
    return endDateTime;
  }

  public void setEndDateTime(String endDateTime) {
    this.endDateTime = endDateTime;
  }

  public Long getGoalMinutes() {
    return goalMinutes;
  }

  public void setGoalMinutes(Long goalMinutes) {
    this.goalMinutes = goalMinutes;
  }
}
