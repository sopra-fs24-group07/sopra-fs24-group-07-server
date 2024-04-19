package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Comment;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.CommentGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.CommentPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.AuthorizationService;
import ch.uzh.ifi.hase.soprafs24.service.CommentService;
import java.util.List;
import java.util.stream.Collectors;
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

  @GetMapping("/teams/{teamId}/tasks/{taskId}/comments")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<CommentGetDTO> getComments(@PathVariable Long teamId, @PathVariable Long taskId,
      @RequestHeader("Authorization") String token) {
    // Get the user for that user token in header and check if that token is in team
    authorizationService.isAuthorizedAndBelongsToTeam(token, teamId);

    // Get comments
    List<Comment> comments = commentService.getCommentsByTaskId(taskId);

    // Convert internal representation of comments back to API
    return comments.stream()
        .map(DTOMapper.INSTANCE::convertEntityToCommentGetDTO)
        .collect(Collectors.toList());
  }
}
