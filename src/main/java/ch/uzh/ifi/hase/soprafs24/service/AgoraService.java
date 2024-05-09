package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.agora.RtcTokenBuilder2;
import ch.uzh.ifi.hase.soprafs24.config.AgoraConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Responsible for fetching the token for the user
 */
public class AgoraService {
  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private AgoraConfig agoraConfig;
  private RtcTokenBuilder2 rtcTokenBuilder2;

  private int tokenExpirationTs = 3600;
  private int privilegeExpiredTs = 3600;

  public AgoraService(AgoraConfig agoraConfig, RtcTokenBuilder2 rtcTokenBuilder2) {
    this.agoraConfig = agoraConfig;
    this.rtcTokenBuilder2 = rtcTokenBuilder2;
  }

  /**
   * Generates a token for the user for the given channel
   * @param existingUserId id of the user that exists (pre-condition)
   * @param channelName name of the channel
   * @return RTC token for the user for this channel
   * @throws ResponseStatusException if the token cannot be generated (500 if env vars not set, 503
   *     if external service not available)
   */
  public String getToken(Long existingUserId, String channelName) {
    // check if the credentials are set (500 error if not)
    checkCredentialVariable(agoraConfig.getAppId(), "AGORA_APP_ID");
    checkCredentialVariable(agoraConfig.getAppKey(), "AGORA_APP_KEY");
    checkCredentialVariable(agoraConfig.getAppCertificate(), "AGORA_APP_CERTIFICATE");

    // generate token or throw 503 error if external service not available
    try {
      return rtcTokenBuilder2.buildTokenWithUserAccount(agoraConfig.getAppId(),
          agoraConfig.getAppCertificate(), channelName, String.valueOf(existingUserId),
          RtcTokenBuilder2.Role.ROLE_PUBLISHER, tokenExpirationTs, privilegeExpiredTs);
    } catch (Exception e) {
      String errMsg = "Error while generating Agora token: " + e.getMessage();
      log.error(errMsg);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, errMsg);
    }
  }

  private void checkCredentialVariable(String variable, String variableName) {
    if (variable == null || variable.isEmpty()) {
      String errMsg = "Need to set environment variable " + variableName + "\n";
      log.error(errMsg);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errMsg);
    }
  }
}
