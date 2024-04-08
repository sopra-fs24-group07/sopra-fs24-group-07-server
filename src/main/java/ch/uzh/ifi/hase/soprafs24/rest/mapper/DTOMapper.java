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
  @Mapping(target = "userId", ignore = true) // Added this line
  @Mapping(target = "name", ignore = true) // Added this line
  @Mapping(target = "token", ignore = true) // Added this line
  User convertLoginPostDTOtoEntity(LoginPostDTO loginPostDTO);

  @Mapping(source = "token", target = "token") AuthGetDTO convertEntityToAuthGetDTO(String token);

  @Mapping(source = "name", target = "name")
  @Mapping(source = "username", target = "username")
  @Mapping(source = "password", target = "password")
  @Mapping(target = "userId", ignore = true) // Added this line
  @Mapping(target = "token", ignore = true) // Added this line
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

  @Mapping(source = "teamId", target = "team", qualifiedByName = "mapTeamIdToTeam")
  @Mapping(source = "title", target = "title")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "status", target = "status")
  @Mapping(target = "taskId", ignore = true)
  @Mapping(target = "creationDate", ignore = true)
  Task convertTaskPostDTOtoEntity(TaskPostDTO taskPostDTO);

  @Named("mapTeamIdToTeam")
  default Team mapTeamIdToTeam(Long teamID) {
    Team team = new Team();
    team.setTeamId(teamID);
    return team;
  }

  @Mapping(source = "taskId", target = "taskId")
  @Mapping(source = "team.teamId", target = "teamId")
  @Mapping(source = "title", target = "title")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "creationDate", target = "creationDate")
  @Mapping(source = "status", target = "status")
  TaskGetDTO convertEntityToTaskGetDTO(Task task);
}
