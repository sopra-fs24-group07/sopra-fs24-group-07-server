package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.entity.TeamUser;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.DataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
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

  private final TeamUserService teamUserService;

  @Autowired
  public UserService(
      @Qualifier("userRepository") UserRepository userRepository, TeamUserService teamUserService) {
    this.userRepository = userRepository;
    this.teamUserService = teamUserService;
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
    try {
      newUser = userRepository.save(newUser);
      userRepository.flush();
    } catch (DataIntegrityViolationException e) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Could not register user. Please check length constraints.");
    }

    log.info("Created Information for User: {} with id {}", newUser, newUser.getUserId());
    return newUser;
  }

  // User updating:
  public User updateUser(User userToUpdate) {
    ServiceHelpers.checkValidString(userToUpdate.getUsername(), "Username");
    ServiceHelpers.checkValidString(userToUpdate.getName(), "Name");
    ServiceHelpers.checkValidString(userToUpdate.getPassword(), "Password");

    User updatedUser =
        userRepository.findById(userToUpdate.getUserId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    updatedUser.setName(userToUpdate.getName());
    updatedUser.setUsername(userToUpdate.getUsername());
    updatedUser.setPassword(userToUpdate.getPassword());

    try {
      userRepository.save(updatedUser);
      userRepository.flush();
    } catch (DataIntegrityViolationException e) {
      Throwable cause = e.getCause();
      // unique
      if (cause instanceof ConstraintViolationException) {
        ConstraintViolationException cvCause = (ConstraintViolationException) cause;
        String constraintName = cvCause.getConstraintName().toLowerCase();
        if (constraintName.contains("uk_")) {
          throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists.");
        }
      }
      // length
      else if (cause instanceof DataException) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Could not update user. Please check length constraints.");
      }
      // others
      else {
        throw e; // rethrow the original exception if it's not one we can handle
      }
    }

    log.debug("Updated Information for User: {}", updatedUser);
    return updatedUser;
  }

  // User deletion:
  public void deleteUser(Long userId) {
    User existingUser = userRepository.findById(userId).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    // delete all teams that the user is part of (this will also call pusher realtime update)
    List<Team> teams = teamUserService.getTeamsOfUser(existingUser.getUserId());
    for (Team team : teams) {
      teamUserService.deleteUserOfTeam(team.getTeamId(), existingUser.getUserId());
    }

    // delete user
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
          HttpStatus.CONFLICT, String.format(baseErrorMessage, "username", "is"));
    }
  }
}
