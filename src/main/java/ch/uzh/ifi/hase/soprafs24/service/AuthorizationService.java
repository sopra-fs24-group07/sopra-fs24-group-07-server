package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.entity.TeamUser;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Authorization Service
 * Check if user exists and if the token is valid
 */
@Service
public class AuthorizationService {
  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;

  private final TeamUserService teamUserService;

  @Autowired
  public AuthorizationService(
      @Qualifier("userRepository") UserRepository userRepository, TeamUserService teamUserService) {
    this.userRepository = userRepository;
    this.teamUserService = teamUserService;
  }

  /**
   * Logs in the user with the given username and password
   * @param username the username of the user
   * @param password the password of the user
   * @return the token of the user, null otherwise
   */
  public String login(String username, String password) {
    log.info("Trying to log in user '{}'", username); // monitor login attempts

    User foundUser = userRepository.findByUsername(username);
    return foundUser != null && foundUser.getPassword().equals(password) ? foundUser.getToken()
                                                                         : null;
  }

  /**
   * Checks if the token is valid
   *
   * @param token the token to be checked
   * @return the user of the token
   * @throws ResponseStatusException if the token is invalid
   */
  public User isAuthorized(String token) {
    log.info("Checking authorization for token '{}'", token); // monitor authorization attempts

    User foundUser = this.userRepository.findByToken(token);
    if (foundUser == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
    }
    return foundUser;
  }

  /**
   * Checks if the user exists and if the the token belongs to that user.
   *
   * @param token the token to be checked
   * @param userId the userId to be checked
   * @return the user of the token/username
   * @throws ResponseStatusException 404 if the user does not exist; 401 if the token is invalid
   */
  public User isExistingAndAuthorized(String token, Long userId) {
    log.info(
        "Checking authorization for user '{}' which should have the token '{}'", userId, token);

    Optional<User> foundUser = userRepository.findById(userId);

    // user not found
    if (foundUser.isEmpty()) {
      log.info("User '{}' not in DB", userId);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }

    // token not corresponding to user token
    if (!foundUser.get().getToken().equals(token)) {
      log.info("User '{}' does not have token '{}'", userId, token);
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
    }
    return foundUser.get();
  }

  /**
   * Checks if the token exists and the user is in the team
   *
   * @param token the token to be checked
   * @param teamId to be checked
   * @return the user of the token/username
   * @throws ResponseStatusException 404 if the team is not found; 401 if the token is invalid the
   *     user does not belong to the team
   */
  public User isAuthorizedAndBelongsToTeam(String token, Long teamId) {
    log.info("Checking authorization for token '{}' and teamId '{}'", token,
        teamId); // monitor authorization attempts

    // check if user is authorized
    User foundUser = isAuthorized(token);

    // check if user is in team
    if (teamUserService.getUsersOfTeam(teamId).stream().noneMatch(
            user -> user.getUserId().equals(foundUser.getUserId()))) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not in team");
    }

    return foundUser;
  }

  // Deprecated
  // /**
  //  * Checks if the token belongs to the user with the given userId
  //  *
  //  * @param token the token to be checked
  //  * @param userId the userId to be checked (might not exist)
  //  * @return the user of the token/username
  //  * @throws ResponseStatusException if the token is invalid or does not belong to the user
  //  */
  // public User isAuthorized(String token, Long userId) {
  //   log.info("Checking authorization for token '{}' which belongs to userId '{}'", token,
  //       userId); // monitor authorization attempts
  //
  //   User foundUser = userRepository.findByToken(token);
  //   if (foundUser == null || !foundUser.getUserId().equals(userId)) {
  //     throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
  //   }
  //   return foundUser;
  // }
}
