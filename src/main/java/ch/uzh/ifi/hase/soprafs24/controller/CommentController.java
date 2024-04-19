package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Comment;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.CommentGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.CommentPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.AuthorizationService;
import ch.uzh.ifi.hase.soprafs24.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1")
public class CommentController {
  private final CommentService commentService;
  private final AuthorizationService authorizationService;

  CommentController(CommentService commentService, AuthorizationService authorizationService) {
    this.commentService = commentService;
    this.authorizationService = authorizationService;
  }

  @PostMapping("/teams/{teamId}/tasks/{taskId}/comments")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public CommentGetDTO createComment(@PathVariable Long teamId, @PathVariable Long taskId,
      @RequestBody CommentPostDTO commentPostDTO, @RequestHeader("Authorization") String token) {
    // Convert API comment to internal representation
    Comment commentInput = DTOMapper.INSTANCE.convertCommentPostDTOtoEntity(commentPostDTO);

    // Check if user is authorized to create comment
    User authorizedUser = authorizationService.isAuthorizedAndBelongsToTeam(
        token, commentPostDTO.getUserId(), teamId);

    // Create comment
    Comment createdComment = commentService.createComment(commentInput, taskId);

    // Convert internal representation of comment back to API
    return DTOMapper.INSTANCE.convertEntityToCommentGetDTO(createdComment);
  }
}
