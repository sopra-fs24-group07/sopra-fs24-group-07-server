package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.repository.TeamRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class TeamServiceTest {
  @Mock private TeamRepository teamRepository;
  @Mock private PusherService pusherService;

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

  // region findByTeamId tests

  @Test
  public void getTeamByTeamId_success() {
    // given
    Team team = new Team();
    team.setTeamId(1L);
    team.setName("Team Name");
    team.setDescription("Team Description");
    team.setTeamUUID("team-uuid");

    // when
    Mockito.when(teamRepository.findById(Mockito.any())).thenReturn(Optional.of(team));

    // then
    Team foundTeam = teamService.getTeamByTeamId(team.getTeamId());

    assertEquals(team.getTeamId(), foundTeam.getTeamId());
    assertEquals(team.getName(), foundTeam.getName());
    assertEquals(team.getDescription(), foundTeam.getDescription());
    assertEquals(team.getTeamUUID(), foundTeam.getTeamUUID());

    // when pusher call -> mock
    Mockito.doNothing().when(pusherService).updateTeam(Mockito.anyString(), Mockito.anyString());
  }

  @Test
  public void getTeamByTeamId_teamNotFound_throwsException() {
    // given
    Team team = new Team();
    team.setTeamId(1L);
    team.setName("Team Name");
    team.setDescription("Team Description");
    team.setTeamUUID("team-uuid");

    // when
    Mockito.when(teamRepository.findById(Mockito.any())).thenReturn(Optional.empty());

    // then
    assertThrows(ResponseStatusException.class, () -> teamService.getTeamByTeamId(2L));
  }

  // endregion

  // region findByTeamUUID tests

  @Test
  public void getTeamByTeamUUID_success() {
    // given
    Team team = new Team();
    team.setName("productiviTeam");
    team.setDescription("We are a productive team!");
    team.setTeamUUID("team-uuid");

    // when
    Mockito.when(teamRepository.findByTeamUUID(Mockito.any())).thenReturn(team);

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
    Mockito.when(teamRepository.findByTeamUUID(Mockito.any())).thenReturn(null);

    // then
    assertThrows(
        ResponseStatusException.class, () -> teamService.getTeamByTeamUUID("invalid team-uuid"));
  }

  // endregion

  // region create team tests

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

    Mockito.verify(teamRepository, Mockito.never()).save(Mockito.any());
  }
  // endregion

  // region edit team tests

  @Test
  public void updateTeam_validInputs_success() {
    // given
    Team updatedTeam = new Team();
    updatedTeam.setTeamId(1L);
    updatedTeam.setName("updatedName");
    updatedTeam.setDescription("updatedDescription");
    updatedTeam.setTeamUUID("team-uuid");

    // when get team by id is called (spy)
    TeamService tempTeamService = Mockito.spy(teamService);
    Mockito.doReturn(testTeam).when(tempTeamService).getTeamByTeamId(Mockito.any());

    // when update team is called
    Team savedUpdatedTeam = tempTeamService.updateTeam(updatedTeam);

    // then
    Mockito.verify(teamRepository, Mockito.times(1)).save(Mockito.any());
    assertEquals(testTeam.getTeamId(), savedUpdatedTeam.getTeamId());
    assertEquals(updatedTeam.getName(), savedUpdatedTeam.getName());
    assertEquals(updatedTeam.getDescription(), savedUpdatedTeam.getDescription());
    Mockito.verify(pusherService, Mockito.times(1))
        .updateTeam(Mockito.anyString(), Mockito.anyString());
  }

  @Test
  public void updateTeam_invalidInputs_throwsException() {
    // given
    Team updatedTeam = new Team();
    updatedTeam.setTeamId(1L);
    updatedTeam.setName(""); // empty name
    updatedTeam.setDescription("updatedDescription");
    updatedTeam.setTeamUUID("team-uuid");

    // when get team by id is called (spy)
    TeamService tempTeamService = Mockito.spy(teamService);
    Mockito.doReturn(testTeam).when(tempTeamService).getTeamByTeamId(Mockito.any());

    // then -> attempt to update team with empty name -> check that an error is thrown
    assertThrows(ResponseStatusException.class, () -> tempTeamService.updateTeam(updatedTeam));
    Mockito.verify(teamRepository, Mockito.never()).save(Mockito.any());
    Mockito.verify(pusherService, Mockito.never())
        .updateTeam(Mockito.anyString(), Mockito.anyString());
  }

  @Test
  public void updateTeam_teamNotFound_throwsException() {
    // given
    Team updatedTeam = new Team();
    updatedTeam.setTeamId(99L);
    updatedTeam.setName("updatedName");
    updatedTeam.setDescription("updatedDescription");
    updatedTeam.setTeamUUID("team-uuid");

    // when get team by id is called (spy)
    TeamService tempTeamService = Mockito.spy(teamService);
    Mockito.doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
        .when(tempTeamService)
        .getTeamByTeamId(Mockito.any());

    // then -> attempt to update team with invalid id -> check that an error is thrown
    assertThrows(ResponseStatusException.class, () -> tempTeamService.updateTeam(updatedTeam));
    Mockito.verify(teamRepository, Mockito.never()).save(Mockito.any());
    Mockito.verify(pusherService, Mockito.never())
        .updateTeam(Mockito.anyString(), Mockito.anyString());
  }
  // endregion
}
