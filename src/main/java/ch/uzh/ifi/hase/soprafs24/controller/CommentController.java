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
@RequestMapping("api/v1/teams/{teamId}/tasks/{taskId}/comments")
public class CommentController {
  private final CommentService commentService;
  private final AuthorizationService authorizationService;

  CommentController(CommentService commentService, AuthorizationService authorizationService) {
    this.commentService = commentService;
    this.authorizationService = authorizationService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public CommentGetDTO createComment(@PathVariable Long teamId, @PathVariable Long taskId,
      @RequestBody CommentPostDTO commentPostDTO, @RequestHeader("Authorization") String token) {
    // Get the user for that user token in header and check if that token is in team
    User authorUser = authorizationService.isAuthorizedAndBelongsToTeam(token, teamId);

    // Convert API comment to internal representation
    Comment commentInput = DTOMapper.INSTANCE.convertCommentPostDTOtoEntity(commentPostDTO);
    commentInput.setUser(authorUser);

    // Create comment
    Comment createdComment = commentService.createComment(commentInput, taskId);

    // Convert internal representation of comment back to API
    return DTOMapper.INSTANCE.convertEntityToCommentGetDTO(createdComment);
  }
}
