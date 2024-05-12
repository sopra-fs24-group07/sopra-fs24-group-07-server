package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.service.AIService;
import ch.uzh.ifi.hase.soprafs24.service.AuthorizationService;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    // check if user is authorized (valid token)
    User authorizedUser = authorizationService.isAuthorized(token);
  
    if (authorizedUser == null) {
      return new ResponseEntity<>("User is not authorized", HttpStatus.UNAUTHORIZED);
    }
  
    try {
      String cleanRequestBody = requestBody.replace("\"", "");
      String prompt = "Please write a very short poem about a team named" + cleanRequestBody;
  
      // Call the AI service to generate a description
      Optional<String> description = aiService.generateDescription(prompt);
      if (!description.isPresent()) {
        return new ResponseEntity<>("Unable to generate description", HttpStatus.BAD_REQUEST);
      }
  
      System.out.println("The description has been set to: " + description.get());
      return new ResponseEntity<>(description.get(), HttpStatus.OK);
    } 
    catch (RuntimeException e) {
      // Log the error message and return a 400 Bad Request response
      System.err.println(e.getMessage());
      return new ResponseEntity<>("Unable to call AI service", HttpStatus.BAD_REQUEST);
    }
  }
}
