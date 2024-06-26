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

  // Constructor for the service with WebClient and ObjectMapper
  public AIService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
    this.webClient = webClientBuilder.baseUrl("https://api.openai.com/v1").build();
    this.objectMapper = objectMapper;
  }

  /**
   * Method to generate a description from a provided prompt
   *
   * @param prompt The input text that needs a description
   * @return A description generated by the AI
   * @throws Exception If the prompt is invalid or there is a failure in the AI service
   */
  public Optional<String> generateDescription(String prompt) throws Exception {
    String requestBody;
    try {
      requestBody = createRequestBody(prompt);
    } catch (Exception e) {
      // throw 400 if there was an error in the prompt building
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Some fields are invalid. Could not build prompt to ask AI.");
    }

    // send requests (may throw 400 or 502
    String responseBody = sendPostRequest(requestBody);
    String description = parseResponseBody(responseBody);
    return Optional.of(description);
  }

  /**
   * Method to create a request body for the POST request
   * @param prompt The input text that needs a description
   * @return A string formatted as a JSON object to be sent as the body of the POST request
   */
  private String createRequestBody(String prompt) {
    return String.format(
        "{\"model\": \"gpt-3.5-turbo-instruct\", \"prompt\":\"%s\", \"max_tokens\": 50}", prompt);
  }

  /**
   * Method to send the POST request to the OpenAI API
   * @param requestBody The body of the POST request, formatted as a JSON object
   * @return The response body from the AI service as a string
   * @throws Exception If there is a client error (400), server error (500), or if the AI service
   *     fails
   */
  private String sendPostRequest(String requestBody) {
    Mono<String> response =
        webClient.post()
            .uri("/completions")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
            .body(BodyInserters.fromValue(requestBody))
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError,
                clientResponse
                -> Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "External AI Service could not understand the prompt")))
            .onStatus(HttpStatus::is5xxServerError,
                clientResponse
                -> Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "External AI Service failed")))
            .bodyToMono(String.class);
    try {
      return response.block();
    } catch (Exception e) {
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY, "AI Service failed, Could not understand what it was saying.");
    }
  }

  /**
   * Method to parse the response body from the AI Service
   * @param responseBody The response body from the AI service as a string
   * @return The parsed text from the response body
   * @throws Exception If there is an error parsing the response body
   */
  private String parseResponseBody(String responseBody) throws Exception {
    JsonNode rootNode = objectMapper.readTree(responseBody);
    JsonNode textNode = rootNode.path("choices").get(0).path("text");
    if (textNode.isMissingNode()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid response from AI Service");
    }
    return textNode.asText().trim();
  }
}
