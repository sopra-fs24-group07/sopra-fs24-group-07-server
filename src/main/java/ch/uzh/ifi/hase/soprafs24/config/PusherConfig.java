package ch.uzh.ifi.hase.soprafs24.config;

import com.pusher.rest.Pusher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!dev")
public class PusherConfig {
  @Value("${PUSHER_APP_ID}") private String appId;

  @Value("${PUSHER_KEY}") private String key;

  @Value("${PUSHER_SECRET}") private String secret;

  @Bean
  public Pusher pusher() {
    Pusher pusher = new Pusher(appId, key, secret);
    pusher.setCluster("eu");
    pusher.setEncrypted(true);
    return pusher;
  }
}
