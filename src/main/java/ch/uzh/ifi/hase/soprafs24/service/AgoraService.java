package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.agora.RtcTokenBuilder2;
import ch.uzh.ifi.hase.soprafs24.config.AgoraCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Responsible for fetching the token for the user
 */
@Service
public class AgoraService {
  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private AgoraCredentials agoraCredentials;
  private RtcTokenBuilder2 rtcTokenBuilder2;

  private int tokenExpirationTs = 3600;
  private int privilegeExpiredTs = 3600;

  public AgoraService(AgoraCredentials agoraCredentials, RtcTokenBuilder2 rtcTokenBuilder2) {
    this.agoraCredentials = agoraCredentials;
    this.rtcTokenBuilder2 = rtcTokenBuilder2;
  }

  /**
   * Generates a token for the user for the given channel
   * @param existingUserId id of the user that exists (pre-condition)
   * @param channelName name of the channel
   * @return RTC token for the user for this channel
   * @throws ResponseStatusException if the token cannot be generated (500 if env vars not set, 503
   *     if external service not available), 400 if channel name is empty
   */
  public String getToken(Long existingUserId, String channelName) {
    // check if the channel name is not emtpy
    ServiceHelpers.checkValidString(channelName, "channelName");

    // check if the credentials are set (500 error if not)
    checkCredentialVariable(agoraCredentials.getAppId(), "AGORA_APP_ID");
    checkCredentialVariable(agoraCredentials.getAppCertificate(), "AGORA_APP_CERTIFICATE");

    // generate token or throw 503 error if external service not available
    try {
      log.info("Generating Agora token for user with id {} and channelName {}", existingUserId,
          channelName);

      return rtcTokenBuilder2.buildTokenWithUserAccount(agoraCredentials.getAppId(),
          agoraCredentials.getAppCertificate(), channelName, String.valueOf(existingUserId),
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
