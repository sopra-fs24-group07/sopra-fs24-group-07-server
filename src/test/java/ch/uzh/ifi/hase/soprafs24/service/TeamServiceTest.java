package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

public class TeamServiceTest {
  @Mock private TeamRepository teamRepository;

  @InjectMocks private TeamService teamService;

  private Team testTeam;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);

    // given
    testTeam = new Team();
    testTeam.setTeamId(1L);
    testTeam.setName("productiviTeam");
    testTeam.setDescription("We are a productive team!");

    // when -> any object is being save in the teamRepository -> return the dummy testTeam
    Mockito.when(teamRepository.save(Mockito.any())).thenReturn(testTeam);
  }

  @Test
  public void createTeam_validInputs_success() {
    // when -> any object is being save in the teamRepository -> return the dummy testTeam
    Team createdTeam = teamService.createTeam(testTeam);

    // then
    Mockito.verify(teamRepository, Mockito.times(1)).save(Mockito.any());

    assertEquals(testTeam.getTeamId(), createdTeam.getTeamId());
    assertEquals(testTeam.getName(), createdTeam.getName());
    assertEquals(testTeam.getDescription(), createdTeam.getDescription());
    assertNotNull(createdTeam.getTeamUUID());
  }

  @Test
  public void createTeam_invalidInputs_throwsException() {
    // when -> any object is being saved in the teamRepository -> return the dummy testTeam
    Team invalidTeam = new Team(); // missing name

    // then -> attempt to create team with empty name -> check that an error is thrown
    assertThrows(ResponseStatusException.class, () -> teamService.createTeam(invalidTeam));
  }
}
