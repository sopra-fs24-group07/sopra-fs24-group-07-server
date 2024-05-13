package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

public class AIServiceTest {
  @Mock private WebClient webClient;

  @Mock private WebClient.Builder webClientBuilder;

  @Mock private WebClient.RequestHeadersSpec requestHeadersSpec;

  @Mock private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

  @Mock private WebClient.RequestBodySpec requestBodySpec;

  @Mock private WebClient.RequestBodyUriSpec requestBodyUriSpec;

  @Mock private WebClient.ResponseSpec responseSpec;

  private AIService aiService;

  private ObjectMapper objectMapper;
  private String openAiApiKey = "test_key";

  @BeforeEach
  private void setup() {
    MockitoAnnotations.openMocks(this);
    objectMapper = new ObjectMapper();
    when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
    when(webClientBuilder.build()).thenReturn(webClient);
    aiService = new AIService(webClientBuilder, objectMapper); // manually initializing AIService
  }

  @Test
  public void generateDescription_validInput_success() {
    String prompt = "test prompt";
    String aiResponse = "{\"choices\": [{\"text\": \"test description\"}]}";
    String expectedDescription = "test description";

    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(aiResponse));
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

    Optional<String> description = aiService.generateDescription(prompt);

    verify(webClient, times(1)).post();
    verify(requestBodyUriSpec, times(1)).uri(anyString());
    verify(requestBodySpec, times(2)).header(anyString(), anyString());
    verify(requestBodySpec, times(1)).body(any());
    verify(requestHeadersSpec, times(1)).retrieve();
    verify(responseSpec, times(1)).bodyToMono(String.class);

    assertTrue(description.isPresent());
    assertEquals(expectedDescription, description.get());
  }

  @Test
  public void generateDescription_apiException_throwsException() {
    String prompt = "test prompt";

    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(String.class))
        .thenReturn(Mono.error(new Exception("AI Service failed")));

    assertThrows(ResponseStatusException.class, () -> aiService.generateDescription(prompt));
  }

  @Test
  public void generateDescription_invalidResponse_throwsException() {
    String prompt = "test prompt";
    String aiResponse = "{\"choices\": [{\"invalid\": \"test description\"}]}";

    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(aiResponse));

    assertThrows(ResponseStatusException.class, () -> aiService.generateDescription(prompt));
  }
}
