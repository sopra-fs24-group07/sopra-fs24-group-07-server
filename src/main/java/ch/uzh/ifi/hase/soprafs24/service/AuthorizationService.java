package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Authorization Service
 * Check if user exists and if the token is valid
 */
@Service
public class AuthorizationService {
  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;

  @Autowired
  public AuthorizationService(@Qualifier("userRepository") UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Logs in the user with the given username and password
   * @param username the username of the user
   * @param password the password of the user
   * @return the token of the user, null otherwise
   */
  public String login(String username, String password) {
    User foundUser = userRepository.findByUsername(username);
    return foundUser != null && foundUser.getPassword().equals(password) ? foundUser.getToken()
                                                                         : null;
  }

  /**
   * Checks if the token is valid
   *
   * @param token the token to be checked
   * @return true if the token is valid, false otherwise
   */
  public boolean isAuthorized(String token) {
    return this.userRepository.findByToken(token) != null;
  }

  /**
   * Checks if the token belongs to the user with the given username
   *
   * @param token the token to be checked
   * @param username the username to be checked
   * @return true if the token belongs to the user with the given username, false otherwise
   */
  public boolean isAuthorized(String token, String username) {
    User foundUser = userRepository.findByToken(token);
    return foundUser != null && foundUser.getUsername().equals(username);
  }
}
