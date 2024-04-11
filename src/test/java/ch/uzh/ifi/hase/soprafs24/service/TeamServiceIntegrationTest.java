package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Test class for using the TeamResource REST resource.
 *
 * @see TeamService
 */
@WebAppConfiguration
@SpringBootTest
public class TeamServiceIntegrationTest {
  @Qualifier("teamRepository") @Autowired private TeamRepository teamRepository;

  @Autowired private TeamService teamService;

  @BeforeEach
  public void setup() {
    teamRepository.deleteAll();
  }

  @Test
  public void getTeamByTeamUUID_success() {
    // given
    Team team = new Team();
    team.setName("productiviTeam");
    team.setDescription("We are a productive team!");
    team.setTeamUUID("team-uuid");

    // when
    teamRepository.saveAndFlush(team);

    // then
    Team foundTeam = teamService.getTeamByTeamUUID(team.getTeamUUID());

    assertEquals(team.getTeamId(), foundTeam.getTeamId());
    assertEquals(team.getName(), foundTeam.getName());
    assertEquals(team.getDescription(), foundTeam.getDescription());
    assertEquals(team.getTeamUUID(), foundTeam.getTeamUUID());
  }

  @Test
  public void getTeamByTeamUUID_teamNotFound_throwsException() {
    // given
    Team team = new Team();
    team.setName("productiviTeam");
    team.setDescription("We are a productive team!");
    team.setTeamUUID("team-uuid");

    // when
    teamRepository.saveAndFlush(team);

    // then
    assertThrows(
        RuntimeException.class, () -> teamService.getTeamByTeamUUID("non-existing-team-uuid"));
  }

  @Test
  public void createTeam_validInputs_success() {
    String teamName = "productiviTeam";

    // given
    Team testTeam = new Team();
    testTeam.setName(teamName);
    testTeam.setDescription("We are a productive team!");

    // when
    Team createdTeam = teamService.createTeam(testTeam);

    // then
    assertEquals(testTeam.getTeamId(), createdTeam.getTeamId());
    assertEquals(testTeam.getName(), createdTeam.getName());
    assertEquals(testTeam.getDescription(), createdTeam.getDescription());

    // only one team should exist
    assertEquals(1, teamRepository.findAll().size());
  }

  @Test
  public void createTeam_validInputs_twoTeamsWithSameName_success() {
    String teamName = "productiviTeam";

    // given
    Team testTeam1 = new Team();
    testTeam1.setName(teamName);
    testTeam1.setDescription("We are a productive team!");

    Team testTeam2 = new Team();
    testTeam2.setName(teamName);
    testTeam2.setDescription("We are also a productive team!");

    // when
    Team createdTeam1 = teamService.createTeam(testTeam1);
    Team createdTeam2 = teamService.createTeam(testTeam2);

    // then
    assertEquals(testTeam1.getTeamId(), createdTeam1.getTeamId());
    assertEquals(testTeam1.getName(), createdTeam1.getName());
    assertEquals(testTeam1.getDescription(), createdTeam1.getDescription());

    assertEquals(testTeam2.getTeamId(), createdTeam2.getTeamId());
    assertEquals(testTeam2.getName(), createdTeam2.getName());
    assertEquals(testTeam2.getDescription(), createdTeam2.getDescription());

    // same name, different id
    assertNotEquals(createdTeam1.getTeamId(), createdTeam2.getTeamId());
    assertEquals(createdTeam1.getName(), createdTeam2.getName());

    // two teams should exist
    assertEquals(2, teamRepository.findAll().size());
  }
}
