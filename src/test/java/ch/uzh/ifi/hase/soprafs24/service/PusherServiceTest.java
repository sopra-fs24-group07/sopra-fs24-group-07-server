package ch.uzh.ifi.hase.soprafs24.service;

import com.pusher.rest.Pusher;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class PusherServiceTest {
  @Mock private Pusher pusher;

  private PusherService pusherService;

  private String channel;
  private String event;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
    pusherService = new PusherService(pusher);

    // given
    channel = "team-teamId";
    event = "session-update";
  }

  @Test
  public void testStartSession_success() {
    Mockito.doReturn(new Mockito())
        .when(pusher)
        .trigger(Mockito.anyString(), Mockito.anyString(), Mockito.any());

    pusherService.startSession("teamId");

    Mockito.verify(pusher, Mockito.times(1))
        .trigger(Mockito.eq(channel), Mockito.eq(event),
            Mockito.eq(Collections.singletonMap("status", "on")));
  }

  @Test
  public void testStartSession_pusherException() {
    Mockito.doThrow(new RuntimeException("Test exception"))
        .when(pusher)
        .trigger(Mockito.anyString(), Mockito.anyString(), Mockito.any());

    pusherService.startSession("teamId");

    Mockito.verify(pusher, Mockito.times(1))
        .trigger(Mockito.eq(channel), Mockito.eq(event),
            Mockito.eq(Collections.singletonMap("status", "on")));
  }

  @Test
  public void testEndSession_success() {
    Mockito.doReturn(new Mockito())
        .when(pusher)
        .trigger(Mockito.anyString(), Mockito.anyString(), Mockito.any());

    pusherService.stopSession("teamId");

    Mockito.verify(pusher, Mockito.times(1))
        .trigger(Mockito.eq(channel), Mockito.eq(event),
            Mockito.eq(Collections.singletonMap("status", "off")));
  }

  @Test
  public void testEndSession_pusherException() {
    Mockito.doThrow(new RuntimeException("Test exception"))
        .when(pusher)
        .trigger(Mockito.anyString(), Mockito.anyString(), Mockito.any());

    pusherService.stopSession("teamId");

    Mockito.verify(pusher, Mockito.times(1))
        .trigger(Mockito.eq(channel), Mockito.eq(event),
            Mockito.eq(Collections.singletonMap("status", "off")));
  }

  @Test
  public void testTaskModification_success() {
    Mockito.doReturn(new Mockito())
        .when(pusher)
        .trigger(Mockito.anyString(), Mockito.anyString(), Mockito.any());

    pusherService.taskModification("teamId");

    Mockito.verify(pusher, Mockito.times(1))
        .trigger(Mockito.eq(channel), Mockito.eq("task-update"),
            Mockito.eq(Collections.singletonMap("tasks", "updated")));
  }

  @Test
  public void testTaskModification_pusherException() {
    Mockito.doThrow(new RuntimeException("Test exception"))
        .when(pusher)
        .trigger(Mockito.anyString(), Mockito.anyString(), Mockito.any());

    pusherService.taskModification("teamId");

    Mockito.verify(pusher, Mockito.times(1))
        .trigger(Mockito.eq(channel), Mockito.eq("task-update"),
            Mockito.eq(Collections.singletonMap("tasks", "updated")));
  }

  @Test
  public void testUpdateTeam_success() {
    Mockito.doReturn(new Mockito())
        .when(pusher)
        .trigger(Mockito.anyString(), Mockito.anyString(), Mockito.any());

    pusherService.updateTeam("teamId");

    Mockito.verify(pusher, Mockito.times(1))
        .trigger(Mockito.eq(channel), Mockito.eq("team-update"),
            Mockito.eq(Collections.singletonMap("team", "updated")));
  }

  @Test
  public void testUpdateTeam_pusherException() {
    Mockito.doThrow(new RuntimeException("Test exception"))
        .when(pusher)
        .trigger(Mockito.anyString(), Mockito.anyString(), Mockito.any());

    pusherService.updateTeam("teamId");

    Mockito.verify(pusher, Mockito.times(1))
        .trigger(Mockito.eq(channel), Mockito.eq("team-update"),
            Mockito.eq(Collections.singletonMap("team", "updated")));
  }

  @Test
  public void testUpdateComments_success() {
    Mockito.doReturn(new Mockito())
        .when(pusher)
        .trigger(Mockito.anyString(), Mockito.anyString(), Mockito.any());

    pusherService.updateComments("teamId");

    Mockito.verify(pusher, Mockito.times(1))
        .trigger(Mockito.eq(channel), Mockito.eq("comment-update"),
            Mockito.eq(Collections.singletonMap("comments", "updated")));
  }

  @Test
  public void testUpdateComments_pusherException() {
    Mockito.doThrow(new RuntimeException("Test exception"))
        .when(pusher)
        .trigger(Mockito.anyString(), Mockito.anyString(), Mockito.any());

    pusherService.updateComments("teamId");

    Mockito.verify(pusher, Mockito.times(1))
        .trigger(Mockito.eq(channel), Mockito.eq("comment-update"),
            Mockito.eq(Collections.singletonMap("comments", "updated")));
  }
}
