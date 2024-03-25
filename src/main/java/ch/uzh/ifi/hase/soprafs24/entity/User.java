package ch.uzh.ifi.hase.soprafs24.entity;

import java.io.Serializable;
import javax.persistence.*;

/**
 * Internal User Representation
 * This class composes the internal representation of the user and defines how
 * the user is stored in the database.
 * Every variable will be mapped into a database field with the @Column
 * annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database -> composes
 * the primary key
 */
@Entity
@Table(name = "USER")
public class User implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id @GeneratedValue private Long userId;

  @Column(length = 100, nullable = false) private String name;

  @Column(length = 100, nullable = false, unique = true) private String username;

  // should be hash
  @Column(nullable = false) private String password;

  @Column(nullable = false, unique = true) private String token;

  // TODO image

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
}
