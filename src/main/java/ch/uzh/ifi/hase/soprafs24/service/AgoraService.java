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

  public AgoraService(AgoraConfig agoraConfig) {
    this.agoraConfig = agoraConfig;
  }

  public String getToken(int userId, String channelName) {
    if (agoraConfig.getAppId() == null || agoraConfig.getAppId().isEmpty()
        || agoraConfig.getAppCertificate() == null || agoraConfig.getAppCertificate().isEmpty()) {
      String errMsg = "Need to set environment variable AGORA_APP_ID and AGORA_APP_CERTIFICATE\n";
      log.error(errMsg);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errMsg);
    }

    RtcTokenBuilder2 rtcTokenBuilder2 = new RtcTokenBuilder2();
    return rtcTokenBuilder2.buildTokenWithUserAccount(agoraConfig.getAppId(),
        agoraConfig.getAppCertificate(), channelName, String.valueOf(userId),
        RtcTokenBuilder2.Role.ROLE_PUBLISHER, 3600, 3600);
  }
}
