package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically
 * transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g.,
 * UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for
 * creating information (POST).
 */
@Mapper
public interface DTOMapper {
  DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

  @Mapping(source = "username", target = "username")
  @Mapping(source = "password", target = "password")
  User convertLoginPostDTOtoEntity(LoginPostDTO loginPostDTO);

  @Mapping(source = "token", target = "token")
  @Mapping(source = "userId", target = "userId")
  AuthGetDTO convertEntityToAuthGetDTO(User user);

  @Mapping(source = "name", target = "name")
  @Mapping(source = "username", target = "username")
  @Mapping(source = "password", target = "password")
  User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

  @Mapping(source = "userId", target = "userId")
  @Mapping(source = "name", target = "name")
  @Mapping(source = "username", target = "username")
  UserGetDTO convertEntityToUserGetDTO(User user);

  @Mapping(source = "name", target = "name")
  @Mapping(source = "description", target = "description")
  Team convertTeamPostDTOtoEntity(TeamPostDTO teamPostDTO);

  @Mapping(source = "name", target = "name")
  @Mapping(source = "description", target = "description")
  Team convertTeamPutDTOtoEntity(TeamPutDTO teamPutDTO);

  @Mapping(source = "teamId", target = "teamId")
  @Mapping(source = "teamUUID", target = "teamUUID")
  @Mapping(source = "name", target = "name")
  @Mapping(source = "description", target = "description")
  TeamGetDTO convertEntityToTeamGetDTO(Team team);

  @Mapping(source = "teamUUID", target = "teamUUID")
  @Mapping(source = "receiverEmail", target = "receiverEmail", defaultValue = "")
  TeamInvitation convertTeamInvitationPostDTOtoEntity(TeamInvitationPostDTO teamInvitationPostDTO);

  @Mapping(target = "title", source = "title")
  @Mapping(target = "description", source = "description", defaultValue = "")
  Task convertTaskPostDTOtoEntity(TaskPostDTO taskPostDTO);

  @Mapping(target = "title", source = "title")
  @Mapping(target = "description", source = "description", defaultValue = "")
  @Mapping(target = "status", source = "status")
  Task convertTaskPutDTOtoEntity(TaskPutDTO taskPutDTO);

  @Mapping(source = "title", target = "title")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "creationDate", target = "creationDate")
  @Mapping(source = "status", target = "status")
  TaskGetDTO convertEntityToTaskGetDTO(Task task);

  @Mapping(source = "text", target = "text")
  @Mapping(source = "userId", target = "user.userId")
  Comment convertCommentPostDTOtoEntity(CommentPostDTO commentPostDTO);

  @Mapping(source = "commentId", target = "commentId")
  @Mapping(source = "text", target = "text")
  @Mapping(source = "creationDate", target = "creationDate")
  @Mapping(source = "user.userId", target = "authorId")
  @Mapping(source = "user.username", target = "authorName")
  CommentGetDTO convertEntityToCommentGetDTO(Comment comment);

  @Mapping(source = "goalMinutes", target = "goalMinutes")
  Session convertSessionPostDTOtoEntity(SessionPostDTO sessionPostDTO);

  @Mapping(
      source = "startDateTime", target = "startDateTime", qualifiedByName = "formatLocalDateTime")
  @Mapping(source = "endDateTime", target = "endDateTime", qualifiedByName = "formatLocalDateTime")
  @Mapping(source = "goalMinutes", target = "goalMinutes")
  SessionGetDTO
  convertEntityToSessionGetDTO(Session session);

  @Mapping(source = "userId", target = "userId")
  @Mapping(source = "teamId", target = "teamId")
  @Mapping(source = "channelName", target = "channelName")
  AgoraAuth convertAgoraAuthPostDTOtoEntity(AgoraAuthPostDTO agoraAuthPostDTO);

  @Mapping(source = "rtcToken", target = "rtcToken")
  @Mapping(source = "rtmToken", target = "rtmToken")
  AgoraAuthGetDTO convertEntityToAgoraAuthGetDTO(String rtcToken, String rtmToken);

  @Mapping(source = "promptParameter", target = "prompt")
  AIPrompt convertAIPromptTeamDescriptionPostDTOtoEntity(
      AIPromptTeamDescriptionPostDTO aiPromptTeamDescriptionPostDTO);

  @Mapping(source = "answer", target = "answer")
  AIPromptGetDTO convertEntityToAIPromptGetDTO(AIPrompt aiPrompt);

  default String formatLocalDateTime(LocalDateTime localDateTime) {
    return localDateTime == null
        ? null
        : localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }
}
