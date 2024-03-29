package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ServiceHelperTests {
  @Test
  public void testCheckValidString_NullString() {
    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class, () -> { ServiceHelpers.checkValidString(null, "Field"); });
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    assertEquals("Field cannot be empty or only whitespace!", exception.getReason());
  }

  @Test
  public void testCheckValidString_EmptyString() {
    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class, () -> { ServiceHelpers.checkValidString("", "Field"); });
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    assertEquals("Field cannot be empty or only whitespace!", exception.getReason());
  }

  @Test
  public void testCheckValidString_WhitespaceString() {
    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class, () -> { ServiceHelpers.checkValidString("   ", "Field"); });
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    assertEquals("Field cannot be empty or only whitespace!", exception.getReason());
  }

  @Test
  public void testCheckValidString_ValidString() {
    // This test will pass if no exception is thrown
    ServiceHelpers.checkValidString("Productiviteam", "Field");
  }
}
