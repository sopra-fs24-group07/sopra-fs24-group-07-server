package ch.uzh.ifi.hase.soprafs24.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ServiceHelpers {
  /**
   * Checks if a string is valid (not null, not empty, not only whitespace)
   * @param s string to check
   * @param field name of the field to check, is used in the exception message
   * @throws ResponseStatusException if the string is invalid
   */
  public static void checkValidString(String s, String field) {
    if (s == null || s.trim().isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, field + " cannot be empty or only whitespace!");
    }
  }
}
