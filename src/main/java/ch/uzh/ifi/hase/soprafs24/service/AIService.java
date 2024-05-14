package ch.uzh.ifi.hase.soprafs24.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
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

  public Optional<String> generateDescription(String prompt) {
    try {
      String requestBody = createRequestBody(prompt);
      String responseBody = sendPostRequest(requestBody);
      String description = parseResponseBody(responseBody);
      return Optional.of(description);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "AI Service failed");
    }
  }

  private String createRequestBody(String prompt) {
    return String.format(
        "{\"model\": \"gpt-3.5-turbo-instruct\", \"prompt\":\"%s\", \"max_tokens\": 50}", prompt);
  }

  private String sendPostRequest(String requestBody) {
    Mono<String> response = webClient.post()
                                .uri("/completions")
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
                                .body(BodyInserters.fromValue(requestBody))
                                .retrieve()
                                .onStatus(HttpStatus::is4xxClientError,
                                    clientResponse -> Mono.error(new Exception("Client Error")))
                                .onStatus(HttpStatus::is5xxServerError,
                                    clientResponse -> Mono.error(new Exception("Server Error")))
                                .bodyToMono(String.class);

    return response.block();
  }

  private String parseResponseBody(String responseBody) throws Exception {
    JsonNode rootNode = objectMapper.readTree(responseBody);
    JsonNode textNode = rootNode.path("choices").get(0).path("text");
    return textNode.asText().trim();
  }
}
