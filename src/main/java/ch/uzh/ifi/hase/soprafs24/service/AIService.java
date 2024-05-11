package ch.uzh.ifi.hase.soprafs24.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
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
      String descriptionPrompt = String.format("%s", prompt);
      String requestBody = String.format(
          "{\"model\": \"gpt-3.5-turbo-instruct\", \"prompt\":\" %s\", \"max_tokens\": 50}",
          descriptionPrompt);
      System.out.println("AI API request body" + requestBody);
      Mono<String> response =
          webClient.post()
              .uri("/completions")
              .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
              .body(BodyInserters.fromValue(requestBody))
              .retrieve()
              .bodyToMono(String.class);

      System.out.println("AI API response:" + response);
      String responseBody = response.block();
      System.out.println("AI API response.block():" + response.block());
      JsonNode rootNode = objectMapper.readTree(responseBody);
      JsonNode textNode = rootNode.path("choices").get(0).path("text");
      return textNode.asText().trim();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
