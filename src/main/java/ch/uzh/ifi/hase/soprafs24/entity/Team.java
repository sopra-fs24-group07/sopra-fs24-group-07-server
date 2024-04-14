package ch.uzh.ifi.hase.soprafs24.entity;

import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name = "TEAM")
public class Team implements Serializable {
  private static final long serialVersionUID = 1L;

  // @Id: primary key
  // @GeneratedValue: auto increment
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long teamId;

  // varchar(100), not null
  @Column(length = 100, nullable = false) private String name;

  // varchar(500), not null
  @Column(length = 500, nullable = false) private String description;

  @Column(nullable = false, unique = true) private String teamUUID;

  public Long getTeamId() {
    return teamId;
  }

  public void setTeamId(Long teamId) {
    this.teamId = teamId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getTeamUUID() {
    return teamUUID;
  }

  public void setTeamUUID(String teamUUID) {
    this.teamUUID = teamUUID;
  }
}
