package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Comment;
import ch.uzh.ifi.hase.soprafs24.entity.Task;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.CommentRepository;
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

    Task task = taskService.getTask(taskId);

    if (task == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
    }

    newComment.setTask(task);

    newComment = commentRepository.save(newComment);

    if (newComment == null) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save comment");
    }

    commentRepository.flush();

    log.debug("Successfully created comment: {}", newComment);

    return newComment;
  }
}
