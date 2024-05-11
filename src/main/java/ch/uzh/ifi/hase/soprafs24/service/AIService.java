package ch.uzh.ifi.hase.soprafs24.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
public class AIService {
  private final WebClient webClient;
  private final ObjectMapper objectMapper;

  @Value("${OPENAI_API_KEY}") private String openAiApiKey;

  public AIService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
    this.webClient = webClientBuilder.baseUrl("https://api.openai.com/v1").build();
    this.objectMapper = objectMapper;
  }

  public String generateDescription(String prompt) {
    try {
      String requestBody = String.format(
          "{\"model\": \"gpt-3.5-turbo-instruct\", \"prompt\":\" %s\", \"max_tokens\": 50}",
          prompt);

      Mono<String> response =
          webClient.post()
              .uri("/completions")
              .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
              .body(BodyInserters.fromValue(requestBody))
              .retrieve()
              .bodyToMono(String.class);

      String responseBody = response.block();
      JsonNode rootNode = objectMapper.readTree(responseBody);
      JsonNode textNode = rootNode.path("choices").get(0).path("text");
      return textNode.asText().trim();
    } catch (Exception e) {
      System.out.println("Mail API Error: " + e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
          "GPT API Exception. Did not make successful call to the external AI. Please contact "
              + "Administrator.");
    }
  }
}
