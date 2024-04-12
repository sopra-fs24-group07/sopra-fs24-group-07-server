package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.Task;
import ch.uzh.ifi.hase.soprafs24.entity.Team;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DTOMapper {
  DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

  @Mapping(source = "username", target = "username")
  @Mapping(source = "password", target = "password")
  User convertLoginPostDTOtoEntity(LoginPostDTO loginPostDTO);

  @Mapping(source = "token", target = "token") AuthGetDTO convertEntityToAuthGetDTO(String token);

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

  @Mapping(source = "teamId", target = "teamId")
  @Mapping(source = "name", target = "name")
  @Mapping(source = "description", target = "description")
  TeamGetDTO convertEntityToTeamGetDTO(Team team);

  @Mapping(target = "title", source = "title", defaultValue = "")
  @Mapping(target = "description", source = "description", defaultValue = "")
  @Mapping(source = "status", target = "status")
  Task convertTaskPostDTOtoEntity(TaskPostDTO taskPostDTO);

  @Mapping(source = "title", target = "title")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "creationDate", target = "creationDate")
  @Mapping(source = "status", target = "status")
  TaskGetDTO convertEntityToTaskGetDTO(Task task);
}
