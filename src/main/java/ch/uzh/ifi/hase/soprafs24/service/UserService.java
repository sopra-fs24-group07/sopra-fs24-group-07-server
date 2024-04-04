package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {
  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;

  @Autowired
  public UserService(@Qualifier("userRepository") UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  // User creation:
  public User createUser(User newUser) {
    // check that username, name, and password are not empty strings
    ServiceHelpers.checkValidString(newUser.getUsername(), "Username");
    ServiceHelpers.checkValidString(newUser.getName(), "Name");
    ServiceHelpers.checkValidString(newUser.getPassword(), "Password");

    newUser.setToken(UUID.randomUUID().toString());
    checkIfUserExists(newUser);
    // saves the given entity but data is only persisted in the database once
    // flush() is called
    newUser = userRepository.save(newUser);
    userRepository.flush();

    log.info("Created Information for User: {} with id {}", newUser, newUser.getUserId());
    return newUser;
  }

  // User updating:
  public User updateUser(User userToUpdate) {
    ServiceHelpers.checkValidString(userToUpdate.getUsername(), "Username");
    ServiceHelpers.checkValidString(userToUpdate.getName(), "Name");
    ServiceHelpers.checkValidString(userToUpdate.getPassword(), "Password");

    User existingUser =
        userRepository.findById(userToUpdate.getUserId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    existingUser.setName(userToUpdate.getName());
    existingUser.setUsername(userToUpdate.getUsername());
    existingUser.setPassword(userToUpdate.getPassword());

    userRepository.save(existingUser);
    userRepository.flush();

    log.debug("Updated Information for User: {}", existingUser);
    return existingUser;
  }

  // User deletion:
  public void deleteUser(Long userId) {
    User existingUser = userRepository.findById(userId).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    userRepository.delete(existingUser);
    userRepository.flush();

    log.debug("Deleted User: {}", existingUser);
  }

  /**
   * This is a helper method that will check the uniqueness criteria of the
   * username defined in the User entity. The method will do nothing if the input is unique
   * and throw an error otherwise.
   *
   * @param userToBeCreated
   * @throws org.springframework.web.server.ResponseStatusException
   * @see User
   */
  private void checkIfUserExists(User userToBeCreated) {
    User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());

    String baseErrorMessage =
        "The %s provided %s not unique. Therefore, the user could not be created!";
    if (userByUsername != null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, String.format(baseErrorMessage, "username", "is"));
    }
  }
}
