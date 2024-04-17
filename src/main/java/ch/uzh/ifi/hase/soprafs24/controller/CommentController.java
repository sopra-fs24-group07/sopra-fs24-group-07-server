package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Comment;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.CommentGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.CommentPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.AuthorizationService;
import ch.uzh.ifi.hase.soprafs24.service.CommentService;
import ch.uzh.ifi.hase.soprafs24.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teams/{teamId}/tasks/{taskId}/comments")
public class CommentController {
  private final CommentService commentService;
  private final AuthorizationService authorizationService;
  private final TaskService taskService;

  CommentController(CommentService commentService, AuthorizationService authorizationService,
      TaskService taskService) {
    this.commentService = commentService;
    this.authorizationService = authorizationService;
    this.taskService = taskService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public CommentGetDTO createComment(@PathVariable Long teamId, @PathVariable Long taskId,
      @RequestBody CommentPostDTO commentPostDTO, @RequestHeader("Authorization") String token) {
    User authorizedUser = authorizationService.isAuthorized(token);

    Comment commentInput = DTOMapper.INSTANCE.convertCommentPostDTOtoEntity(commentPostDTO);
    commentInput.setTask(taskService.getTask(taskId));
    commentInput.setUser(authorizedUser); // Set the user of the comment

    Comment createdComment = commentService.createComment(commentInput);

    return DTOMapper.INSTANCE.convertEntityToCommentGetDTO(createdComment);
  }
}
