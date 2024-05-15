package ch.uzh.ifi.hase.soprafs24.config;

import ch.uzh.ifi.hase.soprafs24.agora.RtcTokenBuilder2;
import ch.uzh.ifi.hase.soprafs24.agora.RtmTokenBuilder2;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class AgoraConfigDev {
  @Bean
  @Primary
  public AgoraCredentials agoraConfig() {
    return Mockito.mock(AgoraCredentials.class);
  }

  @Bean
  @Primary
  public RtcTokenBuilder2 rtcTokenBuilder2() {
    return Mockito.mock(RtcTokenBuilder2.class);
  }

  @Bean
  @Primary
  public RtmTokenBuilder2 rtmTokenBuilder2() {
    return Mockito.mock(RtmTokenBuilder2.class);
  }
}
