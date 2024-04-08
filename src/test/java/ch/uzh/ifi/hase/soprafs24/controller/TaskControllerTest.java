// package ch.uzh.ifi.hase.soprafs24.controller;

// import static org.hamcrest.Matchers.is;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.mockito.BDDMockito.given;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// import ch.uzh.ifi.hase.soprafs24.entity.Task;
// import ch.uzh.ifi.hase.soprafs24.rest.dto.TaskPostDTO;
// import ch.uzh.ifi.hase.soprafs24.service.AuthorizationService;
// import ch.uzh.ifi.hase.soprafs24.service.TaskService;
// import ch.uzh.ifi.hase.soprafs24.service.TeamService;
// import java.util.ArrayList;
// import java.util.List;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mockito;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
// import org.springframework.web.server.ResponseStatusException;

// /**
//  * TaskControllerTest
//  * This is a WebMvcTest which allows to test the TaskController i.e. GET/POST
//  * request without actually sending them over the network.
//  * This tests if the TaskController works.
//  */
// @WebMvcTest(TeamController.class)
// public class TaskControllerTest {
//   @Autowired private MockMvc mockMvc;

//   @MockBean private TaskService taskService;
//   @MockBean private AuthorizationService authorizationService;
//   @MockBean private TeamService teamService;

//   @Test
//   public void createTask_validInput_taskCreated() throws Exception {
//     // given
//     Task task = new Task();
//     task.setTaskId(1L);
//     task.setTitle("Test Task");
//     task.setDescription("This is a test task.");

//     TaskPostDTO taskPostDTO = new TaskPostDTO();
//     taskPostDTO.setTitle("Test Task");
//     taskPostDTO.setDescription("This is a test task.");

//     given(taskService.createTask(Mockito.any())).willReturn(task);

//     // when/then -> do the request + validate the result
//     MockHttpServletRequestBuilder postRequest =
//         post("/api/v1/teams/1/tasks")
//             .contentType(MediaType.APPLICATION_JSON)
//             .content(ControllerTestHelper.asJsonString(taskPostDTO))
//             .header("Authorization", "1234");

//     // then
//     mockMvc.perform(postRequest)
//         .andExpect(status().isCreated())
//         .andExpect(jsonPath("$.taskId", is(task.getTaskId().intValue())))
//         .andExpect(jsonPath("$.title", is(task.getTitle())))
//         .andExpect(jsonPath("$.description", is(task.getDescription())));
//   }

//   // POST
//   @Test
//   public void createTask_missingFields_throwsError() throws Exception {
//     // given
//     TaskPostDTO taskPostDTO = new TaskPostDTO();
//     taskPostDTO.setTitle("Test Task");

//     given(taskService.createTask(Mockito.any()))
//         .willThrow(new ResponseStatusException(
//             HttpStatus.BAD_REQUEST, "Some needed fields are missing in the task object."));

//     // when/then -> do the request + validate the result
//     MockHttpServletRequestBuilder postRequest =
//         post("/api/v1/teams/1/tasks")
//             .contentType(MediaType.APPLICATION_JSON)
//             .content(ControllerTestHelper.asJsonString(taskPostDTO))
//             .header("Authorization", "1234");

//     // then
//     mockMvc.perform(postRequest)
//         .andExpect(status().isBadRequest())
//         .andExpect(
//             result -> assertTrue(result.getResolvedException() instanceof
//             ResponseStatusException))
//         .andExpect(result
//             -> assertTrue(result.getResolvedException().getMessage().contains(
//                 "Some needed fields are missing in the task object.")));
//   }

//   @Test
//   public void createTask_unauthorizedAccess_throwsError() throws Exception {
//     // given
//     TaskPostDTO taskPostDTO = new TaskPostDTO();
//     taskPostDTO.setTitle("Test Task");
//     taskPostDTO.setDescription("This is a test task.");

//     Mockito
//         .doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authorized to
//         access.")) .when(authorizationService) .isAuthorized(Mockito.any());

//     // when/then -> do the request + validate the result
//     MockHttpServletRequestBuilder postRequest =
//         post("/api/v1/teams/1/tasks")
//             .contentType(MediaType.APPLICATION_JSON)
//             .content(ControllerTestHelper.asJsonString(taskPostDTO))
//             .header("Authorization", "1234");

//     // then
//     mockMvc.perform(postRequest)
//         .andExpect(status().isUnauthorized())
//         .andExpect(
//             result -> assertTrue(result.getResolvedException() instanceof
//             ResponseStatusException))
//         .andExpect(result
//             -> assertTrue(
//                 result.getResolvedException().getMessage().contains("Not authorized to
//                 access.")));
//   }

//   // GET
//   @Test
//   public void getTasks_validInput_returnTasks() throws Exception {
//     // given
//     Task task = new Task();
//     task.setTaskId(1L);
//     task.setTitle("Test Task");
//     task.setDescription("This is a test task.");

//     List<Task> tasks = new ArrayList<>();
//     tasks.add(task);

//     given(taskService.getTasksByTeamId(Mockito.anyLong())).willReturn(tasks);

//     // when/then -> do the request + validate the result
//     MockHttpServletRequestBuilder getRequest =
//         get("/api/v1/teams/1/tasks").header("Authorization", "1234");

//     // then
//     mockMvc.perform(getRequest)
//         .andExpect(status().isOk())
//         .andExpect(jsonPath("$[0].taskId", is(task.getTaskId().intValue())))
//         .andExpect(jsonPath("$[0].title", is(task.getTitle())))
//         .andExpect(jsonPath("$[0].description", is(task.getDescription())));
//   }

//   @Test
//   public void getTasks_noTasksInTeam_throwsError() throws Exception {
//     // given
//     given(taskService.getTasksByTeamId(Mockito.anyLong()))
//         .willThrow(
//             new ResponseStatusException(HttpStatus.NOT_FOUND, "No tasks found for team with id
//             1"));

//     // when/then -> do the request + validate the result
//     MockHttpServletRequestBuilder getRequest =
//         get("/api/v1/teams/1/tasks").header("Authorization", "1234");

//     // then
//     mockMvc.perform(getRequest)
//         .andExpect(status().isNotFound())
//         .andExpect(
//             result -> assertTrue(result.getResolvedException() instanceof
//             ResponseStatusException))
//         .andExpect(result
//             -> assertTrue(result.getResolvedException().getMessage().contains(
//                 "No tasks found for team with id 1")));
//   }

//   @Test
//   public void getTasks_unauthorizedAccess_throwsError() throws Exception {
//     // given
//     Mockito
//         .doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authorized to
//         access.")) .when(authorizationService) .isAuthorized(Mockito.any());

//     // when/then -> do the request + validate the result
//     MockHttpServletRequestBuilder getRequest =
//         get("/api/v1/teams/1/tasks").header("Authorization", "1234");

//     // then
//     mockMvc.perform(getRequest)
//         .andExpect(status().isUnauthorized())
//         .andExpect(
//             result -> assertTrue(result.getResolvedException() instanceof
//             ResponseStatusException))
//         .andExpect(result
//             -> assertTrue(
//                 result.getResolvedException().getMessage().contains("Not authorized to
//                 access.")));
//   }
// }
