package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.uzh.ifi.hase.soprafs24.constant.TaskStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Task;
import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TaskGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TaskPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TeamGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TeamPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import org.junit.jupiter.api.Test;

/**
 * DTOMapperTest
 * Tests if the mapping between the internal and the external/API representation
 * works.
 */
public class DTOMapperTest {
  // region user mappings
  @Test
  public void testCreateUser_fromUserPostDTO_toUser_success() {
    // create UserPostDTO
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setName("name");
    userPostDTO.setUsername("username");
    userPostDTO.setPassword("password");

    // MAP -> Create user
    User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    // check content
    assertEquals(userPostDTO.getName(), user.getName());
    assertEquals(userPostDTO.getUsername(), user.getUsername());
    assertEquals(userPostDTO.getPassword(), user.getPassword());
  }

  @Test
  public void testGetUser_fromUser_toUserGetDTO_success() {
    // create User
    User user = new User();
    user.setName("Firstname Lastname");
    user.setUsername("firstname@lastname");
    user.setToken("1");

    // MAP -> Create UserGetDTO
    UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

    // check content
    assertEquals(user.getUserId(), userGetDTO.getUserId());
    assertEquals(user.getName(), userGetDTO.getName());
    assertEquals(user.getUsername(), userGetDTO.getUsername());
  }
  // endregion

  // region team mappings
  @Test
  public void testCreateTeam_fromTeamPostDTO_success() {
    // create TeamPostDTO
    TeamPostDTO teamPostDTO = new TeamPostDTO();
    teamPostDTO.setName("name");
    teamPostDTO.setDescription("description");

    // MAP -> Create Team
    Team team = DTOMapper.INSTANCE.convertTeamPostDTOtoEntity(teamPostDTO);

    // check content
    assertEquals(teamPostDTO.getName(), team.getName());
    assertEquals(teamPostDTO.getDescription(), team.getDescription());
  }

  @Test
  public void testGetTeam_fromTeam_toTeamGetDTO_success() {
    // create Team
    Team team = new Team();
    team.setTeamId(1L);
    team.setTeamUUID("team-uuid");
    team.setName("name");
    team.setDescription("description");

    // MAP -> Create TeamGetDTO
    TeamGetDTO teamGetDTO = DTOMapper.INSTANCE.convertEntityToTeamGetDTO(team);

    // check content
    assertEquals(team.getTeamId(), teamGetDTO.getTeamId());
    assertEquals(team.getTeamUUID(), teamGetDTO.getTeamUUID());
    assertEquals(team.getName(), teamGetDTO.getName());
    assertEquals(team.getDescription(), teamGetDTO.getDescription());
  }
  // endregion

  // region create task
  @Test
  public void testCreateTask_fromTaskPostDTO_toTask_success() {
    // create TaskPostDTO
    TaskPostDTO taskPostDTO = new TaskPostDTO();
    taskPostDTO.setTitle("write book");
    taskPostDTO.setDescription("A productive task");

    // MAP -> Create Task
    Task task = DTOMapper.INSTANCE.convertTaskPostDTOtoEntity(taskPostDTO);

    // check content
    assertEquals(taskPostDTO.getTitle(), task.getTitle());
    assertEquals(taskPostDTO.getDescription(), task.getDescription());
  }

  @Test
  public void testGetTask_fromTask_toTaskGetDTO_success() {
    // create Task
    Task task = new Task();
    task.setTaskId(73L);
    task.setTitle("write book");
    task.setDescription("A productive task");
    task.setStatus(TaskStatus.TODO);

    // MAP -> Create TaskGetDTO
    TaskGetDTO taskGetDTO = DTOMapper.INSTANCE.convertEntityToTaskGetDTO(task);

    // check content
    assertEquals(task.getTaskId(), taskGetDTO.getTaskId());
    assertEquals(task.getTitle(), taskGetDTO.getTitle());
    assertEquals(task.getDescription(), taskGetDTO.getDescription());
    assertEquals(task.getStatus(), taskGetDTO.getStatus()); // compare enums directly
  }
  // endregion
}
