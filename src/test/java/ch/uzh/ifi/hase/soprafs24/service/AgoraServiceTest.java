package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs24.agora.RtcTokenBuilder2;
import ch.uzh.ifi.hase.soprafs24.agora.RtmTokenBuilder2;
import ch.uzh.ifi.hase.soprafs24.config.AgoraCredentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AgoraServiceTest {
  @Mock private RtcTokenBuilder2 rtcTokenBuilder2;
  @Mock private RtmTokenBuilder2 rtmTokenBuilder2;
  @Mock private AgoraCredentials agoraCredentials;

  @InjectMocks AgoraService agoraService;

  private Long userId;
  private String channelName;

  @BeforeEach
  public void setup() {
    // mocks
    MockitoAnnotations.openMocks(this);

    // given
    userId = 1L;
    channelName = "channelName";

    // mock agoraConfig return values
    Mockito.when(agoraCredentials.getAppId()).thenReturn("1234");
    Mockito.when(agoraCredentials.getAppCertificate()).thenReturn("appCertificate");
  }

  // region rtc token
  @Test
  public void testGetRtcToken_success() {
    // when
    Mockito
        .when(rtcTokenBuilder2.buildTokenWithUserAccount(Mockito.same("1234"),
            Mockito.same("appCertificate"), Mockito.same(channelName), Mockito.any(), Mockito.any(),
            Mockito.anyInt(), Mockito.anyInt()))
        .thenReturn("testToken");

    // then
    String token = agoraService.getRtcToken(userId, channelName);

    assertEquals("testToken", token);
  }

  @Test
  public void testGetRtcToken_rtcTokenBuilderException() {
    // when
    Mockito
        .when(rtcTokenBuilder2.buildTokenWithUserAccount(Mockito.same("1234"),
            Mockito.same("appCertificate"), Mockito.same(channelName), Mockito.any(), Mockito.any(),
            Mockito.anyInt(), Mockito.anyInt()))
        .thenThrow(new RuntimeException("Test exception"));

    // then check if the correct exception is thrown and if https status return is correct
    Exception exception =
        assertThrows(RuntimeException.class, () -> agoraService.getRtcToken(userId, channelName));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ((ResponseStatusException) exception).getStatus());
  }

  /* just to cover the helper method and to assure that in the future this check is kept */
  @Test
  public void testGetRtcToken_missingAppId() {
    // given
    Mockito.when(agoraCredentials.getAppId()).thenReturn(null);

    // then check if the correct exception is thrown and if https status return is correct
    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class, () -> agoraService.getRtcToken(userId, channelName));
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
  }

  /* just to cover the helper method and to assure that in the future this check is kept */
  @Test
  public void testGetRtcToken_missingAppId_emptyString() {
    // given
    Mockito.when(agoraCredentials.getAppId()).thenReturn("");

    // then check if the correct exception is thrown and if https status return is correct
    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class, () -> agoraService.getRtcToken(userId, channelName));
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
  }

  /* check if channelName is bad format */
  @Test
  public void testGetRtcToken_badChannelName() {
    // given
    channelName = " ";

    // then check if the correct exception is thrown and if https status return is correct
    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class, () -> agoraService.getRtcToken(userId, channelName));
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
  }
  // endregion

  // region rtm token
  @Test
  public void testGetRtmToken_success() {
    // when
    Mockito
        .when(rtmTokenBuilder2.buildToken(
            Mockito.same("1234"), Mockito.same("appCertificate"), Mockito.any(), Mockito.anyInt()))
        .thenReturn("testToken");

    // then
    String token = agoraService.getRtmToken(userId);

    assertEquals("testToken", token);
  }

  @Test
  public void testGetRtmToken_rtmTokenBuilderException() {
    // when
    Mockito
        .when(rtmTokenBuilder2.buildToken(
            Mockito.same("1234"), Mockito.same("appCertificate"), Mockito.any(), Mockito.anyInt()))
        .thenThrow(new RuntimeException("Test exception"));

    // then check if the correct exception is thrown and if https status return is correct
    Exception exception =
        assertThrows(RuntimeException.class, () -> agoraService.getRtmToken(userId));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ((ResponseStatusException) exception).getStatus());
  }

  /* to cover the helper method and to assure that in the future this check is kept */
  @Test
  public void testGetRtmToken_missingAppId() {
    // given
    Mockito.when(agoraCredentials.getAppId()).thenReturn(null);

    // then check if the correct exception is thrown and if https status return is correct
    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> agoraService.getRtmToken(userId));
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
  }

  /* to cover the helper method and to assure that in the future this check is kept */
  @Test
  public void testGetRtmToken_missingAppId_emptyString() {
    // given
    Mockito.when(agoraCredentials.getAppId()).thenReturn("");

    // then check if the correct exception is thrown and if https status return is correct
    ResponseStatusException exception =
        assertThrows(ResponseStatusException.class, () -> agoraService.getRtmToken(userId));
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
  }
  // endregion
}
