package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.service.AIService;
import ch.uzh.ifi.hase.soprafs24.service.AuthorizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
public class AIController {
  private final AIService aiService;
  private final AuthorizationService authorizationService;

  public AIController(AIService aiService, AuthorizationService authorizationService) {
    this.aiService = aiService;
    this.authorizationService = authorizationService;
  }

  @PostMapping("/gpt-3.5-turbo-instruct")
  public ResponseEntity<String> callGpt35Instruct(
      @RequestBody String requestBody, @RequestHeader("Authorization") String token) {
    User authorizedUser = authorizationService.isAuthorized(token);
    String cleanRequestBody = requestBody.replace("\"", "");
    String prompt = "Please write a very short poem about a team named" + cleanRequestBody;
    String description = aiService.generateDescription(prompt);
    System.out.println("The description has been set to: " + description);
    return ResponseEntity.ok(description);
  }
}