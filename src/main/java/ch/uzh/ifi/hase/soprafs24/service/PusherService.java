package ch.uzh.ifi.hase.soprafs24.service;

import com.pusher.rest.Pusher;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PusherService {
  private final Logger log = LoggerFactory.getLogger(SessionService.class);

  private final Pusher pusher;

  public PusherService(Pusher pusher) {
    // add the pusher object of the config.PusherConfig via dependency injection (inversion of
    // control)
    this.pusher = pusher;
  }

  private void triggerEvent(String channel, String event, Object data) {
    try {
      pusher.trigger(channel, event, data);
    } catch (Exception e) {
      log.error("Error while sending pusher event: {}", e.getMessage());
    }
  }

  public void startSession(String teamId) {
    triggerEvent("team-" + teamId, "session-update", Collections.singletonMap("status", "on"));
  }

  public void stopSession(String teamId) {
    triggerEvent("team-" + teamId, "session-update", Collections.singletonMap("status", "off"));
  }

  /* pusher service when creating or modifying a task */
  public void taskModification(String teamId) {
    triggerEvent("team-" + teamId, "task-update", Collections.singletonMap("tasks", "updated"));
  }

  public void updateTeam(String teamId, String userId) {
    triggerEvent("team-" + teamId, "team-update", Collections.singletonMap("userId", userId));
  }
}
