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
import org.springframework.web.server.ResponseStatusException;

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

  // region getTeamByTeamId tests
  @Test
  public void getTeamByTeamId_success() {
    // given
    Team team = new Team();
    team.setName("productiviTeam");
    team.setDescription("We are a productive team!");
    team.setTeamUUID("team-uuid");

    // when
    teamRepository.saveAndFlush(team);

    // then
    Team foundTeam = teamService.getTeamByTeamId(team.getTeamId());

    assertEquals(team.getTeamId(), foundTeam.getTeamId());
    assertEquals(team.getName(), foundTeam.getName());
    assertEquals(team.getDescription(), foundTeam.getDescription());
  }

  @Test
  public void getTeamByTeamId_teamNotFound_throwsException() {
    // given
    Team team = new Team();
    team.setName("productiviTeam");
    team.setDescription("We are a productive team!");
    team.setTeamUUID("team-uuid");

    // when
    teamRepository.saveAndFlush(team);

    // then
    assertThrows(RuntimeException.class, () -> teamService.getTeamByTeamId(999L));
  }
  // endregion

  // region getTeamByTeamUUID tests
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
  // endregion

  // region createTeam tests
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
  // endregion

  // region updateTeam tests

  @Test
  public void updateTeam_validInputs_success() {
    // given
    Team testTeam = new Team();
    testTeam.setName("productiviTeam");
    testTeam.setDescription("We are a productive team!");
    testTeam.setTeamUUID("team-uuid");

    // when
    teamRepository.saveAndFlush(testTeam);

    // update team
    Team teamToUpdate = new Team();
    teamToUpdate.setTeamId(testTeam.getTeamId());
    teamToUpdate.setName("productivierTeam");
    teamToUpdate.setDescription("We are even more productive!");

    // update team call
    Team updatedTeam = teamService.updateTeam(teamToUpdate);

    // then
    assertEquals(teamToUpdate.getTeamId(), updatedTeam.getTeamId());
    assertEquals(teamToUpdate.getName(), updatedTeam.getName());
    assertEquals(teamToUpdate.getDescription(), updatedTeam.getDescription());
  }

  /* test if changing team name to an existing team name is ok*/
  @Test
  public void updateTeam_validInputs_changeNameToExistingName_success() {
    // given
    Team testTeam1 = new Team();
    testTeam1.setName("productiviTeam");
    testTeam1.setDescription("We are a productive team!");
    testTeam1.setTeamUUID("team-uuid1");

    Team testTeam2 = new Team();
    testTeam2.setName("productivierTeam");
    testTeam2.setDescription("We are even more productive!");
    testTeam2.setTeamUUID("team-uuid2");

    // when
    teamRepository.saveAndFlush(testTeam1);
    teamRepository.saveAndFlush(testTeam2);

    // update team1
    Team teamToUpdate = new Team();
    teamToUpdate.setTeamId(testTeam1.getTeamId());
    teamToUpdate.setName("productivierTeam");
    teamToUpdate.setDescription("We are even more productive!");

    // update team call
    Team updatedTeam = teamService.updateTeam(teamToUpdate);
    // fetch team by id
    Team foundTeam = teamService.getTeamByTeamId(testTeam1.getTeamId());

    // then
    assertEquals(testTeam1.getTeamId(), foundTeam.getTeamId());
    assertEquals(testTeam1.getTeamUUID(), foundTeam.getTeamUUID());
    assertEquals(teamToUpdate.getName(), foundTeam.getName());
    assertEquals(teamToUpdate.getDescription(), foundTeam.getDescription());
  }

  @Test
  public void updateTeam_teamNotFound_throwsException() {
    // given
    Team testTeam = new Team();
    testTeam.setName("productiviTeam");
    testTeam.setDescription("We are a productive team!");
    testTeam.setTeamUUID("team-uuid");

    // when
    teamRepository.saveAndFlush(testTeam);

    // update team
    Team teamToUpdate = new Team();
    teamToUpdate.setTeamId(999L); // wrong id
    teamToUpdate.setName("productivierTeam");
    teamToUpdate.setDescription("We are even more productive!");

    // then
    assertThrows(RuntimeException.class, () -> teamService.updateTeam(teamToUpdate));
  }

  @Test
  public void updateTeam_invalidName_throwsException() {
    // given
    Team testTeam = new Team();
    testTeam.setName("productiviTeam");
    testTeam.setDescription("We are a productive team!");
    testTeam.setTeamUUID("team-uuid");

    // when
    teamRepository.saveAndFlush(testTeam);

    // update team
    Team teamToUpdate = new Team();
    teamToUpdate.setTeamId(testTeam.getTeamId());
    teamToUpdate.setName(""); // empty name
    teamToUpdate.setDescription("We are even more productive!");

    // then
    assertThrows(ResponseStatusException.class, () -> teamService.updateTeam(teamToUpdate));
  }

  // endregion
}
