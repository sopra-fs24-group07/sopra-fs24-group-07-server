package ch.uzh.ifi.hase.soprafs24.agora;

import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see <a
 *     href="https://github.com/AgoraIO/Tools/tree/master/DynamicKey/AgoraDynamicKey/java">...</a>
 */
public class RtcTokenBuilder2 {
  private final Logger log = LoggerFactory.getLogger(UserService.class);

  public enum Role {
    ROLE_PUBLISHER(1),
    ROLE_SUBSCRIBER(2),
    ;

    public int initValue;

    Role(int initValue) {
      this.initValue = initValue;
    }
  }

  // /**
  //  * Build the RTC token with uid.
  //  *
  //  * @param appId:            The App ID issued to you by Agora. Apply for a new App ID from
  //  *                          Agora Dashboard if it is missing from your kit. See Get an App ID.
  //  * @param appCertificate:   Certificate of the application that you registered in
  //  *                          the Agora Dashboard. See Get an App Certificate.
  //  * @param channelName:      Unique channel name for the AgoraRTC session in the string format
  //  * @param uid:              User ID. A 32-bit unsigned integer with a value ranging from 1 to
  //  *     (2^32-1).
  //  *                          optionalUid must be unique.
  //  * @param role:             ROLE_PUBLISHER: A broadcaster/host in a live-broadcast profile.
  //  *                          ROLE_SUBSCRIBER: An audience(default) in a live-broadcast profile.
  //  * @param token_expire:     represented by the number of seconds elapsed since now. If, for
  //  *     example,
  //  *                          you want to access the Agora Service within 10 minutes after the
  //  token
  //  * is generated, set token_expire as 600(seconds).
  //  * @param privilege_expire: represented by the number of seconds elapsed since now. If, for
  //  *     example,
  //  *                          you want to enable your privilege for 10 minutes, set
  //  privilege_expire
  //  * as 600(seconds).
  //  * @return The RTC token.
  //  */
  // public String buildTokenWithUid(String appId, String appCertificate, String channelName, int
  // uid,
  //     Role role, int token_expire, int privilege_expire) {
  //   return buildTokenWithUserAccount(appId, appCertificate, channelName,
  //       AccessToken2.getUidStr(uid), role, token_expire, privilege_expire);
  // }

  /**
   * Build the RTC token with account.
   *
   * @param appId:            The App ID issued to you by Agora. Apply for a new App ID from
   *                          Agora Dashboard if it is missing from your kit. See Get an App ID.
   * @param appCertificate:   Certificate of the application that you registered in
   *                          the Agora Dashboard. See Get an App Certificate.
   * @param channelName:      Unique channel name for the AgoraRTC session in the string format
   * @param account:          The user's account, max length is 255 Bytes.
   * @param role:             ROLE_PUBLISHER: A broadcaster/host in a live-broadcast profile.
   *                          ROLE_SUBSCRIBER: An audience(default) in a live-broadcast profile.
   * @param token_expire:     represented by the number of seconds elapsed since now. If, for
   *     example,
   *                          you want to access the Agora Service within 10 minutes after the token
   * is generated, set token_expire as 600(seconds).
   * @param privilege_expire: represented by the number of seconds elapsed since now. If, for
   *     example,
   *                          you want to enable your privilege for 10 minutes, set privilege_expire
   * as 600(seconds).
   * @return The RTC token.
   */
  public String buildTokenWithUserAccount(String appId, String appCertificate, String channelName,
      String account, Role role, int token_expire, int privilege_expire) {
    AccessToken2 accessToken = new AccessToken2(appId, appCertificate, token_expire);
    AccessToken2.Service serviceRtc = new AccessToken2.ServiceRtc(channelName, account);

    serviceRtc.addPrivilegeRtc(AccessToken2.PrivilegeRtc.PRIVILEGE_JOIN_CHANNEL, privilege_expire);
    if (role == Role.ROLE_PUBLISHER) {
      serviceRtc.addPrivilegeRtc(
          AccessToken2.PrivilegeRtc.PRIVILEGE_PUBLISH_AUDIO_STREAM, privilege_expire);
      serviceRtc.addPrivilegeRtc(
          AccessToken2.PrivilegeRtc.PRIVILEGE_PUBLISH_VIDEO_STREAM, privilege_expire);
      serviceRtc.addPrivilegeRtc(
          AccessToken2.PrivilegeRtc.PRIVILEGE_PUBLISH_DATA_STREAM, privilege_expire);
    }
    accessToken.addService(serviceRtc);

    try {
      return accessToken.build();
    } catch (Exception e) {
      // e.printStackTrace();
      log.error("Error while generating Agora RTC token: " + e.getMessage());
      return "";
    }
  }
}
