package ch.uzh.ifi.hase.soprafs24.config;

import com.pusher.rest.Pusher;
import org.hibernate.cfg.NotYetImplementedException;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class PusherConfigDev {
  @Bean
  @Primary
  public Pusher pusher() {
    // throw new NotYetImplementedException("not yet implemented");
    return Mockito.mock(Pusher.class);
  }
}
