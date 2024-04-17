package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Comment;
import ch.uzh.ifi.hase.soprafs24.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class CommentService {
  private final CommentRepository commentRepository;

  @Autowired
  public CommentService(@Qualifier("commentRepository") CommentRepository commentRepository) {
    this.commentRepository = commentRepository;
  }

  public Comment createComment(Comment newComment) {
    ServiceHelpers.checkValidString(newComment.getText(), "text");
    if (newComment.getTask() == null || newComment.getUser() == null) {
      throw new IllegalArgumentException("Task and User must be set");
    }

    newComment = commentRepository.save(newComment);
    commentRepository.flush();

    return newComment;
  }
}
