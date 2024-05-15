package ch.uzh.ifi.hase.soprafs24.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

public class AgoraCredentials {
  private String appId;
  private String appCertificate;

  public AgoraCredentials(String appId, String appCertificate) {
    this.appId = appId;
    this.appCertificate = appCertificate;
  }

  public String getAppId() {
    return appId;
  }

  public String getAppCertificate() {
    return appCertificate;
  }
}
