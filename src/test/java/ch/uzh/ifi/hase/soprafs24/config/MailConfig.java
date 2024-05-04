package ch.uzh.ifi.hase.soprafs24.config;

import com.mailjet.client.MailjetClient;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"dev", "devRun"})
public class MailConfig {
  @Bean
  @Primary
  public MailjetClient mailjetClient() {
    System.out.println("mailjet mocker");
    return Mockito.mock(MailjetClient.class);
  }
}
