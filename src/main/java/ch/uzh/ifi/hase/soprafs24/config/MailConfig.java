package ch.uzh.ifi.hase.soprafs24.config;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!dev")
public class MailConfig {
  @Value("${MAILJET_KEY}") private String key;
  @Value("${MAILJET_SECRET}") private String secret;

  @Bean
  @Primary
  public MailjetClient mailjetClient() {
    ClientOptions options = ClientOptions.builder().apiKey(key).apiSecretKey(secret).build();
    return new MailjetClient(options);
  }
}
