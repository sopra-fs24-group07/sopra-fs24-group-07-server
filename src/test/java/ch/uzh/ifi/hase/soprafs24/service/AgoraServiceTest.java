package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs24.agora.RtcTokenBuilder2;
import ch.uzh.ifi.hase.soprafs24.config.AgoraConfig;
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
  @Mock private AgoraConfig agoraConfig;

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
    Mockito.when(agoraConfig.getAppId()).thenReturn("1234");
    Mockito.when(agoraConfig.getAppKey()).thenReturn("appKey");
    Mockito.when(agoraConfig.getAppCertificate()).thenReturn("appCertificate");
  }

  @Test
  public void testGetToken_success() {
    // when
    Mockito
        .when(rtcTokenBuilder2.buildTokenWithUserAccount(Mockito.same("1234"),
            Mockito.same("appCertificate"), Mockito.same(channelName), Mockito.any(), Mockito.any(),
            Mockito.anyInt(), Mockito.anyInt()))
        .thenReturn("testToken");

    // then
    String token = agoraService.getToken(userId, channelName);

    assertEquals("testToken", token);
  }

  @Test
  public void testGetToken_rtcTokenBuilderException() {
    // when
    Mockito
        .when(rtcTokenBuilder2.buildTokenWithUserAccount(Mockito.same("1234"),
            Mockito.same("appCertificate"), Mockito.same(channelName), Mockito.any(), Mockito.any(),
            Mockito.anyInt(), Mockito.anyInt()))
        .thenThrow(new RuntimeException("Test exception"));

    // then check if the correct exception is thrown and if https status return is correct
    Exception exception =
        assertThrows(RuntimeException.class, () -> agoraService.getToken(userId, channelName));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ((ResponseStatusException) exception).getStatus());
  }

  /* just to cover the helper method and to assure that in the future this check is kept */
  @Test
  public void testGetToken_missingAppId() {
    // given
    Mockito.when(agoraConfig.getAppId()).thenReturn(null);

    // then check if the correct exception is thrown and if https status return is correct
    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class, () -> agoraService.getToken(userId, channelName));
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
  }
}
