package ch.uzh.ifi.hase.soprafs24.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Profile({"dev", "devRun"})
public class WebConfig {
  @Bean
  @Primary
  public WebClient webClient() {
    return WebClient.create("http://localhost:8080"); // Mocked WebClient
  }
}
