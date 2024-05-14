package ch.uzh.ifi.hase.soprafs24.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Profile("dev")
public class WebConfig {
  @Bean
  public WebClient webClient() {
    return WebClient.create();
  }
}
