package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import ch.uzh.ifi.hase.soprafs24.constant.TaskStatus;
import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

  // region auth mappings
  @Test
  public void testAuthGetDTO_success() {
    // create User
    User user = new User();
    user.setToken("1");
    user.setUserId(1L);

    // MAP -> Create AuthGetDTO
    AuthGetDTO authGetDTO = DTOMapper.INSTANCE.convertEntityToAuthGetDTO(user);

    // check content
    assertEquals(user.getToken(), authGetDTO.getToken());
    assertEquals(user.getUserId(), authGetDTO.getUserId());
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
  public void testCreateTeam_fromTeamPutDTO_success() {
    // create TeamPostDTO
    TeamPutDTO teamPutDTO = new TeamPutDTO();
    teamPutDTO.setName("name");
    teamPutDTO.setDescription("description");

    // MAP -> Create Team
    Team team = DTOMapper.INSTANCE.convertTeamPutDTOtoEntity(teamPutDTO);

    // check content
    assertEquals(teamPutDTO.getName(), team.getName());
    assertEquals(teamPutDTO.getDescription(), team.getDescription());
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

  /* team inviation */
  @Test
  public void testTeamInvitation_noMail_fromTeamInvitationPostDTO_toTeamInvitation_success() {
    // create TeamInvitationPostDTO
    TeamInvitationPostDTO teamInvitationPostDTO = new TeamInvitationPostDTO();
    teamInvitationPostDTO.setTeamUUID("team-uuid");

    // map -> create TeamInvitation
    TeamInvitation teamInvitation =
        DTOMapper.INSTANCE.convertTeamInvitationPostDTOtoEntity(teamInvitationPostDTO);

    assertEquals(teamInvitationPostDTO.getTeamUUID(), teamInvitation.getTeamUUID());
    assertEquals("", teamInvitation.getReceiverEmail());
  }

  @Test
  public void testTeamInvitation_withMail_fromTeamInvitationPostDTO_toTeamInvitation_success() {
    // create TeamInvitationPostDTO
    TeamInvitationPostDTO teamInvitationPostDTO = new TeamInvitationPostDTO();
    teamInvitationPostDTO.setTeamUUID("team-uuid");
    teamInvitationPostDTO.setReceiverEmail("receiver@productiviteam.co");

    // map -> create TeamInvitation
    TeamInvitation teamInvitation =
        DTOMapper.INSTANCE.convertTeamInvitationPostDTOtoEntity(teamInvitationPostDTO);

    assertEquals(teamInvitationPostDTO.getTeamUUID(), teamInvitation.getTeamUUID());
    assertEquals(teamInvitationPostDTO.getReceiverEmail(), teamInvitation.getReceiverEmail());
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

  @Test
  public void testUpdateTask_fromTaskPutDTO_toTask_success() {
    // create TaskPutDTO
    TaskPutDTO taskPutDTO = new TaskPutDTO();
    taskPutDTO.setTaskId(1L);
    taskPutDTO.setTitle("new title");
    taskPutDTO.setDescription("new description");
    taskPutDTO.setStatus(TaskStatus.IN_SESSION);

    // MAP -> Create Task
    Task task = DTOMapper.INSTANCE.convertTaskPutDTOtoEntity(taskPutDTO);

    // check content
    assertEquals(taskPutDTO.getTaskId(), task.getTaskId());
    assertEquals(taskPutDTO.getTitle(), task.getTitle());
    assertEquals(taskPutDTO.getDescription(), task.getDescription());
    assertEquals(taskPutDTO.getStatus(), task.getStatus());
  }

  // endregion

  // region session mappings
  @Test
  public void testCreateSession_fromSessionPostDTO_toSession_success() {
    // create SessionPostDTO
    SessionPostDTO sessionPostDTO = new SessionPostDTO();
    sessionPostDTO.setGoalMinutes(30L);

    // MAP -> Create Session
    Session session = DTOMapper.INSTANCE.convertSessionPostDTOtoEntity(sessionPostDTO);

    // check content
    assertNull(session.getSessionId());
    assertNull(session.getStartDateTime());
    assertNull(session.getEndDateTime());
    assertEquals(sessionPostDTO.getGoalMinutes(), session.getGoalMinutes());
  }

  @Test
  public void testGetSession_fromSession_toSessionGetDTO_success() {
    LocalDateTime startDateTime = LocalDateTime.now().minusHours(3);
    LocalDateTime endDateTime = LocalDateTime.now();

    // create Session
    Session session = new Session();
    session.setSessionId(1L);
    session.setStartDateTime(startDateTime);
    session.setEndDateTime(endDateTime);
    session.setGoalMinutes(30L);

    // MAP -> Create SessionGetDTO
    SessionGetDTO sessionGetDTO = DTOMapper.INSTANCE.convertEntityToSessionGetDTO(session);

    System.out.println(sessionGetDTO.getStartDateTime()); // to verify in output

    // check content
    assertEquals(
        session.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
        sessionGetDTO.getStartDateTime());
    assertEquals(
        session.getEndDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
        sessionGetDTO.getEndDateTime());
    assertEquals(session.getGoalMinutes(), sessionGetDTO.getGoalMinutes());
  }
  // endregion

  // region comment mappings
  @Test
  public void testCreateComment_fromCommentPostDTO_toComment_success() {
    // create CommentPostDTO
    CommentPostDTO commentPostDTO = new CommentPostDTO();
    commentPostDTO.setText("This is a comment");
    commentPostDTO.setUserId(1L);

    // MAP -> Create Comment
    Comment comment = DTOMapper.INSTANCE.convertCommentPostDTOtoEntity(commentPostDTO);

    // check content
    assertEquals(commentPostDTO.getText(), comment.getText());
    assertEquals(commentPostDTO.getUserId(), comment.getUser().getUserId());
  }

  @Test
  public void testGetComment_fromComment_toCommentGetDTO_success() {
    // create Comment
    Comment comment = new Comment();
    comment.setCommentId(1L);
    comment.setText("This is a comment");
    comment.setCreationDate(LocalDateTime.now());
    User user = new User();
    user.setUserId(10L);
    user.setUsername("testUser");
    comment.setUser(user);

    // MAP -> Create CommentGetDTO
    CommentGetDTO commentGetDTO = DTOMapper.INSTANCE.convertEntityToCommentGetDTO(comment);

    // check content
    assertEquals(comment.getCommentId(), commentGetDTO.getCommentId());
    assertEquals(comment.getText(), commentGetDTO.getText());
    assertEquals(comment.getCreationDate(), commentGetDTO.getCreationDate());
    assertEquals(comment.getUser().getUserId(), commentGetDTO.getAuthorId());
    assertEquals(comment.getUser().getUsername(), commentGetDTO.getAuthorName());
  }
  // endregion

  // region ai prompt mappings
  @Test
  public void testCreateAIPrompt_fromAIPromptTeamDescriptionPostDTO_toAIPrompt_success() {
    // create AIPromptTeamDescriptionPostDTO
    AIPromptTeamDescriptionPostDTO aiPromptTeamDescriptionPostDTO =
        new AIPromptTeamDescriptionPostDTO();
    aiPromptTeamDescriptionPostDTO.setPromptParameter("team title");

    // MAP -> Create AIPrompt
    AIPrompt aiPrompt = DTOMapper.INSTANCE.convertAIPromptTeamDescriptionPostDTOtoEntity(
        aiPromptTeamDescriptionPostDTO);

    assertEquals(aiPromptTeamDescriptionPostDTO.getPromptParameter(), aiPrompt.getPrompt());
  }

  @Test
  public void testGetAIPrompt_fromAIPrompt_toAIPromptGetDTO_success() {
    // create AIPrompt
    AIPrompt aiPrompt = new AIPrompt();
    aiPrompt.setAnswer("answer");

    // MAP -> Create AIPromptGetDTO
    AIPromptGetDTO aiPromptGetDTO = DTOMapper.INSTANCE.convertEntityToAIPromptGetDTO(aiPrompt);

    assertEquals(aiPrompt.getAnswer(), aiPromptGetDTO.getAnswer());
  }
}
