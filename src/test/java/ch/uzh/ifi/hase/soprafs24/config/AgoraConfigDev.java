package ch.uzh.ifi.hase.soprafs24.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class AgoraConfigDev {
  private String appId;
  private String appKey;
  private String appCertificate;

  public String getAppId() {
    return appId;
  }

  public String getAppKey() {
    return appKey;
  }

  public String getAppCertificate() {
    return appCertificate;
  }
}
