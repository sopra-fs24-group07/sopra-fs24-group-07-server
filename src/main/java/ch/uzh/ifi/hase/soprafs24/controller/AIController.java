package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.AIPrompt;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.AIPromptGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.AIPromptTeamDescriptionPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.AIService;
import ch.uzh.ifi.hase.soprafs24.service.AuthorizationService;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/ai")
public class AIController {
  private final Logger log = LoggerFactory.getLogger(AIController.class);
  private final AIService aiService;
  private final AuthorizationService authorizationService;

  public AIController(AIService aiService, AuthorizationService authorizationService) {
    this.aiService = aiService;
    this.authorizationService = authorizationService;
  }

  @PostMapping("/gpt-3.5-turbo-instruct")
  public ResponseEntity<AIPromptGetDTO> callGpt35Instruct(
      @RequestBody AIPromptTeamDescriptionPostDTO requestBody,
      @RequestHeader("Authorization") String token) {
    // check if user is authorized (valid token)
    User authorizedUser = authorizationService.isAuthorized(token);
    // Get the prompt parameter from the request body
    AIPrompt aiPrompt =
        DTOMapper.INSTANCE.convertAIPromptTeamDescriptionPostDTOtoEntity(requestBody);
    String promptParameter = aiPrompt.getPrompt();

    try {
      String prompt = "Please write a very short poem about a team named" + aiPrompt.getPrompt();
      // Call the AI service to generate a description
      Optional<String> description = aiService.generateDescription(prompt);
      if (!description.isPresent()) {
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI Service failed");
      }
      log.info("The description has been generated: {}", description.get());
      aiPrompt.setAnswer(description.get());
      return new ResponseEntity<>(
          DTOMapper.INSTANCE.convertEntityToAIPromptGetDTO(aiPrompt), HttpStatus.OK);
    } catch (Exception e) {
      // Log the error message and return a 502 Bad Gateway response
      System.err.println(e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI Service failed");
    }
  }
}
