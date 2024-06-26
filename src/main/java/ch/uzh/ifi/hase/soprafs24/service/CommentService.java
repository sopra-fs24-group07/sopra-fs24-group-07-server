package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Comment;
import ch.uzh.ifi.hase.soprafs24.entity.Task;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.CommentRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class CommentService {
  private final Logger log = LoggerFactory.getLogger(CommentService.class);
  private final CommentRepository commentRepository;
  private final TaskService taskService;

  @Autowired
  public CommentService(@Qualifier("commentRepository") CommentRepository commentRepository,
      TaskService taskService) {
    this.commentRepository = commentRepository;
    this.taskService = taskService;
  }

  public Comment createComment(Comment newComment, Long taskId) {
    ServiceHelpers.checkValidString(newComment.getText(), "text");

    // check that the task exists and add to comment
    newComment.setTask(taskService.getTask(taskId));

    // set creation time
    newComment.setCreationDate(LocalDateTime.now());

    // save
    try {
      newComment = commentRepository.save(newComment);
      commentRepository.flush();
    } catch (Exception e) {
      log.error("Failed to create comment: {}", newComment, e);
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Failed to create comment: " + e.getMessage());
    }

    log.debug("Successfully created comment: {}", newComment);
    return newComment;
  }

  public List<Comment> getCommentsByTaskId(Long taskId) {
    // check that the task exists
    Task task = taskService.getTask(taskId);

    // search for comments with that task
    List<Comment> comments = commentRepository.findByTaskOrderByCreationDateDesc(task);
    if (comments == null) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Comments not found for requested task.");
    }

    log.debug("Successfully fetched comments for task: {}", taskId);

    return comments;
  }

  /**
   * Deletes a comment by its id
   * @param commentId the id of the comment to delete
   * @throws ResponseStatusException 404 if the comment does not exist
   * @return the deleted comment
   */
  public Comment deleteCommentById(Long commentId) {
    // check that the comment exists
    Comment comment = commentRepository.findById(commentId).orElseThrow(
        ()
            -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Comment not found with id: " + commentId));

    // delete
    commentRepository.delete(comment);
    commentRepository.flush();

    log.debug("Successfully deleted comment: {}", comment);
    return comment;
  }
}
