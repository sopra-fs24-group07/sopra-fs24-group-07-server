package ch.uzh.ifi.hase.soprafs24.config;

import ch.uzh.ifi.hase.soprafs24.agora.RtcTokenBuilder2;
import ch.uzh.ifi.hase.soprafs24.agora.RtmTokenBuilder2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!dev")
public class AgoraConfig {
  @Value("${AGORA_APP_ID}") private String appId;
  @Value("${AGORA_APP_CERTIFICATE}") private String appCertificate;

  @Bean
  @Primary
  public AgoraCredentials agoraCredentials() {
    return new AgoraCredentials(appId, appCertificate);
  }

  @Bean
  @Primary
  public RtcTokenBuilder2 rtcTokenBuilder2() {
    return new RtcTokenBuilder2();
  }

  @Bean
  @Primary
  public RtmTokenBuilder2 rtmTokenBuilder2() {
    return new RtmTokenBuilder2();
  }
}
