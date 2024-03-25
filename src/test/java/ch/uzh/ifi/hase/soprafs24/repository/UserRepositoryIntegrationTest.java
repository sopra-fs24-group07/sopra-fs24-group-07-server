package ch.uzh.ifi.hase.soprafs24.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
public class UserRepositoryIntegrationTest {
  @Autowired private TestEntityManager entityManager;

  @Autowired private UserRepository userRepository;

  @Test
  public void findByName_success() {
    // given
    User user = new User();
    user.setName("Firstname Lastname");
    user.setUsername("firstname@lastname");
    user.setPassword("1234");
    user.setToken("1");

    entityManager.persist(user);
    entityManager.flush();

    // when
    User found = userRepository.findByName(user.getName());

    // then
    assertNotNull(found.getUserId());
    assertEquals(found.getName(), user.getName());
    assertEquals(found.getUsername(), user.getUsername());
    assertEquals(found.getPassword(), user.getPassword());
    assertEquals(found.getToken(), user.getToken());
  }
}
