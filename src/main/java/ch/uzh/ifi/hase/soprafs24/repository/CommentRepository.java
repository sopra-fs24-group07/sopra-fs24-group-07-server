package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Comment;
import ch.uzh.ifi.hase.soprafs24.entity.Task;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("commentRepository")
public interface CommentRepository extends JpaRepository<Comment, Long> {

  /**
   * Finds a comment by its Creation Date in Descending Order
   * @param task
   * @return
   */
  List<Comment> findByTaskOrderByCreationDateDesc(Task task);

  /**
   * Finds a comment by its commentId.
   * @param commentId
   * @return
   */
  Comment findByCommentId(Long commentId);

  /**
   * Finds all comments by task.
   *
   * Usage:
   * <pre>{@code
   * Task task = taskRepository.findById(taskId);
   * List<Comment> comments = commentRepository.findByTask(task);
   * }</pre>
   *
   * @param task
   * @return List<Comment>
   */
  List<Comment> findByTask(Task task);

  /**
   * Finds all comments created by a specific user.
   *
   * Usage:
   * <pre>{@code
   *     User user = userRepository.findById(userId);
   *     List<Comment> comments = commentRepository.findByUser(user);
   * }</pre>
   *
   * @param user The user who created the comments.
   * @return A list of comments created by the given user.
   */
  List<Comment> findByUser(User user);
}
