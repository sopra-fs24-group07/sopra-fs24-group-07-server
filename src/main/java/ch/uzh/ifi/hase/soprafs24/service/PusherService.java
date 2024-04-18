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

  public void triggerEvent(String channel, String event, Object data) {
    try {
      pusher.trigger(channel, event, data);
    } catch (Exception e) {
      log.error("Error while sending pusher event: {}", e.getMessage());
    }
  }
  public PusherService() {
    pusher = new Pusher("1787826", "98eb073ecf324dc1bf65", "8f7fed92f02890d41525");
    pusher.setCluster("eu");
    pusher.setEncrypted(true);
  }

  public void startSession(String teamId) {
    triggerEvent("team-" + teamId, "session-update", Collections.singletonMap("status", "on"));
  }

  public void stopSession(String teamId) {
    triggerEvent("team-" + teamId, "session-update", Collections.singletonMap("status", "off"));
  }

  /* pusher service when creating or modifying a task */
  public void taskModification(String teamId) {
    try {
      throw new UnsupportedOperationException("Not implemented yet");
      // todo
    } catch (Exception e) {
      log.error("Error while sending pusher event: {}", e.getMessage());
    }
  }
}
